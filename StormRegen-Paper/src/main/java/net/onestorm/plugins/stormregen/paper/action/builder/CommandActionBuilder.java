package net.onestorm.plugins.stormregen.paper.action.builder;

import net.onestorm.library.common.factory.BuildException;
import net.onestorm.library.storage.StorageMap;
import net.onestorm.plugins.stormregen.paper.StormRegen;
import net.onestorm.plugins.stormregen.paper.action.Action;
import net.onestorm.plugins.stormregen.paper.action.CommandAction;

public class CommandActionBuilder extends AbstractActionBuilder {

    private static final String BUILDER_NAME = "command";

    private final StormRegen plugin;

    public CommandActionBuilder(StormRegen plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return BUILDER_NAME;
    }

    @Override
    protected Action build(StorageMap storage, double probability, boolean shouldSendMessage,
                           String message, int minimumAmount, int maximumAmount) {
        String command = storage.getString("command").orElseThrow(() -> new BuildException("Missing \"command\" key in configuration"));

        return new CommandAction(plugin, probability, shouldSendMessage, message, minimumAmount, maximumAmount, command);
    }
}
