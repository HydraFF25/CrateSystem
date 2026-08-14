package com.mycrates.cratesystem.crate;

import com.mycrates.cratesystem.CrateSystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.mycrates.cratesystem.util.ItemBuilder;

/**
 * crates/*.yml dosyalarını okuyup Crate nesnelerine dönüştürür ve bellekte tutar.
 */
public class CrateManager {

    private final CrateSystemPlugin plugin;
    private final Map<String, Crate> crates = new LinkedHashMap<>();
    private final Map<String, File> crateFiles = new LinkedHashMap<>();
    private final File cratesFolder;

    public CrateManager(CrateSystemPlugin plugin) {
        this.plugin = plugin;
        this.cratesFolder = new File(plugin.getDataFolder(), "crates");
    }

    public void loadAll() {
        crates.clear();
        crateFiles.clear();

        if (!cratesFolder.exists()) {
            cratesFolder.mkdirs();
            plugin.saveResource("crates/example.yml", false);
        }

        File[] files = cratesFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                loadCrateFile(file);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Kasa dosyası yüklenemedi: " + file.getName(), e);
            }
        }
        plugin.getLogger().info(crates.size() + " kasa yüklendi.");
    }

    private void loadCrateFile(File file) {
        String id = file.getName().substring(0, file.getName().length() - 4).toLowerCase();
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        String displayName = cfg.getString("display-name", "&fKasa");

        ItemStack keyItem = readItem(cfg.getConfigurationSection("key"), Material.TRIPWIRE_HOOK, "&fAnahtar");
        ItemStack menuItem = readItem(cfg.getConfigurationSection("display-item"), Material.CHEST, displayName);

        List<CrateReward> rewards = new ArrayList<>();
        List<Map<?, ?>> rewardMaps = cfg.getMapList("rewards");
        for (Map<?, ?> map : rewardMaps) {
            Object idObj = map.get("id");
            String rid = idObj != null ? String.valueOf(idObj) : "odul";

            Object matObj = map.get("material");
            String matName = matObj != null ? String.valueOf(matObj) : "STONE";
            Material material = Material.matchMaterial(matName);
            if (material == null) material = Material.STONE;

            Object amountObj = map.get("amount");
            int amount = amountObj instanceof Number ? ((Number) amountObj).intValue() : 1;

            Object nameObj = map.get("name");
            String name = nameObj != null ? String.valueOf(nameObj) : rid;

            Object chanceObj = map.get("chance");
            double chance = chanceObj instanceof Number ? ((Number) chanceObj).doubleValue() : 1.0;

            List<String> lore = new ArrayList<>();
            Object loreObj = map.get("lore");
            if (loreObj instanceof List<?> loreList) {
                for (Object o : loreList) lore.add(String.valueOf(o));
            }

            List<String> commands = new ArrayList<>();
            Object cmdObj = map.get("commands");
            if (cmdObj instanceof List<?> cmdList) {
                for (Object o : cmdList) commands.add(String.valueOf(o));
            }

            rewards.add(new CrateReward(rid, material, amount, name, lore, chance, commands));
        }

        if (rewards.isEmpty()) {
            plugin.getLogger().warning("Kasa '" + id + "' için hiç ödül tanımlanmamış, atlanıyor.");
            return;
        }

        Crate crate = new Crate(id, displayName, keyItem, menuItem, rewards);

        List<String> locations = cfg.getStringList("physical-locations");
        for (String loc : locations) {
            crate.getPhysicalLocations().add(loc);
        }

        crates.put(id, crate);
        crateFiles.put(id, file);
    }

    private ItemStack readItem(ConfigurationSection section, Material fallbackMaterial, String fallbackName) {
        if (section == null) {
            return new ItemBuilder(fallbackMaterial).name(fallbackName).build();
        }
        String matName = section.getString("material", fallbackMaterial.name());
        Material material = Material.matchMaterial(matName);
        if (material == null) material = fallbackMaterial;
        String name = section.getString("name", fallbackName);
        List<String> lore = section.getStringList("lore");
        return new ItemBuilder(material).name(name).lore(lore).build();
    }

    public Crate getCrate(String id) {
        if (id == null) return null;
        return crates.get(id.toLowerCase());
    }

    public List<Crate> getCrates() {
        return new ArrayList<>(crates.values());
    }

    public Crate getCrateAt(Location loc) {
        String key = Crate.toLocationKey(loc);
        for (Crate crate : crates.values()) {
            if (crate.getPhysicalLocations().contains(key)) {
                return crate;
            }
        }
        return null;
    }

    /**
     * Bir kasanın fiziksel konum listesini ilgili yml dosyasına yazar.
     */
    public void persistPhysicalLocations(Crate crate) {
        File file = crateFiles.get(crate.getId());
        if (file == null) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.set("physical-locations", new ArrayList<>(crate.getPhysicalLocations()));
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Kasa konumları kaydedilemedi: " + file.getName(), e);
        }
    }

    public Location locationFromKey(String key) {
        String[] parts = key.split(",");
        if (parts.length != 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
