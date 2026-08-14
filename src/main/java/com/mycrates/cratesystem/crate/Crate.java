package com.mycrates.cratesystem.crate;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tek bir kasa türünü (id ile ayırt edilir) temsil eder.
 * Örn: "efsanevi", "epic", "common" gibi kasalar.
 */
public class Crate {

    private final String id;
    private final String displayName;
    private final ItemStack keyItem;
    private final ItemStack menuDisplayItem;
    private final List<CrateReward> rewards;
    private final Set<String> physicalLocations = ConcurrentHashMap.newKeySet(); // "world,x,y,z"

    public Crate(String id, String displayName, ItemStack keyItem, ItemStack menuDisplayItem,
                 List<CrateReward> rewards) {
        this.id = id;
        this.displayName = displayName;
        this.keyItem = keyItem;
        this.menuDisplayItem = menuDisplayItem;
        this.rewards = rewards;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getKeyItem() {
        return keyItem.clone();
    }

    public ItemStack getMenuDisplayItem() {
        return menuDisplayItem.clone();
    }

    public List<CrateReward> getRewards() {
        return rewards;
    }

    public Set<String> getPhysicalLocations() {
        return physicalLocations;
    }

    public static String toLocationKey(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public boolean hasPhysicalLocation(Location loc) {
        return physicalLocations.contains(toLocationKey(loc));
    }

    public void addPhysicalLocation(Location loc) {
        physicalLocations.add(toLocationKey(loc));
    }

    public void removePhysicalLocation(Location loc) {
        physicalLocations.remove(toLocationKey(loc));
    }

    /**
     * Ağırlıklı rastgele (weighted random) bir ödül seçer.
     */
    public CrateReward rollReward() {
        double totalWeight = rewards.stream().mapToDouble(CrateReward::getChance).sum();
        if (totalWeight <= 0) {
            return rewards.get(ThreadLocalRandom.current().nextInt(rewards.size()));
        }
        double roll = ThreadLocalRandom.current().nextDouble(0, totalWeight);
        double cumulative = 0;
        for (CrateReward reward : rewards) {
            cumulative += reward.getChance();
            if (roll <= cumulative) {
                return reward;
            }
        }
        return rewards.get(rewards.size() - 1);
    }

    /**
     * Rulet animasyonu için rastgele bir ödül (ağırlıksız, sadece görsel amaçlı).
     */
    public CrateReward randomVisualReward() {
        return rewards.get(ThreadLocalRandom.current().nextInt(rewards.size()));
    }
}
