package org.flennn.lightdeathmessages.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.flennn.lightdeathmessages.DeathMessagesPlugin;
import org.flennn.lightdeathmessages.util.DebugLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ConfigManager {
    private final DeathMessagesPlugin plugin;
    private final DebugLogger logger;
    private volatile PluginSettings settings;

    public ConfigManager(DeathMessagesPlugin plugin, DebugLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        boolean debug = config.getBoolean("debug", false);
        boolean warnOnce = config.getBoolean("logging.warn-once", true);
        logger.configure(debug, warnOnce);

        BroadcastMode broadcastMode = enumValue(BroadcastMode.class, config.getString("broadcast.mode"), BroadcastMode.GLOBAL, "broadcast.mode");
        FormattingMode formattingMode = enumValue(FormattingMode.class, config.getString("formatting.mode"), FormattingMode.LEGACY, "formatting.mode");
        double radius = Math.max(0.0D, config.getDouble("broadcast.radius", 100.0D));
        double maxRadius = Math.max(0.0D, config.getDouble("broadcast.max-radius", 500.0D));
        if (maxRadius > 0.0D && radius > maxRadius) {
            logger.warn("broadcast-radius-capped", "Broadcast radius " + radius + " is higher than max-radius " + maxRadius + ". Using " + maxRadius + ".");
            radius = maxRadius;
        }

        settings = new PluginSettings(
                config.getBoolean("enabled", true),
                debug,
                config.getBoolean("hide-vanilla-death-message", true),
                broadcastMode,
                radius,
                maxRadius,
                lowerSet(config.getStringList("worlds.enabled")),
                lowerSet(config.getStringList("worlds.disabled")),
                perWorldModes(config.getConfigurationSection("worlds.per-world")),
                formattingMode,
                config.getBoolean("placeholders.placeholderapi", true),
                config.getBoolean("display.weapon-name", true),
                config.getBoolean("display.killer-health", true),
                config.getBoolean("display.distance", true),
                config.getBoolean("display.death-location", false),
                config.getBoolean("display.world-name", true),
                lowerSet(config.getStringList("messages.disabled-categories")),
                config.getString("permissions.silent", "deathmessages.silent"),
                config.getString("permissions.bypass", "deathmessages.bypass"),
                config.getBoolean("permissions.respect-silent", true),
                config.getBoolean("permissions.respect-bypass", true),
                config.getBoolean("logging.warn-missing-keys", true),
                warnOnce,
                config.getString("fallback.unknown-message", "&8[&c\\u2620&8] &c{victim} &7died."),
                config.getString("themes.selected-theme", "messages")
        );
    }

    public PluginSettings getSettings() {
        return settings;
    }

    private Map<String, BroadcastMode> perWorldModes(ConfigurationSection section) {
        Map<String, BroadcastMode> modes = new HashMap<>();
        if (section == null) {
            return modes;
        }
        for (String world : section.getKeys(false)) {
            String value = section.getString(world + ".broadcast-mode");
            modes.put(world.toLowerCase(Locale.ROOT), enumValue(BroadcastMode.class, value, BroadcastMode.GLOBAL, "worlds.per-world." + world + ".broadcast-mode"));
        }
        return modes;
    }

    private Set<String> lowerSet(Iterable<String> values) {
        Set<String> set = new HashSet<>();
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                set.add(value.toLowerCase(Locale.ROOT));
            }
        }
        return set;
    }

    private <T extends Enum<T>> T enumValue(Class<T> type, String raw, T fallback, String path) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            logger.warn("invalid-config-" + path, "Invalid config value at " + path + ": " + raw + ". Using " + fallback.name() + ".");
            return fallback;
        }
    }
}
