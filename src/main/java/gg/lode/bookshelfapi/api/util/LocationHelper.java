package gg.lode.bookshelfapi.api.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class LocationHelper {

    public static Vector getDirection(Location startLocation, Location targetLocation) {
        return targetLocation.toVector().subtract(startLocation.toVector()).normalize();
    }

    public static Location centerLocation(Location location) {
        location = location.clone();
        location.setX(location.getBlockX() + 0.5);
        location.setZ(location.getBlockZ() + 0.5);
        return location;
    }

    public static Location decenterLocation(Location location) {
        location = location.clone();
        location.setX(location.getBlockX());
        location.setZ(location.getBlockZ());
        return location;
    }

    public static boolean isBlockSimilar(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX() && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ();
    }

}
