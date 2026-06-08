package org.flennn.lightdeathmessages.death;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.flennn.lightdeathmessages.config.PluginSettings;
import org.flennn.lightdeathmessages.format.FormatManager;
import org.flennn.lightdeathmessages.message.MessagesManager;
import org.flennn.lightdeathmessages.placeholder.PlaceholderResolver;
import org.flennn.lightdeathmessages.util.DebugLogger;
import net.kyori.adventure.text.Component;

import java.util.List;

public final class DeathListener implements Listener {
    private final DeathContextBuilder contextBuilder;
    private final DeathTypeResolver typeResolver;
    private final MessagesManager messagesManager;
    private final PlaceholderResolver placeholderResolver;
    private final FormatManager formatManager;
    private final BroadcastManager broadcastManager;
    private final SettingsProvider settingsProvider;
    private final DebugLogger logger;

    public DeathListener(DeathContextBuilder contextBuilder, DeathTypeResolver typeResolver,
                         MessagesManager messagesManager, PlaceholderResolver placeholderResolver,
                         FormatManager formatManager, BroadcastManager broadcastManager,
                         SettingsProvider settingsProvider, DebugLogger logger) {
        this.contextBuilder = contextBuilder;
        this.typeResolver = typeResolver;
        this.messagesManager = messagesManager;
        this.placeholderResolver = placeholderResolver;
        this.formatManager = formatManager;
        this.broadcastManager = broadcastManager;
        this.settingsProvider = settingsProvider;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        PluginSettings settings = settingsProvider.getSettings();
        if (settings.isHideVanillaDeathMessage()) {
            event.setDeathMessage(null);
        }
        if (!settings.isEnabled()) {
            return;
        }
        if (!settings.isWorldAllowed(event.getEntity().getWorld().getName())) {
            return;
        }
        if (settings.isRespectSilent() && event.getEntity().hasPermission(settings.getSilentPermission())) {
            return;
        }
        if (settings.isRespectBypass() && event.getEntity().hasPermission(settings.getBypassPermission())) {
            return;
        }

        try {
            DeathContext context = contextBuilder.build(event, settings);
            List<String> paths = typeResolver.resolve(context);
            String raw = messagesManager.selectDeathMessage(paths, settings);
            String resolved = placeholderResolver.resolve(raw, context, messagesManager.getCache(), settings);
            Component formatted = formatManager.formatDeath(resolved, settings, weaponHover(context));
            broadcastManager.broadcast(context, formatted, settings);
            logger.debug("Death message paths: " + paths + ", cause=" + context.getDamageCause());
        } catch (RuntimeException ex) {
            logger.warn("death-event-error", "Death message handling failed safely: " + ex.getMessage());
        }
    }

    private String weaponHover(DeathContext context) {
        return "&8&m----------------\n"
                + "&cWeapon\n"
                + "&7Name: &f" + context.getWeaponName() + "\n"
                + "&7Type: &f" + context.getRawWeaponName() + "\n"
                + "&8&m----------------";
    }

    public interface SettingsProvider {
        PluginSettings getSettings();
    }
}
