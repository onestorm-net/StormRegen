package net.onestorm.plugins.stormregen.paper.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.onestorm.plugins.stormregen.paper.StormRegen;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReloadCommand extends BukkitCommand {

    private static final String COMMAND_NAME = "stormregenreload";
    private static final List<String> COMMAND_ALIASES = List.of("srreload");
    private static final String COMMAND_PERMISSION = "stormregen.command.reload";

    private final StormRegen plugin;

    public ReloadCommand(StormRegen plugin) {
        super(COMMAND_NAME);
        this.plugin = plugin;
        setPermission(COMMAND_PERMISSION);
        setAliases(COMMAND_ALIASES);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] arguments) {
        handleCommand(sender, arguments);
        return true;
    }

    private void handleCommand(@NotNull CommandSender sender, @NotNull String[] arguments) {

        if (arguments.length != 0) {
            sender.sendMessage(Component.text("Usage: /stormregenreload"));
            return;
        }

        sender.sendMessage(Component.text("Reloading...", NamedTextColor.GRAY));
        plugin.reload();
        sender.sendMessage(Component.text("Reloaded StormRegen", NamedTextColor.AQUA));


    }
}
