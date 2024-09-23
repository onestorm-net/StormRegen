package net.onestorm.plugins.stormregen.paper.action;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ItemStackAction implements Action {

    private static final Random RANDOM = new Random();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final double probability;
    private final boolean shouldSendMessage;
    private final String message;
    private final int minimumAmount;
    private final int maximumAmount;
    private final boolean dropItemStack;
    private final ItemStack itemStack;

    public ItemStackAction(double probability, boolean shouldSendMessage, String message,
                           int minimumAmount, int maximumAmount, boolean dropItemStack, ItemStack itemStack) {
        this.probability = probability;
        this.shouldSendMessage = shouldSendMessage;
        this.message = message;
        this.minimumAmount = minimumAmount;
        this.maximumAmount = maximumAmount;
        this.dropItemStack = dropItemStack;
        this.itemStack = itemStack;
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

        itemStack.setAmount(amount);

        if (dropItemStack) {
            player.getWorld().dropItemNaturally(state.getLocation(), itemStack);
        } else {
            player.getInventory().addItem(itemStack); // note: items are lost when the players inventory is full
        }

        if (shouldSendMessage) {
            player.sendMessage(MINI_MESSAGE.deserialize(message));
        }
    }
}
