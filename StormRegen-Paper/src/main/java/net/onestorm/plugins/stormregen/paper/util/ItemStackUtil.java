package net.onestorm.plugins.stormregen.paper.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onestorm.library.common.factory.BuildException;
import net.onestorm.library.storage.StorageMap;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemStackUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final int DEFAULT_AMOUNT = 1;

    private ItemStackUtil() {}

    public static ItemStack fromStorage(StorageMap storage) {
        String materialString = storage.getString("material").orElseThrow(() -> new BuildException("Missing \"material\" key in storage"));
        Material material;
        try {
            material = Material.valueOf(materialString.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new BuildException("Could not find a material with the name: " + materialString);
        }
        int amount = storage.getInteger("amount").orElse(DEFAULT_AMOUNT);

        ItemStack item = new ItemStack(material, amount);

        storage.getString("display-name").ifPresent(displayName -> {
            Component displayNameComponent = MINI_MESSAGE.deserialize(displayName);
            item.editMeta(meta -> meta.displayName(displayNameComponent));
        });

        storage.getList("lore").ifPresent(storageList -> {
            List<Component> lore = new ArrayList<>();
            storageList.forEach(value -> {
                Component line;
                switch (value) {
                    case String string -> line = MINI_MESSAGE.deserialize(string);
                    case Number number -> line = Component.text(String.valueOf(number));
                    case Boolean bool -> line = Component.text(bool);
                    case null, default -> {
                        return; // continue
                    }
                }
                lore.add(line);
            });
            item.editMeta(meta -> meta.lore(lore));
        });

        return item;
    }

}
