package net.onestorm.plugins.stormregen.paper.action;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onestorm.plugins.stormregen.paper.StormRegen;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import java.util.Random;

public class CommandAction implements Action {

    private static final Random RANDOM = new Random();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final StormRegen plugin;
    private final double probability;
    private final boolean shouldSendMessage;
    private final String message;
    private final int minimumAmount;
    private final int maximumAmount;
    private final String command;

    public CommandAction(StormRegen plugin, double probability, boolean shouldSendMessage, String message,
                         int minimumAmount, int maximumAmount, String command) {
        this.plugin = plugin;
        this.probability = probability;
        this.shouldSendMessage = shouldSendMessage;
        this.message = message;
        this.minimumAmount = minimumAmount;
        this.maximumAmount = maximumAmount;
        this.command = command;
    }

    @Override
    public void execute(BlockState state, Player player) {

        if (!(probability >= 1.0 || Math.random() < probability)) {
            return;
        }

        int amount;
        if (minimumAmount < maximumAmount) {
            amount = RANDOM.nextInt(minimumAmount, maximumAmount);
        } else {
            amount = maximumAmount;
        }

        String command = this.command;
        command = command.replaceAll("<amount>", String.valueOf(amount));
        command = command.replaceAll("<player>", player.getName());

        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);

        if (shouldSendMessage) {
            player.sendMessage(MINI_MESSAGE.deserialize(message));
        }
    }
}
