package org.flennn.lightdeathmessages.placeholder;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.flennn.lightdeathmessages.config.PluginSettings;
import org.flennn.lightdeathmessages.death.DeathContext;
import org.flennn.lightdeathmessages.format.FormatManager;
import org.flennn.lightdeathmessages.message.MessageCache;
import org.flennn.lightdeathmessages.util.DebugLogger;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

public final class PlaceholderResolver {
    private static final Pattern UNRESOLVED_INTERNAL = Pattern.compile("\\{[a-zA-Z0-9_]+}");
    private final DebugLogger logger;
    private Method papiMethod;
    private boolean papiChecked;

    public PlaceholderResolver(DebugLogger logger) {
        this.logger = logger;
    }

    public String resolve(String template, DeathContext context, MessageCache cache, PluginSettings settings) {
        String message = template.replace("{prefix}", cache.getPrefix());
        for (Map.Entry<String, String> entry : context.getPlaceholders().entrySet()) {
            String value = "weapon".equals(entry.getKey()) ? FormatManager.markWeapon(safe(entry.getValue())) : safe(entry.getValue());
            message = message.replace("{" + entry.getKey() + "}", value);
        }
        message = applyPlaceholderApi(message, context.getVictim(), settings);
        return UNRESOLVED_INTERNAL.matcher(message).replaceAll("");
    }

    private String applyPlaceholderApi(String message, Player player, PluginSettings settings) {
        if (!settings.isPlaceholderApi() || !Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return message;
        }
        try {
            Method method = placeholderApiMethod();
            return method == null ? message : (String) method.invoke(null, player, message);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            logger.warn("placeholderapi-error", "PlaceholderAPI failed while resolving a death message: " + ex.getMessage());
            return message;
        }
    }

    private Method placeholderApiMethod() {
        if (papiChecked) {
            return papiMethod;
        }
        papiChecked = true;
        try {
            Class<?> clazz = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            papiMethod = clazz.getMethod("setPlaceholders", OfflinePlayer.class, String.class);
        } catch (ReflectiveOperationException ex) {
            papiMethod = null;
        }
        return papiMethod;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
