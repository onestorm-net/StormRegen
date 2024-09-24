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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Set;


public class WorldGuardListener implements Listener {

    private final StormRegen plugin;

    public WorldGuardListener(StormRegen plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BreakBlockEvent event) {
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
}
