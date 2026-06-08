package org.flennn.lightdeathmessages.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.flennn.lightdeathmessages.config.PluginSettings;
import org.flennn.lightdeathmessages.util.VersionHelper;

public final class Console {
    private static final String PREFIX = "&8[&eLightDeathMessages&8] ";

    private Console() {
    }

    public static void info(String message) {
        send("&bINFO", message);
    }

    public static void success(String message) {
        send("&aOK", message);
    }

    public static void warn(String message) {
        send("&eWARN", message);
    }

    public static void error(String message) {
        send("&cERROR", message);
    }

    public static void startup(String version, PluginSettings settings, VersionHelper versionHelper, boolean loaded, boolean placeholderApiLoaded) {
        line("&8&m--------------------------------------------------");
        line("&8[&eLightDeathMessages&8]");
        line("&8&m--------------------------------------------------");
        line("&eStatus        &8: " + (loaded ? "&aReady" : "&cReload failed"));
        line("&eTheme Source  &8: &f" + settings.getSelectedTheme());
        line("&eBroadcast     &8: &f" + settings.getDefaultBroadcastMode());
        line("&eDebug Mode    &8: " + (settings.isDebug() ? "&eEnabled" : "&7Disabled"));
        line("&eMade by       &8: &fflennn &8| &eVersion &8: &f" + version);
        line("&8&m--------------------------------------------------");
        if (loaded) {
            success("LightDeathMessages is ready.");
        } else {
            error("LightDeathMessages started with errors. Fix the reload error above, then run /deathmessages reload.");
        }
    }

    private static void send(String level, String message) {
        Bukkit.getConsoleSender().sendMessage(color(PREFIX + level + " &7" + message));
    }

    private static void line(String message) {
        Bukkit.getConsoleSender().sendMessage(color(message));
    }

    private static String platform(VersionHelper versionHelper) {
        if (versionHelper.isFolia()) {
            return "Folia";
        }
        if (versionHelper.isPaper()) {
            return "Paper";
        }
        return "Spigot/Bukkit";
    }

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message == null ? "" : message);
    }
}
