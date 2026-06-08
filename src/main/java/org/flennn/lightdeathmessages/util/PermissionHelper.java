package org.flennn.lightdeathmessages.util;

import org.bukkit.command.CommandSender;

public final class PermissionHelper {
    public boolean has(CommandSender sender, String permission) {
        return sender.hasPermission(permission) || sender.hasPermission("deathmessages.admin");
    }
}
