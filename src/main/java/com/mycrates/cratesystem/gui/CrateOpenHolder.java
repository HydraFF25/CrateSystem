package com.mycrates.cratesystem.gui;

import com.mycrates.cratesystem.crate.Crate;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * CS:GO tarzı rulet animasyonunun oynadığı envanterin sahibi.
 * Listener bu tür sayesinde "bu bir kasa açma GUI'si mi" diye anlar.
 */
public class CrateOpenHolder implements InventoryHolder {

    private final Crate crate;
    private Inventory inventory;
    private boolean animating = true;

    public CrateOpenHolder(Crate crate) {
        this.crate = crate;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Crate getCrate() {
        return crate;
    }

    public boolean isAnimating() {
        return animating;
    }

    public void setAnimating(boolean animating) {
        this.animating = animating;
    }
}
