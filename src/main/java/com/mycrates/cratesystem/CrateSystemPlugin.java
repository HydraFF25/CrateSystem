package com.mycrates.cratesystem;

import com.mycrates.cratesystem.command.CrateCommand;
import com.mycrates.cratesystem.command.KeyCommand;
import com.mycrates.cratesystem.crate.CrateManager;
import com.mycrates.cratesystem.crate.CrateOpenService;
import com.mycrates.cratesystem.key.KeyManager;
import com.mycrates.cratesystem.key.VirtualKeyStorage;
import com.mycrates.cratesystem.listener.BlockInteractListener;
import com.mycrates.cratesystem.listener.InventoryListener;
import org.bukkit.plugin.java.JavaPlugin;

public class CrateSystemPlugin extends JavaPlugin {

    private CrateManager crateManager;
    private KeyManager keyManager;
    private VirtualKeyStorage virtualKeyStorage;
    private CrateOpenService crateOpenService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.keyManager = new KeyManager(this);
        this.virtualKeyStorage = new VirtualKeyStorage(this);
        this.crateManager = new CrateManager(this);
        this.crateManager.loadAll();
        this.crateOpenService = new CrateOpenService(this);

        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockInteractListener(this), this);

        CrateCommand crateCommand = new CrateCommand(this);
        getCommand("crate").setExecutor(crateCommand);
        getCommand("crate").setTabCompleter(crateCommand);

        KeyCommand keyCommand = new KeyCommand(this);
        getCommand("key").setExecutor(keyCommand);
        getCommand("key").setTabCompleter(keyCommand);

        getLogger().info("CrateSystem başarıyla etkinleştirildi.");
    }

    @Override
    public void onDisable() {
        if (virtualKeyStorage != null) {
            virtualKeyStorage.save();
        }
        getLogger().info("CrateSystem devre dışı bırakıldı.");
    }

    public void reloadPlugin() {
        reloadConfig();
        crateManager.loadAll();
    }

    public CrateManager getCrateManager() {
        return crateManager;
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }

    public VirtualKeyStorage getVirtualKeyStorage() {
        return virtualKeyStorage;
    }

    public CrateOpenService getCrateOpenService() {
        return crateOpenService;
    }
}
