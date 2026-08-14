package com.mycrates.cratesystem.crate;

import com.mycrates.cratesystem.CrateSystemPlugin;
import com.mycrates.cratesystem.gui.CrateOpenGUI;
import com.mycrates.cratesystem.util.ColorUtil;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Bir oyuncunun kasa açma isteğini (izin + anahtar kontrolü) doğrulayıp
 * animasyonu başlatan merkezi servis. Menü GUI'si, fiziksel kasa ve
 * /crate open komutu hepsi bu servisi kullanır.
 */
public class CrateOpenService {

    private final CrateSystemPlugin plugin;
    private final CrateOpenGUI crateOpenGUI;
    // Aynı anda birden fazla kasa animasyonu açmasını engellemek için
    private final Set<UUID> currentlyOpening = new HashSet<>();

    public CrateOpenService(CrateSystemPlugin plugin) {
        this.plugin = plugin;
        this.crateOpenGUI = new CrateOpenGUI(plugin);
    }

    public boolean tryOpenCrate(Player player, Crate crate) {
        String prefix = ColorUtil.color(plugin.getConfig().getString("messages.prefix", ""));

        if (!player.hasPermission("cratesystem.use")) {
            player.sendMessage(prefix + ColorUtil.color(plugin.getConfig().getString("messages.no-permission", "")));
            return false;
        }

        if (currentlyOpening.contains(player.getUniqueId())) {
            player.sendMessage(prefix + ColorUtil.color(plugin.getConfig().getString("messages.already-opening", "")));
            return false;
        }

        boolean hasPhysicalKey = plugin.getKeyManager().hasPhysicalKey(player, crate.getId());
        boolean hasVirtualKey = plugin.getVirtualKeyStorage().getVirtualKeys(player.getUniqueId(), crate.getId()) > 0;

        if (!hasPhysicalKey && !hasVirtualKey) {
            player.sendMessage(prefix + ColorUtil.color(plugin.getConfig().getString("messages.no-key", "")));
            return false;
        }

        if (hasPhysicalKey) {
            plugin.getKeyManager().consumePhysicalKey(player, crate.getId());
        } else {
            plugin.getVirtualKeyStorage().consumeVirtualKey(player.getUniqueId(), crate.getId());
        }

        currentlyOpening.add(player.getUniqueId());
        crateOpenGUI.open(player, crate);

        // Animasyon süresine göre kilidi kaldır (güvenlik payı ile)
        int totalSteps = plugin.getConfig().getInt("settings.animation.total-steps", 30);
        int maxDelay = plugin.getConfig().getInt("settings.animation.max-delay-ticks", 9);
        long estimatedTicks = (long) totalSteps * maxDelay + 60L;
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> currentlyOpening.remove(player.getUniqueId()), estimatedTicks);

        return true;
    }
}
