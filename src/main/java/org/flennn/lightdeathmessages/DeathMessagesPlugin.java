package org.flennn.lightdeathmessages;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.flennn.lightdeathmessages.command.CommandManager;
import org.flennn.lightdeathmessages.config.ConfigManager;
import org.flennn.lightdeathmessages.config.PluginSettings;
import org.flennn.lightdeathmessages.death.BroadcastManager;
import org.flennn.lightdeathmessages.death.DeathContextBuilder;
import org.flennn.lightdeathmessages.death.DeathListener;
import org.flennn.lightdeathmessages.death.DeathTypeResolver;
import org.flennn.lightdeathmessages.format.FormatManager;
import org.flennn.lightdeathmessages.message.MessagesManager;
import org.flennn.lightdeathmessages.placeholder.PlaceholderResolver;
import org.flennn.lightdeathmessages.theme.ThemeManager;
import org.flennn.lightdeathmessages.util.Console;
import org.flennn.lightdeathmessages.util.DebugLogger;
import org.flennn.lightdeathmessages.util.PermissionHelper;
import org.flennn.lightdeathmessages.util.VersionHelper;

public final class DeathMessagesPlugin extends JavaPlugin {
    private DebugLogger debugLogger;
    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private ThemeManager themeManager;
    private FormatManager formatManager;
    private VersionHelper versionHelper;

    @Override
    public void onEnable() {
        debugLogger = new DebugLogger(false, true);
        configManager = new ConfigManager(this, debugLogger);
        messagesManager = new MessagesManager(this, debugLogger);
        themeManager = new ThemeManager(this, messagesManager, debugLogger);
        formatManager = new FormatManager(debugLogger);
        versionHelper = new VersionHelper();

        boolean loaded = reloadAll();
        registerListener();
        registerCommand();

        Console.startup(getDescription().getVersion(), configManager.getSettings(), versionHelper, loaded,
                getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"));
    }

    @Override
    public void onDisable() {
        Console.info("Disabled.");
    }

    public boolean reloadAll() {
        try {
            configManager.load();
            formatManager.configure(configManager.getSettings().getFormattingMode());
            themeManager.ensureThemes();
            boolean messagesLoaded = messagesManager.load();
            debugLogger.configure(configManager.getSettings().isDebug(), configManager.getSettings().isWarnOnce());
            return messagesLoaded;
        } catch (RuntimeException ex) {
            Console.error("Reload failed: " + ex.getMessage());
            return false;
        }
    }

    private void registerListener() {
        getServer().getPluginManager().registerEvents(new DeathListener(
                new DeathContextBuilder(),
                new DeathTypeResolver(),
                messagesManager,
                new PlaceholderResolver(debugLogger),
                formatManager,
                new BroadcastManager(),
                () -> configManager.getSettings(),
                debugLogger
        ), this);
    }

    private void registerCommand() {
        PluginCommand command = getCommand("deathmessages");
        if (command == null) {
            Console.error("Command deathmessages is missing from plugin.yml.");
            return;
        }
        CommandManager manager = new CommandManager(this, themeManager, new PermissionHelper());
        command.setExecutor(manager);
        command.setTabCompleter(manager);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public FormatManager getFormatManager() {
        return formatManager;
    }
}
