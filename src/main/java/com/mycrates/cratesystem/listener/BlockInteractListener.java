package com.mycrates.cratesystem.listener;

import com.mycrates.cratesystem.CrateSystemPlugin;
import com.mycrates.cratesystem.crate.Crate;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockInteractListener implements Listener {

    private final CrateSystemPlugin plugin;

    public BlockInteractListener(CrateSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        Location loc = block.getLocation();
        Crate crate = plugin.getCrateManager().getCrateAt(loc);
        if (crate == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        boolean opened = plugin.getCrateOpenService().tryOpenCrate(player, crate);
        if (opened) {
            playBlockEffects(loc);
        }
    }

    private void playBlockEffects(Location loc) {
        try {
            String soundName = plugin.getConfig().getString("settings.physical-crate.open-sound", "BLOCK_CHEST_OPEN");
            Sound sound = Sound.valueOf(soundName);
            loc.getWorld().playSound(loc, sound, 1f, 1f);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            String particleName = plugin.getConfig().getString("settings.physical-crate.particle", "SPELL_WITCH");
            Particle particle = Particle.valueOf(particleName);
            loc.getWorld().spawnParticle(particle, loc.clone().add(0.5, 1, 0.5), 20, 0.3, 0.3, 0.3, 0.01);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
