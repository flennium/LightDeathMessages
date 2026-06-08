package org.flennn.lightdeathmessages.format;

import org.bukkit.ChatColor;
import org.flennn.lightdeathmessages.config.FormattingMode;
import org.flennn.lightdeathmessages.config.PluginSettings;
import org.flennn.lightdeathmessages.util.DebugLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Pattern;

public final class FormatManager {
    public static final String WEAPON_START = "\uE000";
    public static final String WEAPON_END = "\uE001";
    private static final Pattern MINI_TAGS = Pattern.compile("<[^>]+>");
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private final DebugLogger logger;
    private volatile Formatter formatter = this::legacy;

    public FormatManager(DebugLogger logger) {
        this.logger = logger;
    }

    public void configure(FormattingMode mode) {
        if (mode == FormattingMode.PLAIN) {
            formatter = this::plain;
            return;
        }
        if (mode == FormattingMode.MINIMESSAGE) {
            formatter = this::miniMessage;
            return;
        }
        formatter = this::legacy;
    }

    public String format(String message, PluginSettings settings) {
        try {
            return stripWeaponMarkers(formatter.format(message));
        } catch (RuntimeException ex) {
            logger.warn("format-error", "Could not format death message: " + ex.getMessage());
            return plain(message);
        }
    }

    public Component formatDeath(String message, PluginSettings settings, String weaponHoverText) {
        try {
            String formatted = formatter.format(message);
            return componentWithWeaponHover(formatted, weaponHoverText);
        } catch (RuntimeException ex) {
            logger.warn("format-error", "Could not format death message component: " + ex.getMessage());
            return LEGACY.deserialize(plain(message));
        }
    }

    public static String markWeapon(String weapon) {
        return WEAPON_START + weapon + WEAPON_END;
    }

    private String legacy(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private String miniMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', simpleMiniMessage(message));
    }

    private String plain(String message) {
        String legacy = ChatColor.translateAlternateColorCodes('&', MINI_TAGS.matcher(stripWeaponMarkers(message)).replaceAll(""));
        String stripped = ChatColor.stripColor(legacy);
        return stripped == null ? "" : stripped;
    }

    private String simpleMiniMessage(String message) {
        return MINI_TAGS.matcher(message
                .replace("<black>", "&0")
                .replace("<dark_blue>", "&1")
                .replace("<dark_green>", "&2")
                .replace("<dark_aqua>", "&3")
                .replace("<dark_red>", "&4")
                .replace("<dark_purple>", "&5")
                .replace("<gold>", "&6")
                .replace("<gray>", "&7")
                .replace("<dark_gray>", "&8")
                .replace("<blue>", "&9")
                .replace("<green>", "&a")
                .replace("<aqua>", "&b")
                .replace("<red>", "&c")
                .replace("<light_purple>", "&d")
                .replace("<yellow>", "&e")
                .replace("<white>", "&f")
                .replace("<bold>", "&l")
                .replace("<italic>", "&o")
                .replace("<underlined>", "&n")
                .replace("<strikethrough>", "&m")
                .replace("<reset>", "&r")).replaceAll("");
    }

    private Component componentWithWeaponHover(String message, String hoverText) {
        int start = message.indexOf(WEAPON_START);
        if (start < 0) {
            return LEGACY.deserialize(message);
        }

        Component result = Component.empty();
        int index = 0;
        while (start >= 0) {
            int end = message.indexOf(WEAPON_END, start + WEAPON_START.length());
            if (end < 0) {
                result = result.append(LEGACY.deserialize(stripWeaponMarkers(message.substring(index))));
                return result;
            }

            result = result.append(LEGACY.deserialize(message.substring(index, start)));
            String weaponText = message.substring(start + WEAPON_START.length(), end);
            Component weapon = LEGACY.deserialize(weaponText)
                    .hoverEvent(LEGACY.deserialize(ChatColor.translateAlternateColorCodes('&', hoverText)));
            result = result.append(weapon);
            index = end + WEAPON_END.length();
            start = message.indexOf(WEAPON_START, index);
        }

        if (index < message.length()) {
            result = result.append(LEGACY.deserialize(message.substring(index)));
        }
        return result;
    }

    private String stripWeaponMarkers(String message) {
        return message.replace(WEAPON_START, "").replace(WEAPON_END, "");
    }

    private interface Formatter {
        String format(String message);
    }
}
