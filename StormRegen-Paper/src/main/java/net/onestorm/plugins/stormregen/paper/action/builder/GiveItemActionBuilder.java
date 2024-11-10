package net.onestorm.plugins.stormregen.paper.action.builder;

import net.onestorm.library.common.factory.BuildException;
import net.onestorm.library.storage.StorageMap;
import net.onestorm.plugins.stormregen.paper.action.Action;
import net.onestorm.plugins.stormregen.paper.action.ItemStackAction;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class GiveItemActionBuilder extends AbstractActionBuilder {

    private static final String BUILDER_NAME = "give-item";
    private static final boolean DEFAULT_DROP_ITEM_STACK = true;

    @Override
    public String getName() {
        return BUILDER_NAME;
    }

    @Override
    protected Action build(StorageMap storage, double probability, boolean shouldSendMessage,
                           String message, int minimumAmount, int maximumAmount) {

        boolean dropItemStack = storage.getBoolean("drop-item-stack").orElse(DEFAULT_DROP_ITEM_STACK);
        String giveInput = storage.getString("give-input")
            .orElseThrow(() -> new BuildException("Missing \"give-input\" key in storage while building: " + BUILDER_NAME));

        ItemStack itemStack;
        try {
            itemStack = Bukkit.getItemFactory().createItemStack(giveInput);
        } catch (IllegalArgumentException e) {
            throw new BuildException("Invalid value for \"give-input\" (input should be the same as /minecraft:give @p <input>): " + BUILDER_NAME, e);
        }

        return new ItemStackAction(probability, shouldSendMessage, message, minimumAmount, maximumAmount, dropItemStack, itemStack);

    }
}
