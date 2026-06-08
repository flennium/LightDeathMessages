package org.flennn.lightdeathmessages.death;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.flennn.lightdeathmessages.config.PluginSettings;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class DeathContextBuilder {
    public DeathContext build(PlayerDeathEvent event, PluginSettings settings) {
        Player victim = event.getEntity();
        EntityDamageEvent damage = victim.getLastDamageCause();
        EntityDamageEvent.DamageCause cause = damage == null ? null : damage.getCause();
        String causeName = damageTypeKey(damage);
        if (causeName == null || causeName.isEmpty()) {
            causeName = cause == null ? "unknown" : normalize(cause.name());
        }
        Player killer = victim.getKiller();
        Entity damager = damage instanceof EntityDamageByEntityEvent byEntity ? byEntity.getDamager() : null;

        boolean projectile = false;
        String projectileType = "none";
        boolean mobKill = false;
        String mobType = "none";
        boolean explosion = isExplosionCause(causeName);
        String explosionType = explosionType(damager, causeName);

        if (damager instanceof Projectile projectileEntity) {
            projectile = true;
            projectileType = normalize(projectileEntity.getType().name());
            ProjectileSource shooter = projectileEntity.getShooter();
            if (shooter instanceof Player shooterPlayer) {
                killer = shooterPlayer;
            } else if (shooter instanceof LivingEntity living) {
                mobKill = true;
                mobType = normalize(living.getType().name());
            }
        } else if (damager instanceof Player player) {
            killer = player;
        } else if (damager instanceof LivingEntity living) {
            mobKill = true;
            mobType = normalize(living.getType().name());
        }

        if (damager instanceof FallingBlock fallingBlock) {
            String material = fallingBlock.getBlockData().getMaterial().name();
            if (material.endsWith("ANVIL")) {
                causeName = "anvil";
            } else {
                causeName = "falling_block";
            }
        }

        if (damager != null && isExplosionEntity(damager)) {
            explosion = true;
            explosionType = explosionType(damager, causeName);
        }

        ItemStack weapon = killer == null ? null : killer.getInventory().getItemInMainHand();
        String rawWeapon = rawWeaponName(weapon);
        String cleanWeapon = settings.isShowWeaponName() ? cleanWeaponName(weapon) : "None";
        Location location = victim.getLocation();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("victim", victim.getName());
        placeholders.put("victim_displayname", safe(victim.getDisplayName()));
        placeholders.put("victim_uuid", victim.getUniqueId().toString());
        placeholders.put("killer", killer == null ? "Unknown" : killer.getName());
        placeholders.put("killer_displayname", killer == null ? "Unknown" : safe(killer.getDisplayName()));
        placeholders.put("killer_uuid", killer == null ? "" : killer.getUniqueId().toString());
        placeholders.put("mob", title(mobType));
        placeholders.put("weapon", cleanWeapon);
        placeholders.put("weapon_raw", rawWeapon);
        placeholders.put("weapon_clean", cleanWeapon);
        placeholders.put("world", settings.isShowWorldName() ? location.getWorld().getName() : "");
        placeholders.put("x", settings.isShowDeathLocation() ? String.valueOf(location.getBlockX()) : "");
        placeholders.put("y", settings.isShowDeathLocation() ? String.valueOf(location.getBlockY()) : "");
        placeholders.put("z", settings.isShowDeathLocation() ? String.valueOf(location.getBlockZ()) : "");
        placeholders.put("killer_health", settings.isShowKillerHealth() && killer != null ? String.format(Locale.US, "%.1f", killer.getHealth()) : "");
        placeholders.put("distance", settings.isShowDistance() && killer != null && killer.getWorld().equals(victim.getWorld()) ? String.format(Locale.US, "%.1f", killer.getLocation().distance(location)) : "");
        placeholders.put("death_cause", title(causeName));
        placeholders.put("projectile", title(projectileType));
        placeholders.put("server", Bukkit.getServer().getName());

        return new DeathContext(victim, killer, location, causeName, mobType, projectileType, cleanWeapon,
                rawWeapon, explosionType, killer != null, mobKill, projectile, explosion, placeholders);
    }

    private boolean isExplosionCause(String causeName) {
        return "entity_explosion".equals(causeName)
                || "block_explosion".equals(causeName)
                || "bad_respawn_point".equals(causeName)
                || causeName.contains("explosion");
    }

    private boolean isExplosionEntity(Entity entity) {
        String type = entity.getType().name();
        return "PRIMED_TNT".equals(type) || "TNT".equals(type) || "ENDER_CRYSTAL".equals(type) || "END_CRYSTAL".equals(type);
    }

    private String explosionType(Entity damager, String causeName) {
        if (damager != null) {
            String type = damager.getType().name();
            if ("PRIMED_TNT".equals(type) || "TNT".equals(type)) {
                return "tnt";
            }
            if ("ENDER_CRYSTAL".equals(type) || "END_CRYSTAL".equals(type)) {
                return "crystal";
            }
        }
        if ("bad_respawn_point".equals(causeName) || causeName.contains("respawn_anchor")) {
            return "respawn_anchor";
        }
        if (causeName.contains("bed")) {
            return "bed";
        }
        if ("block_explosion".equals(causeName)) {
            return "block";
        }
        if ("entity_explosion".equals(causeName)) {
            return "entity";
        }
        return "generic";
    }

    private String damageTypeKey(EntityDamageEvent damage) {
        if (damage == null) {
            return null;
        }
        try {
            Object source = damage.getClass().getMethod("getDamageSource").invoke(damage);
            Object type = source.getClass().getMethod("getDamageType").invoke(source);
            Object key = type.getClass().getMethod("getKey").invoke(type);
            String value = String.valueOf(key);
            int separator = value.indexOf(':');
            return normalize(separator >= 0 ? value.substring(separator + 1) : value);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private String rawWeaponName(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return "None";
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }
        return item.getType().name();
    }

    private String cleanWeaponName(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return "None";
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            String displayName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName()));
            return displayName == null || displayName.trim().isEmpty() ? title(item.getType().name()) : displayName;
        }
        return title(item.getType().name());
    }

    private String normalize(String value) {
        return value == null ? "unknown" : value.toLowerCase(Locale.ROOT);
    }

    private String title(String value) {
        if (value == null || value.isEmpty() || "none".equals(value)) {
            return "None";
        }
        String[] parts = value.toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.length() == 0 ? "Unknown" : builder.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
