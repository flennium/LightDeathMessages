package org.flennn.lightdeathmessages.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.flennn.lightdeathmessages.DeathMessagesPlugin;
import org.flennn.lightdeathmessages.config.PluginSettings;
import org.flennn.lightdeathmessages.message.MessageCache;
import org.flennn.lightdeathmessages.theme.ThemeManager;
import org.flennn.lightdeathmessages.util.PermissionHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CommandManager implements CommandExecutor, TabCompleter {
    private final DeathMessagesPlugin plugin;
    private final ThemeManager themeManager;
    private final PermissionHelper permissions;

    public CommandManager(DeathMessagesPlugin plugin, ThemeManager themeManager, PermissionHelper permissions) {
        this.plugin = plugin;
        this.themeManager = themeManager;
        this.permissions = permissions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            return help(sender);
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "reload" -> reload(sender);
            case "version" -> version(sender);
            case "test" -> test(sender, args);
            case "theme" -> theme(sender, args);
            default -> {
                send(sender, "unknown-command", Collections.emptyMap());
                yield true;
            }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            if (permissions.has(sender, "deathmessages.admin")) {
                options.add("help");
                options.add("version");
            }
            if (permissions.has(sender, "deathmessages.reload")) {
                options.add("reload");
            }
            if (permissions.has(sender, "deathmessages.test")) {
                options.add("test");
            }
            if (permissions.has(sender, "deathmessages.theme")
                    || permissions.has(sender, "deathmessages.theme.preview")
                    || permissions.has(sender, "deathmessages.theme.apply")) {
                options.add("theme");
            }
            return filter(options, args[0]);
        }
        if (args.length == 2 && "theme".equalsIgnoreCase(args[0])) {
            List<String> options = new ArrayList<>();
            if (permissions.has(sender, "deathmessages.theme")) {
                options.add("list");
            }
            if (permissions.has(sender, "deathmessages.theme.preview")) {
                options.add("preview");
            }
            if (permissions.has(sender, "deathmessages.theme.apply")) {
                options.add("apply");
            }
            return filter(options, args[1]);
        }
        if (args.length == 3 && "theme".equalsIgnoreCase(args[0])) {
            if (("preview".equalsIgnoreCase(args[1]) && !permissions.has(sender, "deathmessages.theme.preview"))
                    || ("apply".equalsIgnoreCase(args[1]) && !permissions.has(sender, "deathmessages.theme.apply"))) {
                return Collections.emptyList();
            }
            return filter(themeManager.listThemes(), args[2]);
        }
        if (args.length == 2 && "test".equalsIgnoreCase(args[0])) {
            if (!permissions.has(sender, "deathmessages.test")) {
                return Collections.emptyList();
            }
            return filter(testTypes(), args[1]);
        }
        if (args.length == 3 && "test".equalsIgnoreCase(args[0])) {
            if (!permissions.has(sender, "deathmessages.test")) {
                return Collections.emptyList();
            }
            return filter(Arrays.asList("self", "global", "world", "radius", "disabled"), args[2]);
        }
        return Collections.emptyList();
    }

    private boolean help(CommandSender sender) {
        if (!permissions.has(sender, "deathmessages.admin")) {
            send(sender, "no-permission", Collections.emptyMap());
            return true;
        }
        for (String line : plugin.getMessagesManager().getCommandList("help", placeholders())) {
            sender.sendMessage(plugin.getFormatManager().format(line, plugin.getConfigManager().getSettings()));
        }
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (!permissions.has(sender, "deathmessages.reload")) {
            send(sender, "no-permission", Collections.emptyMap());
            return true;
        }
        if (plugin.reloadAll()) {
            send(sender, "reload-success", Collections.emptyMap());
        } else {
            send(sender, "reload-failed", Collections.emptyMap());
        }
        return true;
    }

    private boolean version(CommandSender sender) {
        if (!permissions.has(sender, "deathmessages.admin")) {
            send(sender, "no-permission", Collections.emptyMap());
            return true;
        }
        Map<String, String> values = placeholders();
        values.put("version", plugin.getDescription().getVersion());
        send(sender, "version", values);
        return true;
    }

    private boolean test(CommandSender sender, String[] args) {
        if (!permissions.has(sender, "deathmessages.test")) {
            send(sender, "no-permission", Collections.emptyMap());
            return true;
        }
        String type = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "unknown";
        TestBroadcast broadcast = TestBroadcast.from(args.length >= 3 ? args[2] : "self");
        MessageCache cache = plugin.getMessagesManager().getCache();
        if ("all".equals(type)) {
            for (String path : allTestPaths(cache)) {
                sendPreview(sender, cache, path, broadcast, true);
            }
        } else {
            sendPreview(sender, cache, testPath(type), broadcast, true);
        }
        send(sender, "test-sent", Collections.emptyMap());
        return true;
    }

    private void sendPreview(CommandSender sender, MessageCache cache, String path, TestBroadcast broadcast, boolean allLines) {
        List<String> messages = cache.getList(path);
        if (messages.isEmpty()) {
            deliverTestMessage(sender, previewMessage(plugin.getConfigManager().getSettings().getFallbackUnknownMessage(), cache), broadcast);
            return;
        }
        if (!allLines) {
            deliverTestMessage(sender, previewMessage(messages.get(0), cache), broadcast);
            return;
        }
        for (String raw : messages) {
            deliverTestMessage(sender, previewMessage(raw, cache), broadcast);
        }
    }

    private void deliverTestMessage(CommandSender sender, String message, TestBroadcast broadcast) {
        if (broadcast == TestBroadcast.DISABLED) {
            return;
        }
        if (broadcast == TestBroadcast.SELF || !(sender instanceof Player player)) {
            sender.sendMessage(message);
            return;
        }
        if (broadcast == TestBroadcast.GLOBAL) {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                online.sendMessage(message);
            }
            return;
        }
        if (broadcast == TestBroadcast.WORLD) {
            for (Player online : player.getWorld().getPlayers()) {
                online.sendMessage(message);
            }
            return;
        }
        double radiusSquared = plugin.getConfigManager().getSettings().getBroadcastRadiusSquared();
        for (Player online : player.getWorld().getPlayers()) {
            if (online.getLocation().distanceSquared(player.getLocation()) <= radiusSquared) {
                online.sendMessage(message);
            }
        }
    }

    private String testPath(String type) {
        return switch (type) {
            case "melee" -> "player.melee";
            case "arrow" -> "player.projectile.arrow";
            case "trident" -> "player.projectile.trident";
            case "mob" -> "mob.generic";
            case "fall" -> "environment.fall";
            case "void" -> "environment.void";
            case "lava" -> "environment.lava";
            case "fire" -> "environment.fire";
            case "drowning" -> "environment.drowning";
            case "poison" -> "environment.poison";
            case "magic" -> "environment.magic";
            case "tnt" -> "explosion.tnt";
            case "crystal" -> "explosion.crystal";
            case "bed" -> "explosion.bed";
            case "anchor" -> "explosion.respawn_anchor";
            default -> "fallback.unknown";
        };
    }

    private List<String> testTypes() {
        return new ArrayList<>(Arrays.asList(
                "all", "melee", "arrow", "trident", "mob", "fall", "void", "lava",
                "fire", "drowning", "poison", "magic", "tnt", "crystal", "bed", "anchor", "unknown"
        ));
    }

    private List<String> allTestPaths(MessageCache cache) {
        Set<String> paths = new LinkedHashSet<>(canonicalDeathCategories());
        paths.addAll(cache.getDeathCategories());
        return new ArrayList<>(paths);
    }

    private List<String> canonicalDeathCategories() {
        return Arrays.asList(
                "fallback.unknown",
                "player.generic",
                "player.melee",
                "player.magic",
                "player.explosion",
                "player.projectile.generic",
                "player.projectile.arrow",
                "player.projectile.trident",
                "player.projectile.fireball",
                "player.projectile.snowball",
                "mob.zombie",
                "mob.skeleton",
                "mob.creeper",
                "mob.wolf",
                "mob.warden",
                "mob.generic",
                "environment.generic",
                "environment.kill",
                "environment.world_border",
                "environment.contact",
                "environment.cactus",
                "environment.cramming",
                "environment.custom",
                "environment.dragon_breath",
                "environment.drowning",
                "environment.dryout",
                "environment.entity_attack",
                "environment.fall",
                "environment.fire",
                "environment.lava",
                "environment.suffocation",
                "environment.starvation",
                "environment.poison",
                "environment.projectile",
                "environment.magic",
                "environment.freezing",
                "environment.lightning",
                "environment.anvil",
                "environment.falling_block",
                "environment.fly_into_wall",
                "environment.campfire",
                "environment.hot_floor",
                "environment.melting",
                "environment.sonic_boom",
                "environment.suicide",
                "environment.thorns",
                "environment.void",
                "environment.wither",
                "environment.block_explosion",
                "environment.entity_explosion",
                "environment.bad_respawn_point",
                "environment.outside_border",
                "environment.generic_kill",
                "environment.invulnerability_reduction",
                "explosion.generic",
                "explosion.block",
                "explosion.entity",
                "explosion.tnt",
                "explosion.crystal",
                "explosion.bed",
                "explosion.respawn_anchor"
        );
    }

    private boolean theme(CommandSender sender, String[] args) {
        if (args.length < 2 || "list".equalsIgnoreCase(args[1])) {
            if (!permissions.has(sender, "deathmessages.theme")) {
                send(sender, "no-permission", Collections.emptyMap());
                return true;
            }
            Map<String, String> values = placeholders();
            values.put("themes", String.join(", ", themeManager.listThemes()));
            send(sender, "theme-list", values);
            return true;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        String theme = args.length >= 3 ? args[2].toLowerCase(Locale.ROOT) : "";
        if ("preview".equals(action)) {
            return previewTheme(sender, theme);
        }
        if ("apply".equals(action)) {
            return applyTheme(sender, theme);
        }
        send(sender, "unknown-command", Collections.emptyMap());
        return true;
    }

    private boolean previewTheme(CommandSender sender, String theme) {
        if (!permissions.has(sender, "deathmessages.theme.preview")) {
            send(sender, "no-permission", Collections.emptyMap());
            return true;
        }
        MessageCache cache = themeManager.preview(theme);
        if (cache == null) {
            Map<String, String> values = placeholders();
            values.put("theme", theme);
            send(sender, "theme-not-found", values);
            return true;
        }
        Map<String, String> values = placeholders();
        values.put("theme", theme);
        send(sender, "theme-preview-header", values);
        List<String> sample = cache.getList("player.melee");
        String raw = sample.isEmpty() ? "{prefix} &c{victim} &7died." : sample.get(0);
        sender.sendMessage(previewMessage(raw, cache));
        return true;
    }

    private boolean applyTheme(CommandSender sender, String theme) {
        if (!permissions.has(sender, "deathmessages.theme.apply")) {
            send(sender, "no-permission", Collections.emptyMap());
            return true;
        }
        Map<String, String> values = placeholders();
        values.put("theme", theme);
        if (!themeManager.exists(theme)) {
            send(sender, "theme-not-found", values);
            return true;
        }
        if (themeManager.apply(theme)) {
            send(sender, "theme-applied", values);
        } else {
            send(sender, "theme-apply-failed", values);
        }
        return true;
    }

    private String previewMessage(String raw, MessageCache cache) {
        Map<String, String> values = placeholders();
        String message = raw.replace("{prefix}", cache.getPrefix());
        for (Map.Entry<String, String> entry : values.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return plugin.getFormatManager().format(message, plugin.getConfigManager().getSettings());
    }

    private void send(CommandSender sender, String path, Map<String, String> extra) {
        Map<String, String> values = placeholders();
        values.putAll(extra);
        String raw = plugin.getMessagesManager().getCommand(path, values);
        sender.sendMessage(plugin.getFormatManager().format(raw, plugin.getConfigManager().getSettings()));
    }

    private Map<String, String> placeholders() {
        Map<String, String> values = new HashMap<>();
        values.put("victim", "flennium");
        values.put("victim_displayname", "flennium");
        values.put("victim_uuid", "fa5a0664-3af9-4d17-86be-1d59e84c445d");
        values.put("killer", "x222");
        values.put("killer_displayname", "x222");
        values.put("killer_uuid", "caf89054-e260-4b4e-aa14-e1e7016ece4a");
        values.put("mob", "Zombie");
        values.put("weapon", "Diamond Sword");
        values.put("weapon_raw", "DIAMOND_SWORD");
        values.put("weapon_clean", "Diamond Sword");
        values.put("world", "world");
        values.put("x", "0");
        values.put("y", "64");
        values.put("z", "0");
        values.put("killer_health", "20.0");
        values.put("distance", "12.5");
        values.put("death_cause", "Entity Attack");
        values.put("projectile", "Arrow");
        values.put("server", "Hypixel");
        values.put("version", plugin.getDescription().getVersion());
        values.put("themes", "");
        values.put("theme", "");
        return values;
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                matches.add(option);
            }
        }
        return matches;
    }

    private enum TestBroadcast {
        SELF,
        GLOBAL,
        WORLD,
        RADIUS,
        DISABLED;

        private static TestBroadcast from(String raw) {
            if (raw == null) {
                return SELF;
            }
            try {
                return TestBroadcast.valueOf(raw.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                return SELF;
            }
        }
    }
}
