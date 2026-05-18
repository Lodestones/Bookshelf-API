package gg.lode.bookshelfapi.api.compat;

import org.bukkit.Bukkit;

public class FoliaCompat {

    private static final boolean IS_FOLIA;

    static {
        // Probe a few stable Folia signals. RegionizedServer existed on
        // 1.21.x Folia but was renamed/relocated by 26.1.x, so we also
        // check the API-level GlobalRegionScheduler class and the
        // Server#getGlobalRegionScheduler() method which Folia guarantees.
        boolean folia = hasClass("io.papermc.paper.threadedregions.RegionizedServer")
                || hasClass("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler")
                || hasServerMethod("getGlobalRegionScheduler");
        IS_FOLIA = folia;
    }

    private static boolean hasClass(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean hasServerMethod(String name) {
        try {
            Bukkit.getServer().getClass().getMethod(name);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }
}
