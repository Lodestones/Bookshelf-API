package gg.lode.bookshelfapi.api.compat;

import org.bukkit.Bukkit;

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
}
