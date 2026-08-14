package com.mycrates.cratesystem.command;

import com.mycrates.cratesystem.CrateSystemPlugin;
import com.mycrates.cratesystem.crate.Crate;
import com.mycrates.cratesystem.gui.CrateMenuGUI;
import com.mycrates.cratesystem.util.ColorUtil;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CrateCommand implements CommandExecutor, TabCompleter {

    private final CrateSystemPlugin plugin;
    private final CrateMenuGUI crateMenuGUI;

    public CrateCommand(CrateSystemPlugin plugin) {
        this.plugin = plugin;
        this.crateMenuGUI = new CrateMenuGUI(plugin);
    }

    private String msg(String path) {
        return ColorUtil.color(plugin.getConfig().getString("messages.prefix", "")
                + plugin.getConfig().getString(path, ""));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                crateMenuGUI.open(player);
                return true;
            }
            sender.sendMessage("Kullanım: /crate <reload|list|menu|give|setblock|removeblock|open>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload" -> {
                if (!requireAdmin(sender)) return true;
                plugin.reloadPlugin();
                sender.sendMessage(msg("messages.reloaded"));
                return true;
            }
            case "list" -> {
                String names = plugin.getCrateManager().getCrates().stream()
                        .map(Crate::getId).collect(Collectors.joining(", "));
                sender.sendMessage(msg("messages.crate-list").replace("%crates%", names));
                return true;
            }
            case "menu" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Bu komut sadece oyuncular içindir.");
                    return true;
                }
                crateMenuGUI.open(player);
                return true;
            }
            case "open" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Bu komut sadece oyuncular içindir.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(msg("messages.invalid-usage").replace("%usage%", "/crate open <kasa>"));
                    return true;
                }
                Crate crate = plugin.getCrateManager().getCrate(args[1]);
                if (crate == null) {
                    sender.sendMessage(msg("messages.crate-not-found").replace("%crate%", args[1]));
                    return true;
                }
                plugin.getCrateOpenService().tryOpenCrate(player, crate);
                return true;
            }
            case "give" -> {
                if (!requireAdmin(sender)) return true;
                if (args.length < 3) {
                    sender.sendMessage(msg("messages.invalid-usage").replace("%usage%", "/crate give <oyuncu> <kasa> [miktar]"));
                    return true;
                }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.color("&cOyuncu bulunamadı veya çevrimdışı."));
                    return true;
                }
                Crate crate = plugin.getCrateManager().getCrate(args[2]);
                if (crate == null) {
                    sender.sendMessage(msg("messages.crate-not-found").replace("%crate%", args[2]));
                    return true;
                }
                int amount = 1;
                if (args.length >= 4) {
                    try {
                        amount = Integer.parseInt(args[3]);
                    } catch (NumberFormatException ignored) {
                    }
                }
                plugin.getKeyManager().giveKey(target, crate, amount);
                sender.sendMessage(msg("messages.key-given")
                        .replace("%player%", target.getName())
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%crate%", crate.getDisplayName()));
                target.sendMessage(msg("messages.key-received")
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%crate%", crate.getDisplayName()));
                return true;
            }
            case "setblock" -> {
                if (!requireAdmin(sender)) return true;
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Bu komut sadece oyuncular içindir.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(msg("messages.invalid-usage").replace("%usage%", "/crate setblock <kasa>"));
                    return true;
                }
                Crate crate = plugin.getCrateManager().getCrate(args[1]);
                if (crate == null) {
                    sender.sendMessage(msg("messages.crate-not-found").replace("%crate%", args[1]));
                    return true;
                }
                Block target = getTargetBlock(player);
                if (target == null) {
                    sender.sendMessage(ColorUtil.color("&cBaktığın yerde geçerli bir blok yok (menzil: 6 blok)."));
                    return true;
                }
                crate.addPhysicalLocation(target.getLocation());
                plugin.getCrateManager().persistPhysicalLocations(crate);
                sender.sendMessage(msg("messages.block-set").replace("%crate%", crate.getDisplayName()));
                return true;
            }
            case "removeblock" -> {
                if (!requireAdmin(sender)) return true;
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Bu komut sadece oyuncular içindir.");
                    return true;
                }
                Block target = getTargetBlock(player);
                if (target == null) {
                    sender.sendMessage(ColorUtil.color("&cBaktığın yerde geçerli bir blok yok."));
                    return true;
                }
                Crate crate = plugin.getCrateManager().getCrateAt(target.getLocation());
                if (crate == null) {
                    sender.sendMessage(ColorUtil.color("&cBu blokta tanımlı bir kasa yok."));
                    return true;
                }
                crate.removePhysicalLocation(target.getLocation());
                plugin.getCrateManager().persistPhysicalLocations(crate);
                sender.sendMessage(msg("messages.block-removed"));
                return true;
            }
            default -> {
                sender.sendMessage("Kullanım: /crate <reload|list|menu|give|setblock|removeblock|open>");
                return true;
            }
        }
    }

    private boolean requireAdmin(CommandSender sender) {
        if (!sender.hasPermission("cratesystem.admin")) {
            sender.sendMessage(msg("messages.no-permission"));
            return false;
        }
        return true;
    }

    private Block getTargetBlock(Player player) {
        RayTraceResult result = player.rayTraceBlocks(6);
        if (result == null || result.getHitBlock() == null) return null;
        return result.getHitBlock();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.addAll(List.of("reload", "list", "menu", "give", "setblock", "removeblock", "open"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("open")
                || args[0].equalsIgnoreCase("setblock"))) {
            if (args[0].equalsIgnoreCase("give")) {
                plugin.getServer().getOnlinePlayers().forEach(p -> options.add(p.getName()));
            } else {
                plugin.getCrateManager().getCrates().forEach(c -> options.add(c.getId()));
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            plugin.getCrateManager().getCrates().forEach(c -> options.add(c.getId()));
        }
        String current = args[args.length - 1].toLowerCase();
        return options.stream().filter(o -> o.toLowerCase().startsWith(current)).collect(Collectors.toList());
    }
}
