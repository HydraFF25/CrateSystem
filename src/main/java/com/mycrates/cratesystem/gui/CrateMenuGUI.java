package com.mycrates.cratesystem.gui;

import com.mycrates.cratesystem.CrateSystemPlugin;
import com.mycrates.cratesystem.crate.Crate;
import com.mycrates.cratesystem.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

/**
 * /crate menu komutu ile açılan, tüm kasaların listelendiği ana menü.
 */
public class CrateMenuGUI {

    private final CrateSystemPlugin plugin;

    public CrateMenuGUI(CrateSystemPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        List<Crate> crates = plugin.getCrateManager().getCrates();
        int size = Math.max(9, ((crates.size() / 9) + 1) * 9);
        size = Math.min(54, size);

        CrateMenuHolder holder = new CrateMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, size, ColorUtil.color("&8Kasalar"));
        holder.setInventory(inventory);

        int slot = 0;
        for (Crate crate : crates) {
            if (slot >= size) break;
            inventory.setItem(slot, crate.getMenuDisplayItem());
            holder.getSlotToCrate().put(slot, crate.getId());
            slot++;
        }

        player.openInventory(inventory);
    }
}
