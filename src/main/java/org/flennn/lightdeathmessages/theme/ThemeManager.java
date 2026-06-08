package org.flennn.lightdeathmessages.theme;

import org.flennn.lightdeathmessages.DeathMessagesPlugin;
import org.flennn.lightdeathmessages.message.MessageCache;
import org.flennn.lightdeathmessages.message.MessagesManager;
import org.flennn.lightdeathmessages.util.DebugLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ThemeManager {
    private final DeathMessagesPlugin plugin;
    private final MessagesManager messagesManager;
    private final DebugLogger logger;

    public ThemeManager(DeathMessagesPlugin plugin, MessagesManager messagesManager, DebugLogger logger) {
        this.plugin = plugin;
        this.messagesManager = messagesManager;
        this.logger = logger;
    }

    public void ensureThemes() {
        File themesDir = new File(plugin.getDataFolder(), "themes");
        if (!themesDir.exists() && !themesDir.mkdirs()) {
            logger.warn("themes-dir", "Could not create themes directory.");
            return;
        }
        for (String theme : bundledThemes()) {
            File file = new File(themesDir, theme + ".yml");
            if (!file.exists()) {
                plugin.saveResource("themes/" + theme + ".yml", false);
                logger.warn("generated-theme-" + theme, "Generated missing theme: " + theme + ".yml.");
            }
        }
    }

    public List<String> listThemes() {
        ensureThemes();
        File[] files = new File(plugin.getDataFolder(), "themes").listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return Collections.emptyList();
        }
        List<String> names = new ArrayList<>();
        for (File file : files) {
            names.add(file.getName().substring(0, file.getName().length() - 4));
        }
        Collections.sort(names);
        return names;
    }

    public boolean exists(String theme) {
        return themeFile(theme).isFile();
    }

    public MessageCache preview(String theme) {
        File file = themeFile(theme);
        if (!file.isFile()) {
            return null;
        }
        try {
            MessageCache cache = messagesManager.loadFile(file);
            validateTheme(cache, theme);
            return cache;
        } catch (RuntimeException ex) {
            logger.warn("theme-preview-" + theme, "Could not preview theme " + theme + ": " + ex.getMessage());
            return null;
        }
    }

    public boolean apply(String theme) {
        File themeFile = themeFile(theme);
        if (!themeFile.isFile()) {
            return false;
        }
        File backup = null;
        File messages = new File(plugin.getDataFolder(), "messages.yml");
        try {
            validateTheme(messagesManager.loadFile(themeFile), theme);
            if (messages.exists()) {
                backup = new File(plugin.getDataFolder(), "messages-" + timestamp() + ".bak.yml");
                Files.copy(messages.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            Files.copy(themeFile.toPath(), messages.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getConfig().set("themes.selected-theme", "messages");
            plugin.saveConfig();
            boolean loaded = plugin.reloadAll();
            if (!loaded && backup != null) {
                Files.copy(backup.toPath(), messages.toPath(), StandardCopyOption.REPLACE_EXISTING);
                messagesManager.load();
            }
            return loaded;
        } catch (RuntimeException | IOException ex) {
            if (backup != null) {
                try {
                    Files.copy(backup.toPath(), messages.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    messagesManager.load();
                } catch (IOException ignored) {
                    // The original failure is more useful to report.
                }
            }
            logger.warn("theme-apply-" + theme, "Could not apply theme " + theme + ": " + ex.getMessage());
            return false;
        }
    }

    private File themeFile(String theme) {
        return new File(new File(plugin.getDataFolder(), "themes"), theme + ".yml");
    }

    private String timestamp() {
        return DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
    }

    private void validateTheme(MessageCache cache, String theme) {
        if (cache.getList("fallback.unknown").isEmpty()) {
            throw new IllegalArgumentException("Theme " + theme + " is missing fallback.unknown.");
        }
        if (cache.getList("player.melee").isEmpty()) {
            throw new IllegalArgumentException("Theme " + theme + " is missing player.melee.");
        }
        if (cache.getString("commands.reload-success", "").isEmpty()) {
            throw new IllegalArgumentException("Theme " + theme + " is missing commands.reload-success.");
        }
    }

    private List<String> bundledThemes() {
        InputStream stream = plugin.getResource("themes/index.yml");
        if (stream == null) {
            return Collections.emptyList();
        }
        List<String> themes = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String theme = cleanThemeName(line);
                if (!theme.isEmpty() && !theme.startsWith("#")) {
                    themes.add(theme);
                }
            }
        } catch (IOException ex) {
            logger.warn("theme-index", "Could not read bundled theme index: " + ex.getMessage());
        }
        return themes;
    }

    private String cleanThemeName(String raw) {
        return raw == null ? "" : raw
                .replace("\uFEFF", "")
                .replaceAll("[^A-Za-z0-9_-]", "")
                .trim();
    }
}
