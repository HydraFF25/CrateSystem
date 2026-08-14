package com.mycrates.cratesystem.listener;

import com.mycrates.cratesystem.CrateSystemPlugin;
import com.mycrates.cratesystem.crate.Crate;
import com.mycrates.cratesystem.gui.CrateMenuHolder;
import com.mycrates.cratesystem.gui.CrateOpenHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryListener implements Listener {

    private final CrateSystemPlugin plugin;

    public InventoryListener(CrateSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof CrateOpenHolder) {
            // Rulet animasyonu sırasında hiçbir item alınamaz/taşınamaz
            event.setCancelled(true);
            return;
        }

        if (holder instanceof CrateMenuHolder menuHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            String crateId = menuHolder.getSlotToCrate().get(event.getRawSlot());
            if (crateId == null) return;

            Crate crate = plugin.getCrateManager().getCrate(crateId);
            if (crate == null) return;

            player.closeInventory();
            plugin.getCrateOpenService().tryOpenCrate(player, crate);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof CrateOpenHolder) {
            event.setCancelled(true);
        }
    }
}
