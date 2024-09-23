package net.onestorm.plugins.stormregen.paper.regeneration;

import net.onestorm.plugins.stormregen.paper.action.Action;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.Collection;

public class RegenerationData {

    private final Material triggerMaterial;
    private final BlockData replacementBlock;
    private final int delay;
    private final boolean preventItemDrops;
    private final boolean preventExperienceDrops;
    private final Collection<Action> actions = new ArrayList<>();

    public RegenerationData(Material triggerMaterial, BlockData replacementBlock, int delay,
                            boolean preventItemDrops, boolean preventExperienceDrops) {
        this.triggerMaterial = triggerMaterial;
        this.replacementBlock = replacementBlock;
        this.delay = delay;
        this.preventItemDrops = preventItemDrops;
        this.preventExperienceDrops = preventExperienceDrops;
    }

    public Material getTriggerMaterial() {
        return triggerMaterial;
    }

    public BlockData getReplacementBlock() {
        return replacementBlock;
    }

    public int getDelay() {
        return delay;
    }

    public boolean preventItemDrops() {
        return preventItemDrops;
    }

    public boolean preventExperienceDrops() {
        return preventExperienceDrops;
    }

    public Collection<Action> getActions() {
        return actions;
    }

}
