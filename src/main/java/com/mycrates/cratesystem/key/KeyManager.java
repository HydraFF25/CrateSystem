package com.mycrates.cratesystem.key;

import com.mycrates.cratesystem.CrateSystemPlugin;
import com.mycrates.cratesystem.crate.Crate;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Fiziksel (item olarak elde tutulan) kasa anahtarlarını yönetir.
 * Anahtarın hangi kasaya ait olduğu ItemMeta'nın PersistentDataContainer'ında saklanır.
 */
public class KeyManager {

    private final CrateSystemPlugin plugin;
    private final NamespacedKey crateKeyId;

    public KeyManager(CrateSystemPlugin plugin) {
        this.plugin = plugin;
        this.crateKeyId = new NamespacedKey(plugin, "crate_key_id");
    }

    /**
     * Verilen kasa için işaretlenmiş bir anahtar item'ı üretir.
     */
    public ItemStack createKeyItem(Crate crate) {
        ItemStack item = crate.getKeyItem();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(crateKeyId, PersistentDataType.STRING, crate.getId());
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Bu item'ın hangi kasanın anahtarı olduğunu döndürür, değilse null.
     */
    public String getCrateIdFromKey(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(crateKeyId, PersistentDataType.STRING);
    }

    public boolean isKeyFor(ItemStack item, String crateId) {
        String id = getCrateIdFromKey(item);
        return id != null && id.equalsIgnoreCase(crateId);
    }

    /**
     * Oyuncunun envanterinde bu kasa için en az 1 fiziksel anahtar olup olmadığını kontrol eder.
     */
    public boolean hasPhysicalKey(Player player, String crateId) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isKeyFor(item, crateId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Oyuncunun envanterinden 1 adet fiziksel anahtar düşer. Başarılıysa true döner.
     */
    public boolean consumePhysicalKey(Player player, String crateId) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (isKeyFor(item, crateId)) {
                if (item.getAmount() <= 1) {
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - 1);
                }
                return true;
            }
        }
        return false;
    }

    public void giveKey(Player player, Crate crate, int amount) {
        ItemStack key = createKeyItem(crate);
        key.setAmount(Math.max(1, amount));
        player.getInventory().addItem(key).values()
                .forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
    }
}
