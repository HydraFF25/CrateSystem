package com.mycrates.cratesystem.key;

import com.mycrates.cratesystem.CrateSystemPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * /crate open komutu ile fiziksel item olmadan kullanılabilen sanal anahtarları
 * data.yml içinde saklar. Yol: players.<uuid>.<crateId> = miktar
 */
public class VirtualKeyStorage {

    private final CrateSystemPlugin plugin;
    private final File file;
    private YamlConfiguration data;

    public VirtualKeyStorage(CrateSystemPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "data.yml oluşturulamadı!", e);
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "data.yml kaydedilemedi!", e);
        }
    }

    private String path(UUID uuid, String crateId) {
        return "players." + uuid + "." + crateId.toLowerCase();
    }

    public int getVirtualKeys(UUID uuid, String crateId) {
        return data.getInt(path(uuid, crateId), 0);
    }

    public void addVirtualKeys(UUID uuid, String crateId, int amount) {
        int current = getVirtualKeys(uuid, crateId);
        data.set(path(uuid, crateId), current + amount);
        save();
    }

    /**
     * 1 adet sanal anahtar düşürür. Yeterli anahtar yoksa false döner.
     */
    public boolean consumeVirtualKey(UUID uuid, String crateId) {
        int current = getVirtualKeys(uuid, crateId);
        if (current <= 0) return false;
        data.set(path(uuid, crateId), current - 1);
        save();
        return true;
    }
}
