package org.flennn.lightdeathmessages.death;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DeathTypeResolver {
    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

    public List<String> resolve(DeathContext context) {
        String key = cacheKey(context);
        List<String> cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        List<String> paths = new ArrayList<>();

        if (context.isPlayerKill()) {
            if (context.isProjectile()) {
                paths.add("player.projectile." + context.getProjectileType());
                paths.add("player.projectile.generic");
                paths.add("player.generic");
            } else if ("magic".equals(context.getDamageCause())) {
                paths.add("player.magic");
                paths.add("player.generic");
            } else if (context.isExplosion()) {
                paths.add("player.explosion");
                paths.add("player.generic");
            } else {
                paths.add("player.melee");
                paths.add("player.generic");
            }
        } else if (context.isMobKill()) {
            paths.add("mob." + context.getMobType());
            paths.add("mob.generic");
        } else if (context.isExplosion()) {
            paths.add("explosion." + context.getExplosionType());
            paths.add("explosion.generic");
        } else {
            paths.add("environment." + environmentKey(context.getDamageCause()));
            paths.add("environment.generic");
        }

        paths.add("fallback.unknown");
        List<String> immutable = Collections.unmodifiableList(paths);
        cache.put(key, immutable);
        return immutable;
    }

    private String cacheKey(DeathContext context) {
        if (context.isPlayerKill()) {
            if (context.isProjectile()) {
                return "player:projectile:" + context.getProjectileType();
            }
            if ("magic".equals(context.getDamageCause())) {
                return "player:magic";
            }
            if (context.isExplosion()) {
                return "player:explosion";
            }
            return "player:melee";
        }
        if (context.isMobKill()) {
            return "mob:" + context.getMobType();
        }
        if (context.isExplosion()) {
            return "explosion:" + context.getExplosionType();
        }
        return "environment:" + environmentKey(context.getDamageCause());
    }

    private String environmentKey(String cause) {
        if (cause == null || cause.isEmpty()) {
            return "generic";
        }
        return switch (cause) {
            case "kill", "generic_kill" -> "kill";
            case "world_border", "outside_border" -> "world_border";
            case "contact", "cactus", "sweet_berry_bush", "stalagmite" -> "cactus";
            case "cramming" -> "cramming";
            case "custom", "generic" -> "custom";
            case "dragon_breath" -> "dragon_breath";
            case "fall" -> "fall";
            case "void", "out_of_world" -> "void";
            case "fire", "fire_tick", "on_fire", "in_fire" -> "fire";
            case "campfire" -> "campfire";
            case "hot_floor" -> "hot_floor";
            case "lava" -> "lava";
            case "drowning" -> "drowning";
            case "dryout" -> "dryout";
            case "suffocation", "in_wall" -> "suffocation";
            case "starvation" -> "starvation";
            case "poison" -> "poison";
            case "magic", "indirect_magic" -> "magic";
            case "wither" -> "wither";
            case "freeze" -> "freezing";
            case "lightning" -> "lightning";
            case "anvil" -> "anvil";
            case "falling_block" -> "falling_block";
            case "fly_into_wall" -> "fly_into_wall";
            case "melting" -> "melting";
            case "sonic_boom" -> "sonic_boom";
            case "suicide" -> "suicide";
            case "thorns" -> "thorns";
            case "invulnerability_reduction" -> "invulnerability_reduction";
            case "entity_attack", "entity_sweep_attack", "player_attack", "mob_attack" -> "entity_attack";
            case "projectile", "arrow", "trident", "fireball", "mob_projectile", "spit", "sonic_boom_projectile" -> "projectile";
            default -> cause;
        };
    }
}
