package org.flennn.lightdeathmessages.config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public final class PluginSettings {
    private final boolean enabled;
    private final boolean debug;
    private final boolean hideVanillaDeathMessage;
    private final BroadcastMode broadcastMode;
    private final double broadcastRadiusSquared;
    private final double broadcastRadius;
    private final double maxBroadcastRadius;
    private final Set<String> enabledWorlds;
    private final Set<String> disabledWorlds;
    private final Map<String, BroadcastMode> perWorldBroadcastModes;
    private final FormattingMode formattingMode;
    private final boolean placeholderApi;
    private final boolean showWeaponName;
    private final boolean showKillerHealth;
    private final boolean showDistance;
    private final boolean showDeathLocation;
    private final boolean showWorldName;
    private final Set<String> disabledCategoryExact;
    private final Set<String> disabledCategoryPrefixes;
    private final String silentPermission;
    private final String bypassPermission;
    private final boolean respectSilent;
    private final boolean respectBypass;
    private final boolean warnMissingKeys;
    private final boolean warnOnce;
    private final String fallbackUnknownMessage;
    private final String selectedTheme;

    public PluginSettings(boolean enabled, boolean debug, boolean hideVanillaDeathMessage,
                          BroadcastMode broadcastMode, double broadcastRadius, double maxBroadcastRadius,
                          Set<String> enabledWorlds, Set<String> disabledWorlds,
                          Map<String, BroadcastMode> perWorldBroadcastModes,
                          FormattingMode formattingMode, boolean placeholderApi,
                          boolean showWeaponName, boolean showKillerHealth, boolean showDistance,
                          boolean showDeathLocation, boolean showWorldName, Set<String> disabledCategories,
                          String silentPermission, String bypassPermission, boolean respectSilent,
                          boolean respectBypass, boolean warnMissingKeys,
                          boolean warnOnce, String fallbackUnknownMessage, String selectedTheme) {
        this.enabled = enabled;
        this.debug = debug;
        this.hideVanillaDeathMessage = hideVanillaDeathMessage;
        this.broadcastMode = broadcastMode;
        this.broadcastRadius = broadcastRadius;
        this.maxBroadcastRadius = maxBroadcastRadius;
        this.broadcastRadiusSquared = broadcastRadius * broadcastRadius;
        this.enabledWorlds = Collections.unmodifiableSet(enabledWorlds);
        this.disabledWorlds = Collections.unmodifiableSet(disabledWorlds);
        this.perWorldBroadcastModes = Collections.unmodifiableMap(perWorldBroadcastModes);
        this.formattingMode = formattingMode;
        this.placeholderApi = placeholderApi;
        this.showWeaponName = showWeaponName;
        this.showKillerHealth = showKillerHealth;
        this.showDistance = showDistance;
        this.showDeathLocation = showDeathLocation;
        this.showWorldName = showWorldName;
        this.disabledCategoryExact = Collections.unmodifiableSet(disabledCategories);
        this.disabledCategoryPrefixes = Collections.unmodifiableSet(prefixes(disabledCategories));
        this.silentPermission = silentPermission;
        this.bypassPermission = bypassPermission;
        this.respectSilent = respectSilent;
        this.respectBypass = respectBypass;
        this.warnMissingKeys = warnMissingKeys;
        this.warnOnce = warnOnce;
        this.fallbackUnknownMessage = fallbackUnknownMessage;
        this.selectedTheme = selectedTheme;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isHideVanillaDeathMessage() {
        return hideVanillaDeathMessage;
    }

    public BroadcastMode getBroadcastMode(String worldName) {
        return perWorldBroadcastModes.getOrDefault(worldName.toLowerCase(), broadcastMode);
    }

    public double getBroadcastRadiusSquared() {
        return broadcastRadiusSquared;
    }

    public double getBroadcastRadius() {
        return broadcastRadius;
    }

    public double getMaxBroadcastRadius() {
        return maxBroadcastRadius;
    }

    public boolean isWorldAllowed(String worldName) {
        String key = worldName.toLowerCase();
        if (disabledWorlds.contains(key)) {
            return false;
        }
        return enabledWorlds.isEmpty() || enabledWorlds.contains(key);
    }

    public FormattingMode getFormattingMode() {
        return formattingMode;
    }

    public boolean isPlaceholderApi() {
        return placeholderApi;
    }

    public boolean isShowWeaponName() {
        return showWeaponName;
    }

    public boolean isShowKillerHealth() {
        return showKillerHealth;
    }

    public boolean isShowDistance() {
        return showDistance;
    }

    public boolean isShowDeathLocation() {
        return showDeathLocation;
    }

    public boolean isShowWorldName() {
        return showWorldName;
    }

    public boolean isCategoryDisabled(String category) {
        if (disabledCategoryExact.isEmpty()) {
            return false;
        }
        if (disabledCategoryExact.contains(category)) {
            return true;
        }
        for (String disabledPrefix : disabledCategoryPrefixes) {
            if (category.startsWith(disabledPrefix)) {
                return true;
            }
        }
        return false;
    }

    public String getSilentPermission() {
        return silentPermission;
    }

    public String getBypassPermission() {
        return bypassPermission;
    }

    public boolean isRespectSilent() {
        return respectSilent;
    }

    public boolean isRespectBypass() {
        return respectBypass;
    }

    public boolean isWarnMissingKeys() {
        return warnMissingKeys;
    }

    public boolean isWarnOnce() {
        return warnOnce;
    }

    public String getFallbackUnknownMessage() {
        return fallbackUnknownMessage;
    }

    public String getSelectedTheme() {
        return selectedTheme;
    }

    public BroadcastMode getDefaultBroadcastMode() {
        return broadcastMode;
    }

    private Set<String> prefixes(Set<String> categories) {
        Set<String> prefixes = new java.util.HashSet<>();
        for (String category : categories) {
            prefixes.add(category + ".");
        }
        return prefixes;
    }
}
