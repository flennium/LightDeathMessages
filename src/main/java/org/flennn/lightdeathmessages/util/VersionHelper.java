package org.flennn.lightdeathmessages.util;

import org.bukkit.Bukkit;

public final class VersionHelper {
    public boolean isPaper() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }


    public String serverVersion() {
        return Bukkit.getVersion();
    }
}
