package com.mycrates.cratesystem.command;

import com.mycrates.cratesystem.CrateSystemPlugin;
import com.mycrates.cratesystem.crate.Crate;
import com.mycrates.cratesystem.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /key give <oyuncu> <kasa> [miktar] - fiziksel anahtar verir.
 * /key vgive <oyuncu> <kasa> [miktar] - sanal (envanter yer kaplamayan) anahtar verir.
 */
public class KeyCommand implements CommandExecutor, TabCompleter {

    private final CrateSystemPlugin plugin;

    public KeyCommand(CrateSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cratesystem.admin")) {
            sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "")
                    + plugin.getConfig().getString("messages.no-permission", "")));
            return true;
        }

        if (args.length < 3 || !(args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("vgive"))) {
            sender.sendMessage(ColorUtil.color("&cKullanım: /key <give|vgive> <oyuncu> <kasa> [miktar]"));
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtil.color("&cOyuncu bulunamadı veya çevrimdışı."));
            return true;
        }

        Crate crate = plugin.getCrateManager().getCrate(args[2]);
        if (crate == null) {
            sender.sendMessage(ColorUtil.color("&cKasa bulunamadı: " + args[2]));
            return true;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException ignored) {
            }
        }

        if (args[0].equalsIgnoreCase("give")) {
            plugin.getKeyManager().giveKey(target, crate, amount);
        } else {
            plugin.getVirtualKeyStorage().addVirtualKeys(target.getUniqueId(), crate.getId(), amount);
        }

        sender.sendMessage(ColorUtil.color("&a" + target.getName() + " oyuncusuna " + amount + "x "
                + crate.getDisplayName() + " &aanahtarı verildi."));
        target.sendMessage(ColorUtil.color("&a" + amount + "x " + crate.getDisplayName() + " &aanahtarı aldın!"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.addAll(List.of("give", "vgive"));
        } else if (args.length == 2) {
            plugin.getServer().getOnlinePlayers().forEach(p -> options.add(p.getName()));
        } else if (args.length == 3) {
            plugin.getCrateManager().getCrates().forEach(c -> options.add(c.getId()));
        }
        String current = args[args.length - 1].toLowerCase();
        return options.stream().filter(o -> o.toLowerCase().startsWith(current)).collect(Collectors.toList());
    }
}
