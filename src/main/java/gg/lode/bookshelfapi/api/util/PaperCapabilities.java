package gg.lode.bookshelfapi.api.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Locale;

public final class PaperCapabilities {

    private static final MethodHandle SET_ITEM_MODEL; // ItemMeta#setItemModel(NamespacedKey) in 1.21.4+
    private static final Method SET_MAX_STACK_SIZE;   // ItemMeta#setMaxStackSize(int)
    private static final Method SET_HIDE_TOOLTIP;     // ItemMeta#setHideTooltip(boolean)
    private static final Method SET_GLINT_OVERRIDE;   // ItemMeta#setEnchantmentGlintOverride(boolean)
    private static final Method SET_TOOLTIP_STYLE;    // ItemMeta#setTooltipStyle(NamespacedKey)
    private static final Method SET_GLIDER;           // ItemMeta#setGlider(boolean)
    private static final Method SET_RARITY;           // ItemMeta#setRarity(Enum)

    static {
        MethodHandle handle = null;
        Method maxStack = null;
        Method hideTooltip = null;
        Method glintOverride = null;
        Method tooltipStyle = null;
        Method glider = null;
        Method rarity = null;
        try {
            handle = MethodHandles.lookup().findVirtual(
                    ItemMeta.class,
                    "setItemModel",
                    MethodType.methodType(void.class, NamespacedKey.class)
            );
        } catch (Throwable ignored) {
            // Older Paper: method does not exist
        }
        // Use reflective discovery for the rest to avoid hard-coding parameter types
        for (Method m : ItemMeta.class.getMethods()) {
            String name = m.getName();
            Class<?>[] params = m.getParameterTypes();
            if ("setMaxStackSize".equals(name) && params.length == 1 && params[0] == int.class) {
                maxStack = m;
            } else if ("setHideTooltip".equals(name) && params.length == 1 && params[0] == boolean.class) {
                hideTooltip = m;
            } else if ("setEnchantmentGlintOverride".equals(name) && params.length == 1 && params[0] == boolean.class) {
                glintOverride = m;
            } else if ("setTooltipStyle".equals(name) && params.length == 1 && params[0].getName().equals("org.bukkit.NamespacedKey")) {
                tooltipStyle = m; // expect NamespacedKey
            } else if ("setGlider".equals(name) && params.length == 1 && params[0] == boolean.class) {
                glider = m;
            } else if ("setRarity".equals(name) && params.length == 1 && params[0].isEnum()) {
                rarity = m;
            }
        }
        SET_ITEM_MODEL = handle;
        SET_MAX_STACK_SIZE = maxStack;
        SET_HIDE_TOOLTIP = hideTooltip;
        SET_GLINT_OVERRIDE = glintOverride;
        SET_TOOLTIP_STYLE = tooltipStyle;
        SET_GLIDER = glider;
        SET_RARITY = rarity;
    }

    private PaperCapabilities() {
    }

    public static boolean supportsItemModel() {
        return SET_ITEM_MODEL != null;
    }

    public static void setItemModelIfSupported(ItemMeta meta, NamespacedKey key) {
        if (SET_ITEM_MODEL == null || meta == null || key == null) return;
        try {
            SET_ITEM_MODEL.invoke(meta, key);
        } catch (Throwable ignored) {
            // Defensive: signature mismatch or runtime failure → no-op
        }
    }

    public static void setMaxStackSizeIfSupported(ItemMeta meta, Integer size) {
        if (meta == null || size == null || SET_MAX_STACK_SIZE == null) return;
        try {
            SET_MAX_STACK_SIZE.invoke(meta, size.intValue());
        } catch (Throwable ignored) {
        }
    }

    public static void setHideTooltipIfSupported(ItemMeta meta, Boolean hide) {
        if (meta == null || hide == null || SET_HIDE_TOOLTIP == null) return;
        try {
            SET_HIDE_TOOLTIP.invoke(meta, hide.booleanValue());
        } catch (Throwable ignored) {
        }
    }

    public static void setEnchantmentGlintOverrideIfSupported(ItemMeta meta, Boolean override) {
        if (meta == null || override == null || SET_GLINT_OVERRIDE == null) return;
        try {
            SET_GLINT_OVERRIDE.invoke(meta, override.booleanValue());
        } catch (Throwable ignored) {
        }
    }

    public static void setGliderIfSupported(ItemMeta meta, Boolean glider) {
        if (meta == null || glider == null || SET_GLIDER == null) return;
        try {
            SET_GLIDER.invoke(meta, glider.booleanValue());
        } catch (Throwable ignored) {
        }
    }

    public static void setTooltipStyleIfSupported(ItemMeta meta, NamespacedKey styleKey) {
        if (meta == null || styleKey == null || SET_TOOLTIP_STYLE == null) return;
        try {
            SET_TOOLTIP_STYLE.invoke(meta, styleKey);
        } catch (Throwable ignored) {
        }
    }

    public static void setRarityIfSupported(ItemMeta meta, Object itemRarityOrName) {
        if (meta == null || itemRarityOrName == null || SET_RARITY == null) return;
        try {
            Class<?> enumType = SET_RARITY.getParameterTypes()[0];
            Object enumValue = null;
            if (enumType.isInstance(itemRarityOrName)) {
                enumValue = itemRarityOrName;
            } else if (itemRarityOrName instanceof String s) {
                enumValue = enumValueIgnoreCase(enumType, s);
            }
            if (enumValue != null) {
                SET_RARITY.invoke(meta, enumValue);
            }
        } catch (Throwable ignored) {
        }
    }

    private static Object enumValueIgnoreCase(Class<?> enumType, String name) {
        if (!enumType.isEnum()) return null;
        Object[] constants = enumType.getEnumConstants();
        String needle = name.toUpperCase(Locale.ROOT);
        for (Object c : constants) {
            if (((Enum<?>) c).name().equalsIgnoreCase(needle)) return c;
        }
        return null;
    }
}


