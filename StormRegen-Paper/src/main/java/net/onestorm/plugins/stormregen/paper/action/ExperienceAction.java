package net.onestorm.plugins.stormregen.paper.action;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onestorm.plugins.stormregen.paper.StormRegen;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.Random;

public class ExperienceAction implements Action {

    private static final Random RANDOM = new Random();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final StormRegen plugin;
    private final double probability;
    private final boolean shouldSendMessage;
    private final String message;
    private final int minimumAmount;
    private final int maximumAmount;
    private final boolean dropExperience;

    public ExperienceAction(StormRegen plugin, double probability, boolean shouldSendMessage, String message,
                            int minimumAmount, int maximumAmount, boolean dropExperience) {
        this.plugin = plugin;
        this.probability = probability;
        this.shouldSendMessage = shouldSendMessage;
        this.message = message;
        this.minimumAmount = minimumAmount;
        this.maximumAmount = maximumAmount;
        this.dropExperience = dropExperience;
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

        if (dropExperience) {
            player.getWorld().spawn(state.getLocation(), ExperienceOrb.class, experienceOrb -> experienceOrb.setExperience(amount));
        } else {
            PlayerExpChangeEvent event = new PlayerExpChangeEvent(player, amount);
            plugin.getServer().getPluginManager().callEvent(event);
            player.giveExp(event.getAmount());
        }

        if (shouldSendMessage) {
            player.sendMessage(MINI_MESSAGE.deserialize(message));
        }

    }
}
