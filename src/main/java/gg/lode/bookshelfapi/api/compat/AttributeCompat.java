package gg.lode.bookshelfapi.api.compat;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;

public class AttributeCompat {

    /**
     * Safely sets a value on an Attribute (e.g., PLAYER_BLOCK_INTERACTION_RANGE) if it exists.
     *
     * @param target The entity to modify
     * @param attributeName The exact field name of the Attribute enum (e.g., "PLAYER_BLOCK_INTERACTION_RANGE")
     * @param value The value to set (int, float, or double)
     */
    public static void setAttribute(LivingEntity target, String attributeName, Number value) {
        try {
            // Get the attribute field reflectively
            Field field = Attribute.class.getField(attributeName);
            field.setAccessible(true);
            Attribute attribute = (Attribute) field.get(null);

            // Get the attribute instance from the entity
            AttributeInstance attributeInstance = target.getAttribute(attribute);
            if (attributeInstance != null) {
                attributeInstance.setBaseValue(value.doubleValue());
            }
        } catch (Throwable t) {
            // Attribute doesn't exist or failed — silently ignore or log
            // t.printStackTrace(); // Uncomment for debugging
        }
    }

    /**
     * Safely retrieves the base value of a Bukkit attribute.
     *
     * @param target The entity to query
     * @param attributeName The exact field name of the Attribute enum
     * @return The attribute value if available, or -1 if not present
     */
    public static double getAttribute(LivingEntity target, String attributeName) {
        try {
            Field field = Attribute.class.getField(attributeName);
            field.setAccessible(true);
            Attribute attribute = (Attribute) field.get(null);

            AttributeInstance attributeInstance = target.getAttribute(attribute);
            if (attributeInstance != null) {
                return attributeInstance.getBaseValue();
            }
        } catch (Throwable t) {
            // t.printStackTrace(); // Optional logging
        }
        return -1;
    }

}