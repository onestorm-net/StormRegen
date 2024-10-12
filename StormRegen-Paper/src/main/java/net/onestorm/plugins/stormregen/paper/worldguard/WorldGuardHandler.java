package net.onestorm.plugins.stormregen.paper.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.onestorm.plugins.stormregen.paper.StormRegen;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Set;


public class WorldGuardHandler implements EventExecutor, Listener {

    private final StormRegen plugin;

    public WorldGuardHandler(StormRegen plugin) {
        this.plugin = plugin;
    }

    public void handle(BreakBlockEvent event) {
        Event.Result originalResult = event.getResult();
        Object cause = event.getCause().getRootCause();

        if (cause instanceof Player player) {
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

            for (Block block : event.getBlocks()) {
                Location location = BukkitAdapter.adapt(block.getLocation());
                ApplicableRegionSet regionSet = container.createQuery().getApplicableRegions(location);
                Set<Material> materials = regionSet.queryValue(localPlayer, plugin.getAllowBlockBreakFlag());
                if (materials != null && materials.contains(block.getType())) {
                    event.setResult(Event.Result.ALLOW);
                } else {
                    event.setResult(originalResult);
                    break;
                }
            }
        }
    }

    public void register(EventPriority priority) {
        plugin.getServer().getPluginManager().registerEvent(BreakBlockEvent.class, this, priority, this, plugin, true);
    }

    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) {
        if (!(event instanceof BreakBlockEvent breakBlockEvent)) {
            return;
        }
        handle(breakBlockEvent);
    }
}
