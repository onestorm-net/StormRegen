package net.onestorm.plugins.stormregen.paper.listener;


import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.ProtectionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.onestorm.plugins.stormregen.paper.StormRegen;
import net.onestorm.plugins.stormregen.paper.regeneration.RegenerationData;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlockBreakListener implements Listener {

    private static final long TICKS_IN_SECOND = 20L;
    private static final ProtectionQuery PROTECTION_QUERY = new ProtectionQuery();

    private final Map<Block, Regeneration> regenerationMap = new ConcurrentHashMap<>();
    private final StormRegen plugin;

    public BlockBreakListener(StormRegen plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // WorldGuard start
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        Location location = BukkitAdapter.adapt(block.getLocation());

        if (!query.testState(location, localPlayer, plugin.getBlockRegenerationFlag())) {
            return; // not a regen region
        }

        if (!PROTECTION_QUERY.testBlockBreak(player, block)) {
            return; // calls blockbreakevent, to check if the player is allowed to break this block
        }
        // WorldGuard end

        RegenerationData regenerationData = plugin.getRegenerationDataMap().get(block.getType());

        if (regenerationData == null) {
            return;
        }

        if (regenerationData.preventExperienceDrops()) {
            event.setExpToDrop(0);
        }

        if (regenerationData.preventItemDrops()) {
            event.setDropItems(false);
        }

        final BukkitScheduler scheduler = plugin.getServer().getScheduler();
        final BlockState blockState = block.getState();

        // replacement
        final RegenerationData finalRegenerationData = regenerationData;
        scheduler.runTask(plugin, () -> {
            final BlockData replacementBlock = finalRegenerationData.getReplacementBlock();
            if (!replacementBlock.getMaterial().isAir()) {
                block.setBlockData(finalRegenerationData.getReplacementBlock());
            }
            finalRegenerationData.getActions().forEach(blockAction -> blockAction.execute(blockState, player));
        });

        if (regenerationMap.containsKey(block)) {
            return;
        }

        // regeneration
        BukkitTask task = scheduler.runTaskLater(plugin, () -> {
            final Regeneration regeneration = regenerationMap.remove(block);
            if (regeneration == null) return;
            block.setBlockData(regeneration.originalBlockState().getBlockData());
        }, regenerationData.getDelay() * TICKS_IN_SECOND);
        regenerationMap.put(block, new Regeneration(task, block.getState()));
    }

    public void close() {
        HandlerList.unregisterAll(this);
        regenerationMap.forEach((block, regeneration) -> {
            regeneration.task().cancel();
            block.setBlockData(regeneration.originalBlockState().getBlockData());
        });
    }

    private record Regeneration(BukkitTask task, BlockState originalBlockState) { }

}
