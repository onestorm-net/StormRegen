package net.onestorm.plugins.stormregen.paper.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.onestorm.plugins.stormregen.paper.StormRegen;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings("UnstableApiUsage")
public class ReloadCommand implements BasicCommand {

    private static final String COMMAND_PERMISSION = "stormregen.command.reload";

    private final StormRegen plugin;

    public ReloadCommand(StormRegen plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable String permission() {
        return COMMAND_PERMISSION;
    }

    @Override
    public void execute(@NotNull CommandSourceStack source, @NotNull String[] arguments) {
        CommandSender sender = source.getSender();

        if (arguments.length != 0) {
            sender.sendMessage(Component.text("Usage: /stormregenreload"));
            return;
        }

        sender.sendMessage(Component.text("Reloading...", NamedTextColor.GRAY));
        plugin.reload();
        sender.sendMessage(Component.text("Reloaded StormRegen", NamedTextColor.AQUA));
    }
}
