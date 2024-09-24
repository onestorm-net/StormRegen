package net.onestorm.plugins.stormregen.paper.worldguard.flag;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public class BlockMaterialFlag extends Flag<Material> {

    public BlockMaterialFlag(String name) {
        super(name);
    }

    @Override
    public Material parseInput(FlagContext context) throws InvalidFlagFormat {
        Material material = Material.matchMaterial(context.getUserInput());
        if (material == null) {
            throw new InvalidFlagFormat("Unable to find the material! Please refer to https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html for valid ids");
        }

        if (!material.isBlock()) {
            throw new InvalidFlagFormat("This material isn't seen as 'placeable block', use alternative id");
        }

        return material;
    }

    @Override
    public Material unmarshal(@Nullable Object o) {
        assert o != null;
        Material material = Material.matchMaterial(o.toString());
        if (material == null) {
            material = Material.matchMaterial(o.toString(), true);
        }
        return material;
    }

    @Override
    public Object marshal(Material material) {
        return material.name();
    }
}
