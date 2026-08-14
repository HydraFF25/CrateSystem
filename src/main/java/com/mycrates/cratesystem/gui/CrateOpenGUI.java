package com.mycrates.cratesystem.gui;

import com.mycrates.cratesystem.CrateSystemPlugin;
import com.mycrates.cratesystem.crate.Crate;
import com.mycrates.cratesystem.crate.CrateReward;
import com.mycrates.cratesystem.util.ColorUtil;
import com.mycrates.cratesystem.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

/**
 * CrazyCrates/ExcellentCrates tarzı, ödüllerin bir şerit üzerinde kayıp
 * yavaşlayarak bir ödülde durduğu rulet animasyonunu oluşturur ve oynatır.
 */
public class CrateOpenGUI {

    // Şeridin bulunduğu satır (0-indexed) ve o satırdaki slotlar
    private static final int STRIP_ROW = 2; // 3. satır (0,1,2)
    private static final int[] STRIP_SLOTS = {18, 19, 20, 21, 22, 23, 24, 25, 26};
    private static final int POINTER_SLOT = 22; // STRIP_SLOTS'un ortası

    private final CrateSystemPlugin plugin;

    public CrateOpenGUI(CrateSystemPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Crate crate) {
        int totalSteps = plugin.getConfig().getInt("settings.animation.total-steps", 30);
        int startDelay = plugin.getConfig().getInt("settings.animation.start-delay-ticks", 1);
        int maxDelay = plugin.getConfig().getInt("settings.animation.max-delay-ticks", 9);
        // Şerit/pointer slotları 54'lük (6 satır) bir envanteri baz alır, bu yüzden sabittir.
        int size = 54;

        CrateOpenHolder holder = new CrateOpenHolder(crate);
        Inventory inventory = Bukkit.createInventory(holder, size,
                ColorUtil.color("&8» " + crate.getDisplayName() + " &8«"));
        holder.setInventory(inventory);

        decorateBorder(inventory, size);

        // Baştan itibaren şeridi rastgele ödüllerle doldur
        for (int slot : STRIP_SLOTS) {
            inventory.setItem(slot, crate.randomVisualReward().toItemStack());
        }
        // İşaretçi (pointer) göstergesi - üst ve alt satırda ok işareti
        placePointerMarkers(inventory, size);

        player.openInventory(inventory);

        // Kazanılacak ödül baştan (ağırlıklı olarak) belirlenir.
        CrateReward winningReward = crate.rollReward();

        runStep(player, holder, crate, winningReward, 0, totalSteps, startDelay, maxDelay);
    }

    private void runStep(Player player, CrateOpenHolder holder, Crate crate, CrateReward winningReward,
                          int step, int totalSteps, int startDelay, int maxDelay) {
        Inventory inv = holder.getInventory();
        // Oyuncu GUI'yi kapattıysa animasyonu durdur
        if (inv.getViewers().isEmpty()) {
            holder.setAnimating(false);
            return;
        }

        boolean lastStep = step >= totalSteps - 1;

        for (int slot : STRIP_SLOTS) {
            if (lastStep && slot == POINTER_SLOT) {
                inv.setItem(slot, winningReward.toItemStack());
            } else {
                inv.setItem(slot, crate.randomVisualReward().toItemStack());
            }
        }

        float pitch = 0.5f + (1.5f * step / Math.max(1, totalSteps));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, pitch);

        if (lastStep) {
            holder.setAnimating(false);
            finish(player, holder, crate, winningReward);
            return;
        }

        // Adım ilerledikçe gecikmeyi (deceleration) artır -> yavaşlama hissi
        double progress = (double) step / (double) totalSteps;
        int delay = (int) Math.round(startDelay + (maxDelay - startDelay) * Math.pow(progress, 2));

        Bukkit.getScheduler().runTaskLater(plugin, () ->
                runStep(player, holder, crate, winningReward, step + 1, totalSteps, startDelay, maxDelay), delay);
    }

    private void finish(Player player, CrateOpenHolder holder, Crate crate, CrateReward reward) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

        String msg = plugin.getConfig().getString("messages.crate-won", "&6Kazandın: %reward%");
        player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "")
                + msg.replace("%reward%", reward.getDisplayName())));

        // Komutları konsoldan çalıştır
        for (String cmd : reward.getCommands()) {
            String parsed = cmd.replace("%player%", player.getName());
            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed));
        }

        // Eğer komut tanımlı değilse item'ı doğrudan envantere ver
        if (reward.getCommands().isEmpty()) {
            ItemStack item = reward.toItemStack();
            player.getInventory().addItem(item).values()
                    .forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        }

        // Kısa bir süre sonra GUI'yi kapat
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.getOpenInventory().getTopInventory().getHolder() == holder) {
                player.closeInventory();
            }
        }, 40L);
    }

    private void decorateBorder(Inventory inv, int size) {
        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            if (row != STRIP_ROW) {
                inv.setItem(i, glass);
            }
        }
    }

    private void placePointerMarkers(Inventory inv, int size) {
        ItemStack marker = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                .name("&a▼ Kazanılan Ödül ▼")
                .build();
        int above = POINTER_SLOT - 9;
        int below = POINTER_SLOT + 9;
        if (above >= 0) inv.setItem(above, marker);
        if (below < size) inv.setItem(below, marker);
    }
}
