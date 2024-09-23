package net.onestorm.plugins.stormregen.paper.action;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

public interface Action {

    void execute(BlockState state, Player player);

}
