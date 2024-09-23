package net.onestorm.plugins.stormregen.paper.action.builder;

import net.onestorm.library.common.factory.BuildException;
import net.onestorm.library.storage.StorageMap;
import net.onestorm.plugins.stormregen.paper.action.Action;
import net.onestorm.plugins.stormregen.paper.action.ItemStackAction;
import net.onestorm.plugins.stormregen.paper.util.ItemStackUtil;
import org.bukkit.inventory.ItemStack;

public class ItemStackActionBuilder extends AbstractActionBuilder {
    private static final String BUILDER_NAME = "item-stack";
    private static final boolean DEFAULT_DROP_ITEM_STACK = true;

    @Override
    public String getName() {
        return BUILDER_NAME;
    }

    @Override
    protected Action build(StorageMap storage, double probability, boolean shouldSendMessage,
                           String message, int minimumAmount, int maximumAmount) {
        boolean dropItemStack = storage.getBoolean("drop-item-stack").orElse(DEFAULT_DROP_ITEM_STACK);
        StorageMap itemMap = storage.getMap("item-stack")
                                 .orElseThrow(() -> new BuildException("Missing \"item-stack\" key in storage while building: " + BUILDER_NAME));
        ItemStack itemStack = ItemStackUtil.fromStorage(itemMap);
        return new ItemStackAction(probability, shouldSendMessage, message, minimumAmount, maximumAmount, dropItemStack, itemStack);
    }
}
