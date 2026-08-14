package com.mycrates.cratesystem.util;

import org.bukkit.ChatColor;

public class ColorUtil {

    private ColorUtil() {
    }

    public static String color(String input) {
        if (input == null) return "";
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
