package net.onestorm.plugins.stormregen.paper;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.onestorm.library.common.factory.BuildException;
import net.onestorm.library.common.factory.Factory;
import net.onestorm.library.common.factory.GenericFactory;
import net.onestorm.library.common.factory.context.StorageBuildContext;
import net.onestorm.library.storage.StorageMap;
import net.onestorm.library.storage.file.FileStorage;
import net.onestorm.library.storage.file.json.JsonStorage;
import net.onestorm.plugins.stormregen.paper.action.Action;
import net.onestorm.plugins.stormregen.paper.action.builder.CommandActionBuilder;
import net.onestorm.plugins.stormregen.paper.action.builder.ExperienceActionBuilder;
import net.onestorm.plugins.stormregen.paper.action.builder.ItemStackActionBuilder;
import net.onestorm.plugins.stormregen.paper.command.ReloadCommand;
import net.onestorm.plugins.stormregen.paper.listener.BlockBreakHandler;
import net.onestorm.plugins.stormregen.paper.regeneration.RegenerationData;
import net.onestorm.plugins.stormregen.paper.worldguard.WorldGuardHandler;
import net.onestorm.plugins.stormregen.paper.worldguard.flag.BlockMaterialFlag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class StormRegen extends JavaPlugin {

    // regeneration data
    public static final int DEFAULT_REGENERATION_DELAY = 10; // seconds;
    public static final String DEFAULT_REPLACEMENT_BLOCK_DATA = "minecraft:air";
    public static final boolean DEFAULT_PREVENT_ITEM_DROPS = false;
    public static final boolean DEFAULT_PREVENT_EXPERIENCE_DROPS = false;
    private static final String REGENERATION_DATA_FILE_NAME = "block-list.json";

    // configuration
    public static final String WORLD_GUARD_PRIORITY_PATH = "event-priority.world-guard-break-block";
    public static final String BUKKIT_PRIORITY_PATH = "event-priority.bukkit-block-break";
    public static final String DEFAULT_EVENT_PRIORITY_STRING = "LOWEST";
    public static final EventPriority DEFAULT_EVENT_PRIORITY = EventPriority.LOWEST;
    private static final String CONFIGURATION_FILE_NAME = "configuration.json";

    private final FileStorage regenerationDataStorage = new JsonStorage();
    private final FileStorage configurationStorage = new JsonStorage();
    private final Factory<Action> actionFactory = new GenericFactory<>();

    private EventPriority blockBreakEventPriority;
    private EventPriority worldGuardEventPriority;
    private StateFlag blockRegenerationFlag;
    private SetFlag<Material> allowBlockBreakFlag;
    private BlockBreakHandler blockBreakHandler;
    private WorldGuardHandler worldGuardHandler;
    private Map<Material, RegenerationData> regenerationDataMap = new ConcurrentHashMap<>();

    @Override
    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            blockRegenerationFlag = new StateFlag("block-regeneration", false);
            registry.register(blockRegenerationFlag);
            allowBlockBreakFlag = new SetFlag<>("allow-block-break", new BlockMaterialFlag(null));
            registry.register(allowBlockBreakFlag);

        } catch (FlagConflictException e) {
            getLogger().log(Level.WARNING, "Conflicting flag", e);
        }
    }

    @Override
    public void onEnable() {
        // action factory
        loadFactory();
        // load configs
        loadConfiguration();
        // load regeneration data from storage
        loadRegenerationDataMap();
        // load events
        loadEvents();
        // load commands
        loadCommands();
    }

    @Override
    public void onDisable() {
        blockBreakHandler.close();
    }

    private void loadFactory() {
        actionFactory.registerBuilder(new CommandActionBuilder(this));
        actionFactory.registerBuilder(new ExperienceActionBuilder(this));
        actionFactory.registerBuilder(new ItemStackActionBuilder());
    }

    private void loadConfiguration() {
        File file = new File(getDataFolder(), CONFIGURATION_FILE_NAME);
        if (!file.exists()) {
            saveResource(CONFIGURATION_FILE_NAME, false);
        }
        try {
            configurationStorage.load(file);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not load " + CONFIGURATION_FILE_NAME + " file", e);
            return;
        }

        String worldGuardPriorityString = configurationStorage.getString(WORLD_GUARD_PRIORITY_PATH).orElse(DEFAULT_EVENT_PRIORITY_STRING);
        String bukkitPriorityString = configurationStorage.getString(BUKKIT_PRIORITY_PATH).orElse(DEFAULT_EVENT_PRIORITY_STRING);
        try {
            worldGuardEventPriority = EventPriority.valueOf(worldGuardPriorityString);
        } catch (IllegalArgumentException e) {
            getLogger().log(Level.WARNING, "Invalid priority given for \"" + WORLD_GUARD_PRIORITY_PATH + "\". defaulting to " + DEFAULT_EVENT_PRIORITY.name(), e);
            worldGuardEventPriority = DEFAULT_EVENT_PRIORITY;
        }

        try {
            blockBreakEventPriority = EventPriority.valueOf(bukkitPriorityString);
        } catch (IllegalArgumentException e) {
            getLogger().log(Level.WARNING, "Invalid priority given for \"" + BUKKIT_PRIORITY_PATH + "\". defaulting to " + DEFAULT_EVENT_PRIORITY.name(), e);
            blockBreakEventPriority = DEFAULT_EVENT_PRIORITY;
        }

    }

    private void loadRegenerationDataMap() {
        File file = new File(getDataFolder(), REGENERATION_DATA_FILE_NAME);
        if (!file.exists()) {
            saveResource(REGENERATION_DATA_FILE_NAME, false);
        }
        try {
            regenerationDataStorage.load(file);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not load " + REGENERATION_DATA_FILE_NAME + " file", e);
            return;
        }

        Map<Material, RegenerationData> newRegenerationDataMap = new ConcurrentHashMap<>();
        Optional<StorageMap> optionalRegenerationDataMap = regenerationDataStorage.getMap("block-list");
        if (optionalRegenerationDataMap.isPresent()) {
            StorageMap regenerationDataMap = optionalRegenerationDataMap.get();
            regenerationDataMap.forEach((key, value) -> {
                if (!(value instanceof StorageMap storageMap)) {
                    getLogger().log(Level.WARNING, "The path 'block-list." + key + "' is not a Json Object");
                    return;
                }
                try {
                    RegenerationData regenerationData = loadRegenerationData(storageMap);
                    newRegenerationDataMap.put(regenerationData.getTriggerMaterial(), regenerationData);
                } catch (Exception e) {
                    getLogger().log(Level.WARNING, "Uncaught exception while creating RegenerationData (block-list." + key + ")", e);
                }
            });
        }
        regenerationDataMap = newRegenerationDataMap;
    }

    private RegenerationData loadRegenerationData(StorageMap storage) {
        String triggerMaterialString = storage.getString("trigger-material")
            .orElseThrow(() -> new IllegalArgumentException("Missing key: \"trigger-material\""));
        Material triggerMaterial = Material.valueOf(triggerMaterialString.toUpperCase(Locale.ENGLISH));
        String replacementBlockString = storage.getString("replacement-block-data").orElse(DEFAULT_REPLACEMENT_BLOCK_DATA);
        BlockData replacementBlockData = Bukkit.createBlockData(replacementBlockString);
        int delay = storage.getInteger("regeneration-delay").orElse(DEFAULT_REGENERATION_DELAY);
        boolean preventItemDrops = storage.getBoolean("prevent-item-drops").orElse(DEFAULT_PREVENT_ITEM_DROPS);
        boolean preventExperienceDrops = storage.getBoolean("prevent-experience-drops").orElse(DEFAULT_PREVENT_EXPERIENCE_DROPS);

        RegenerationData data = new RegenerationData(triggerMaterial, replacementBlockData, delay, preventItemDrops, preventExperienceDrops);

        Optional<StorageMap> optionalActionMap = storage.getMap("actions");

        if (optionalActionMap.isEmpty()) {
            return data;
        }

        StorageMap actionMap = optionalActionMap.get();

        actionMap.forEach((key, value) -> {
            if (!(value instanceof StorageMap actionStorage)) {
                return;
            }

            Action action;
            try {
                action = actionFactory.build(new StorageBuildContext(actionStorage));
            } catch (BuildException e) {
                getLogger().log(Level.WARNING, "Build exception while building an action: " + key, e);
                return;
            }

            data.getActions().add(action);
        });

        return data;
    }

    private void loadEvents() {
        if (blockBreakHandler != null) {
            blockBreakHandler.close(); // unregisters and regenerates all blocks
        }
        if (worldGuardHandler != null) {
            HandlerList.unregisterAll(worldGuardHandler);
        }

        blockBreakHandler = new BlockBreakHandler(this);
        worldGuardHandler = new WorldGuardHandler(this);

        blockBreakHandler.register(blockBreakEventPriority);
        worldGuardHandler.register(worldGuardEventPriority);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void loadCommands() {
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("stormregenreload", List.of("srreload"), new ReloadCommand(this));
        });
    }

    public void reload() {
        loadRegenerationDataMap();
        loadConfiguration();
        loadEvents();
    }

    public Map<Material, RegenerationData> getRegenerationDataMap() {
        return regenerationDataMap;
    }

    public StateFlag getBlockRegenerationFlag() {
        return blockRegenerationFlag;
    }
    public SetFlag<Material> getAllowBlockBreakFlag() {
        return allowBlockBreakFlag;
    }
}
