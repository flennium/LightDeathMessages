package org.flennn.lightdeathmessages.death;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.flennn.lightdeathmessages.config.BroadcastMode;
import org.flennn.lightdeathmessages.config.PluginSettings;
import net.kyori.adventure.text.Component;

public final class BroadcastManager {
    public void broadcast(DeathContext context, String message, PluginSettings settings) {
        BroadcastMode mode = settings.getBroadcastMode(context.getVictim().getWorld().getName());
        if (mode == BroadcastMode.DISABLED || message == null || message.isEmpty()) {
            return;
        }

        if (mode == BroadcastMode.GLOBAL) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message);
            }
            return;
        }

        World world = context.getVictim().getWorld();
        if (mode == BroadcastMode.WORLD) {
            for (Player player : world.getPlayers()) {
                player.sendMessage(message);
            }
            return;
        }

        double radiusSquared = settings.getBroadcastRadiusSquared();
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distanceSquared(context.getLocation()) <= radiusSquared) {
                player.sendMessage(message);
            }
        }
    }

    public void broadcast(DeathContext context, Component message, PluginSettings settings) {
        BroadcastMode mode = settings.getBroadcastMode(context.getVictim().getWorld().getName());
        if (mode == BroadcastMode.DISABLED) {
            return;
        }

        if (mode == BroadcastMode.GLOBAL) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message);
            }
            return;
        }

        World world = context.getVictim().getWorld();
        if (mode == BroadcastMode.WORLD) {
            for (Player player : world.getPlayers()) {
                player.sendMessage(message);
            }
            return;
        }

        double radiusSquared = settings.getBroadcastRadiusSquared();
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distanceSquared(context.getLocation()) <= radiusSquared) {
                player.sendMessage(message);
            }
        }
    }
}
