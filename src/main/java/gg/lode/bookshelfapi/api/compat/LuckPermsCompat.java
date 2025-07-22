package gg.lode.bookshelfapi.api.compat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public class LuckPermsCompat {

    public static String getGroupNodeValue(UUID uuid, String startsWith) {
        try {
            if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) return null;

            Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object luckPerms = providerClass.getMethod("get").invoke(null);

            Method getUserManager = luckPerms.getClass().getMethod("getUserManager");
            Object userManager = getUserManager.invoke(luckPerms);

            Method getUser = userManager.getClass().getMethod("getUser", UUID.class);
            Object user = getUser.invoke(userManager, uuid);
            if (user == null) return null;

            String primaryGroup = (String) user.getClass().getMethod("getPrimaryGroup").invoke(user);

            Method getGroupManager = luckPerms.getClass().getMethod("getGroupManager");
            Object groupManager = getGroupManager.invoke(luckPerms);

            Method getGroup = groupManager.getClass().getMethod("getGroup", String.class);
            Object group = getGroup.invoke(groupManager, primaryGroup);
            if (group == null) return null;

            Object nodeSet = group.getClass().getMethod("getNodes").invoke(group);
            for (Object node : (Iterable<?>) nodeSet) {
                String key = (String) node.getClass().getMethod("getKey").invoke(node);
                if (key.startsWith(startsWith)) {
                    String[] split = key.split("\\.");
                    return split.length > 2 ? split[2] : split[1];
                }
            }

        } catch (Exception ignored) {
        }

        return null;
    }

    public static boolean hasPermission(UUID uniqueId, String permission) {
        try {
            if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
                Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
                Object luckPerms = providerClass.getMethod("get").invoke(null);
                Method getUserManager = luckPerms.getClass().getMethod("getUserManager");
                Object userManager = getUserManager.invoke(luckPerms);
                Method getUser = userManager.getClass().getMethod("getUser", UUID.class);
                Object user = getUser.invoke(userManager, uniqueId);
                if (user != null) {
                    Method data = user.getClass().getMethod("getCachedData");
                    Object cachedData = data.invoke(user);
                    Method metaData = cachedData.getClass().getMethod("getPermissionData");
                    Object permissionData = metaData.invoke(cachedData);
                    Method check = permissionData.getClass().getMethod("checkPermission", String.class);
                    Object result = check.invoke(permissionData, permission);
                    Method asBoolean = result.getClass().getMethod("asBoolean");
                    return (boolean) asBoolean.invoke(result);
                }
            }
        } catch (Exception ignored) {
        }
        // Fallback to Bukkit
        return false;
    }

    public static boolean hasPermission(Player player, String permission) {
        try {
            if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
                Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
                Object luckPerms = providerClass.getMethod("get").invoke(null);
                Method getUserManager = luckPerms.getClass().getMethod("getUserManager");
                Object userManager = getUserManager.invoke(luckPerms);
                Method getUser = userManager.getClass().getMethod("getUser", UUID.class);
                Object user = getUser.invoke(userManager, player.getUniqueId());
                if (user != null) {
                    Method data = user.getClass().getMethod("getCachedData");
                    Object cachedData = data.invoke(user);
                    Method metaData = cachedData.getClass().getMethod("getPermissionData");
                    Object permissionData = metaData.invoke(cachedData);
                    Method check = permissionData.getClass().getMethod("checkPermission", String.class);
                    Object result = check.invoke(permissionData, permission);
                    Method asBoolean = result.getClass().getMethod("asBoolean");
                    return (boolean) asBoolean.invoke(result);
                }
            }
        } catch (Exception ignored) {
        }
        // Fallback to Bukkit
        return player.hasPermission(permission);
    }
}
