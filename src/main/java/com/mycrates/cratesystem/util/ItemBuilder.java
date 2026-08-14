package com.mycrates.cratesystem.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Basit zincirlenebilir ItemStack oluşturucu.
 */
public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack base) {
        this.item = base.clone();
        this.meta = item.getItemMeta();
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(Math.max(1, amount));
        return this;
    }

    public ItemBuilder name(String colored) {
        if (meta != null) {
            meta.setDisplayName(ColorUtil.color(colored));
        }
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        if (meta != null && lines != null) {
            List<String> colored = new ArrayList<>();
            for (String line : lines) {
                colored.add(ColorUtil.color(line));
            }
            meta.setLore(colored);
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        if (meta != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, PersistentDataType<String, String> type, String value) {
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(key, type, value);
        }
        return this;
    }

    public ItemBuilder edit(Consumer<ItemMeta> consumer) {
        if (meta != null) {
            consumer.accept(meta);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
}
