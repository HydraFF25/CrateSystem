package com.mycrates.cratesystem.crate;

import com.mycrates.cratesystem.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * Bir kasadan çıkabilecek tek bir ödülü temsil eder.
 */
public class CrateReward {

    private final String id;
    private final Material material;
    private final int amount;
    private final String displayName;
    private final List<String> lore;
    private final double chance; // ağırlık (weight) olarak kullanılır
    private final List<String> commands;

    public CrateReward(String id, Material material, int amount, String displayName,
                        List<String> lore, double chance, List<String> commands) {
        this.id = id;
        this.material = material;
        this.amount = amount;
        this.displayName = displayName;
        this.lore = lore == null ? Collections.emptyList() : lore;
        this.chance = Math.max(0.0, chance);
        this.commands = commands == null ? Collections.emptyList() : commands;
    }

    public String getId() {
        return id;
    }

    public double getChance() {
        return chance;
    }

    public List<String> getCommands() {
        return commands;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * GUI'de gösterilecek/oyuncuya verilecek item.
     */
    public ItemStack toItemStack() {
        return new ItemBuilder(material)
                .amount(amount)
                .name(displayName)
                .lore(lore)
                .build();
    }
}
