package org.flennn.lightdeathmessages.message;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.flennn.lightdeathmessages.DeathMessagesPlugin;
import org.flennn.lightdeathmessages.config.PluginSettings;
import org.flennn.lightdeathmessages.util.DebugLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class MessagesManager {
    private final DeathMessagesPlugin plugin;
    private final DebugLogger logger;
    private volatile MessageCache cache = MessageCache.empty();

    public MessagesManager(DeathMessagesPlugin plugin, DebugLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public boolean load() {
        String selectedTheme = plugin.getConfigManager().getSettings().getSelectedTheme();
        File file = selectedTheme == null || selectedTheme.equalsIgnoreCase("messages") || selectedTheme.equalsIgnoreCase("messages.yml")
                ? new File(plugin.getDataFolder(), "messages.yml")
                : new File(new File(plugin.getDataFolder(), "themes"), selectedTheme + ".yml");
        if (!file.exists()) {
            if (file.getName().equals("messages.yml")) {
                plugin.saveResource("messages.yml", false);
                logger.warn("generated-messages", "Generated missing messages.yml.");
            } else {
                logger.warn("selected-theme-missing", "Selected theme " + selectedTheme + " does not exist. Falling back to messages.yml.");
                file = new File(plugin.getDataFolder(), "messages.yml");
                if (!file.exists()) {
                    plugin.saveResource("messages.yml", false);
                    logger.warn("generated-messages", "Generated missing messages.yml.");
                }
            }
        }
        try {
            cache = loadFile(file);
            return true;
        } catch (RuntimeException ex) {
            logger.warn("messages-load-failed", "Could not load messages.yml: " + ex.getMessage());
            return false;
        }
    }

    public MessageCache loadFile(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        Map<String, List<String>> lists = new HashMap<>();
        Map<String, String> strings = new HashMap<>();
        flatten(yaml, "", lists, strings);
        String prefix = yaml.getString("prefix", "&8[&cDeath&8]");
        return new MessageCache(prefix, lists, strings);
    }

    public MessageCache getCache() {
        return cache;
    }

    public String getCommand(String path, Map<String, String> placeholders) {
        String raw = cache.getString("commands." + path, "{prefix} &cMissing command message: " + path);
        return replaceBasic(raw, placeholders);
    }

    public List<String> getCommandList(String path, Map<String, String> placeholders) {
        List<String> raw = cache.getList("commands." + path);
        if (raw.isEmpty()) {
            return Collections.singletonList(getCommand(path, placeholders));
        }
        List<String> out = new ArrayList<>();
        for (String line : raw) {
            out.add(replaceBasic(line, placeholders));
        }
        return out;
    }

    public String selectDeathMessage(List<String> candidates, PluginSettings settings) {
        for (String path : candidates) {
            if (settings.isCategoryDisabled(path)) {
                continue;
            }
            List<String> messages = cache.getList(path);
            if (!messages.isEmpty()) {
                return messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
            }
        }
        List<String> fallback = cache.getList("fallback.unknown");
        if (!fallback.isEmpty()) {
            return fallback.get(ThreadLocalRandom.current().nextInt(fallback.size()));
        }
        return settings.getFallbackUnknownMessage();
    }

    private String replaceBasic(String raw, Map<String, String> placeholders) {
        String message = raw.replace("{prefix}", cache.getPrefix());
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    private void flatten(ConfigurationSection section, String path, Map<String, List<String>> lists, Map<String, String> strings) {
        for (String key : section.getKeys(false)) {
            String childPath = path.isEmpty() ? key : path + "." + key;
            Object value = section.get(key);
            if (value instanceof ConfigurationSection child) {
                flatten(child, childPath, lists, strings);
            } else if (value instanceof List<?>) {
                List<String> list = section.getStringList(key);
                if (!list.isEmpty()) {
                    if (isDeathMessagePath(childPath)) {
                        List<String> sanitized = new ArrayList<>();
                        for (String line : list) {
                            sanitized.add(line.replace("{prefix}", "&8[&c\\u2620&8]"));
                        }
                        list = sanitized;
                    }
                    lists.put(childPath, Collections.unmodifiableList(new ArrayList<>(list)));
                }
            } else if (value != null) {
                strings.put(childPath, String.valueOf(value));
            }
        }
    }

    private boolean isDeathMessagePath(String path) {
        return path.startsWith("fallback.")
                || path.startsWith("player.")
                || path.startsWith("mob.")
                || path.startsWith("environment.")
                || path.startsWith("explosion.");
    }
}
