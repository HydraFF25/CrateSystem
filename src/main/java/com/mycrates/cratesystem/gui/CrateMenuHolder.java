package com.mycrates.cratesystem.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tüm kasaların listelendiği ana menünün holder'ı.
 * Slot -> kasa id eşlemesini tutar.
 */
public class CrateMenuHolder implements InventoryHolder {

    private Inventory inventory;
    private final Map<Integer, String> slotToCrate = new LinkedHashMap<>();

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Map<Integer, String> getSlotToCrate() {
        return slotToCrate;
    }
}
