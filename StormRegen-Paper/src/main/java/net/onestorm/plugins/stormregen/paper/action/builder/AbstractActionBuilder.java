package net.onestorm.plugins.stormregen.paper.action.builder;

import net.onestorm.library.common.factory.BuildException;
import net.onestorm.library.common.factory.Builder;
import net.onestorm.library.common.factory.context.BuildContext;
import net.onestorm.library.common.factory.context.StorageBuildContext;
import net.onestorm.library.storage.StorageMap;
import net.onestorm.plugins.stormregen.paper.action.Action;

import java.util.Optional;

public abstract class AbstractActionBuilder implements Builder<Action> {

    private static final double DEFAULT_PROBABILITY = 0.0;
    private static final String DEFAULT_MESSAGE = "<red>Error: No message set</red>";
    private static final int DEFAULT_AMOUNT = 0;

    @Override
    public Action build(BuildContext context) {
        if (!(context instanceof StorageBuildContext storageContext)) {
            throw new BuildException("Context is not an instance of StorageBuildContext.");
        }
        StorageMap storage = storageContext.getStorage();
        double probability = storage.getDouble("probability").orElse(DEFAULT_PROBABILITY);
        Optional<String> optionalMessage = storage.getString("message");
        boolean shouldSendMessage = optionalMessage.isPresent();
        String message = optionalMessage.orElse(DEFAULT_MESSAGE);
        int minimumAmount = storage.getInteger("minimum-amount").orElse(storage.getInteger("amount").orElse(DEFAULT_AMOUNT));
        int maximumAmount = storage.getInteger("maximum-amount").orElse(storage.getInteger("amount").orElse(DEFAULT_AMOUNT));
        return build(storage, probability, shouldSendMessage, message, minimumAmount, maximumAmount);
    }

    protected abstract Action build(StorageMap storage, double probability, boolean shouldSendMessage,
                                    String message, int minimumAmount, int maximumAmount);

}
