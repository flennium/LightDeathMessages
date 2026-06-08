package org.flennn.lightdeathmessages.death;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DeathContext {
    private final Player victim;
    private final Player killer;
    private final Location location;
    private final String damageCause;
    private final String mobType;
    private final String projectileType;
    private final String weaponName;
    private final String rawWeaponName;
    private final String explosionType;
    private final boolean playerKill;
    private final boolean mobKill;
    private final boolean projectile;
    private final boolean explosion;
    private final Map<String, String> placeholders;

    public DeathContext(Player victim, Player killer, Location location, String damageCause,
                        String mobType, String projectileType, String weaponName, String rawWeaponName,
                        String explosionType, boolean playerKill, boolean mobKill,
                        boolean projectile, boolean explosion, Map<String, String> placeholders) {
        this.victim = victim;
        this.killer = killer;
        this.location = location;
        this.damageCause = damageCause;
        this.mobType = mobType;
        this.projectileType = projectileType;
        this.weaponName = weaponName;
        this.rawWeaponName = rawWeaponName;
        this.explosionType = explosionType;
        this.playerKill = playerKill;
        this.mobKill = mobKill;
        this.projectile = projectile;
        this.explosion = explosion;
        this.placeholders = Collections.unmodifiableMap(new HashMap<>(placeholders));
    }

    public Player getVictim() {
        return victim;
    }

    public Player getKiller() {
        return killer;
    }

    public Location getLocation() {
        return location;
    }

    public String getDamageCause() {
        return damageCause;
    }

    public String getMobType() {
        return mobType;
    }

    public String getProjectileType() {
        return projectileType;
    }

    public String getWeaponName() {
        return weaponName;
    }

    public String getRawWeaponName() {
        return rawWeaponName;
    }

    public String getExplosionType() {
        return explosionType;
    }

    public boolean isPlayerKill() {
        return playerKill;
    }

    public boolean isMobKill() {
        return mobKill;
    }

    public boolean isProjectile() {
        return projectile;
    }

    public boolean isExplosion() {
        return explosion;
    }

    public Map<String, String> getPlaceholders() {
        return placeholders;
    }
}
