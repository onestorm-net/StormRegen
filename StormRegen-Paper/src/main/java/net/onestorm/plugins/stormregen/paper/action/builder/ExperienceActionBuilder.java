package net.onestorm.plugins.stormregen.paper.action.builder;

import net.onestorm.library.storage.StorageMap;
import net.onestorm.plugins.stormregen.paper.StormRegen;
import net.onestorm.plugins.stormregen.paper.action.Action;
import net.onestorm.plugins.stormregen.paper.action.ExperienceAction;

public class ExperienceActionBuilder extends AbstractActionBuilder {
    private static final String BUILDER_NAME = "experience";
    private static final boolean DEFAULT_DROP_EXPERIENCE = true;

    private final StormRegen plugin;

    public ExperienceActionBuilder(StormRegen plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return BUILDER_NAME;
    }

    @Override
    protected Action build(StorageMap storage, double probability, boolean shouldSendMessage,
                           String message, int minimumAmount, int maximumAmount) {
        boolean dropExperience = storage.getBoolean("drop-experience").orElse(DEFAULT_DROP_EXPERIENCE);
        return new ExperienceAction(plugin, probability, shouldSendMessage, message, minimumAmount, maximumAmount, dropExperience);
    }
}
