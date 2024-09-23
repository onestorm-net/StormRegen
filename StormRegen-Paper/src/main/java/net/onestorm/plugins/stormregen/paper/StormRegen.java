package net.onestorm.plugins.stormregen.paper;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
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
import net.onestorm.plugins.stormregen.paper.listener.BlockBreakListener;
import net.onestorm.plugins.stormregen.paper.regeneration.RegenerationData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class StormRegen extends JavaPlugin {

    public static final int DEFAULT_REGENERATION_DELAY = 10; // seconds;
    public static final String DEFAULT_REPLACEMENT_BLOCK_DATA = "minecraft:air";
    public static final boolean DEFAULT_PREVENT_ITEM_DROPS = false;
    public static final boolean DEFAULT_PREVENT_EXPERIENCE_DROPS = false;
    private static final String REGENERATION_DATA_FILE_NAME = "block-list.json";

    private final FileStorage storage = new JsonStorage();
    private final Factory<Action> actionFactory = new GenericFactory<>();
    private StateFlag blockRegenerationFlag;
    private BlockBreakListener listener;
    private Map<Material, RegenerationData> regenerationDataMap = new ConcurrentHashMap<>();

    @Override
    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("block-regeneration", false);
            registry.register(flag);
            blockRegenerationFlag = flag;
        } catch (FlagConflictException e) {
            getLogger().log(Level.WARNING, "Conflict flag: block-regeneration", e);
        }
    }

    @Override
    public void onEnable() {
        listener = new BlockBreakListener(this);
        loadFactory();
        loadRegenerationDataMap();
        loadCommands();
    }

    @Override
    public void onDisable() {
        listener.close();
    }

    private void loadRegenerationDataMap() {
        File file = new File(getDataFolder(), REGENERATION_DATA_FILE_NAME);
        if (!file.exists()) {
            saveResource(REGENERATION_DATA_FILE_NAME, false);
        }
        try {
            storage.load(file);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not load configuration file", e);
            return;
        }

        Map<Material, RegenerationData> newRegenerationDataMap = new ConcurrentHashMap<>();
        Optional<StorageMap> optionalRegenerationDataMap = storage.getMap("block-list");
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
        String triggerMaterialString = storage.getString("trigger-material").orElseThrow(() -> {
            return new IllegalArgumentException("Missing key: \"trigger-material\"");
        });
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

    private void loadCommands() {
        getServer().getCommandMap().register(getName(), new ReloadCommand(this));
    }

    private void loadFactory() {
        actionFactory.registerBuilder(new CommandActionBuilder(this));
        actionFactory.registerBuilder(new ExperienceActionBuilder(this));
        actionFactory.registerBuilder(new ItemStackActionBuilder(this));
    }
    public void reload() {
        loadRegenerationDataMap();
    }

    public Map<Material, RegenerationData> getRegenerationDataMap() {
        return regenerationDataMap;
    }

    public StateFlag getBlockRegenerationFlag() {
        return blockRegenerationFlag;
    }
}
