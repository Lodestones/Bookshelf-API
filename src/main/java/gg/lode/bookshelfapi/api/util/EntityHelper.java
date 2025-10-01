package gg.lode.bookshelfapi.api.util;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * A utility class for entity related operations.
 * This class contains methods for giving/dropping items, killing entities gracefully,
 * resetting player defaults, checking if a player is on the ground,
 * teleporting entities, reducing damage by resistance,
 * and forcing damage events.
 *
 * @author Robsutar
 */
@SuppressWarnings("UnstableApiUsage")
public class EntityHelper {
    public static void giveOrDrop(HumanEntity p, ItemStack... items) {
        giveOrDrop(p.getInventory(), p.getLocation(), items);
    }

    public static void giveOrDrop(Inventory inv, Location dropLoc, ItemStack... items) {
        for (var stack : inv.addItem(items).values()) {
            var i = dropLoc.getWorld().spawn(dropLoc, Item.class);
            i.setItemStack(stack);
        }
    }

    public static void killGracefully(LivingEntity le) {
        if (!le.isDead() && le.getHealth() > 0) {
            if (le.isInvulnerable() || le instanceof Player)
                le.setHealth(0);
            else
                le.damage(999999999);
        }
    }

    public static void resetDefaults(Player p) {
        p.getInventory().clear();
        p.setExperienceLevelAndProgress(0);

        p.clearActivePotionEffects();
        p.setHealth(
                Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue()
        );
        p.setFoodLevel(20);
    }

    @SuppressWarnings("deprecation")
    public static boolean isOnGround(Player p) {
        if (p.isOnGround()) {
            var pos = p.getLocation();
            var ray = pos.getWorld().rayTraceBlocks(pos, new Vector(0.0, -1.0, 0.0), 0.05, FluidCollisionMode.NEVER);
            return ray != null;
        } else {
            return false;
        }
    }

    public static void teleportEvenWithPassengers(Entity entity, Location pos) {
        if (pos.getWorld() != entity.getWorld())
            throw new IllegalArgumentException("Entity and pos needs to be in the same world");
        try {
            var getHandle = entity.getClass().getMethod("getHandle");
            var handle = getHandle.invoke(entity);
            var craftEntityClass = handle.getClass();

            var method = craftEntityClass.getMethod("a", double.class, double.class, double.class, float.class, float.class);
            method.invoke(handle, pos.getX(), pos.getY(), pos.getZ(), pos.getYaw(), pos.getPitch());
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static double reduceDamageByResistance(LivingEntity damaged, double damage) {

        final double armorReduce;
        var armorAtt = damaged.getAttribute(Attribute.GENERIC_ARMOR);
        if (armorAtt != null) {
            var armorAttValue = armorAtt.getValue();
            armorReduce = damage - (damage * (1 - (armorAttValue / (armorAttValue + 20))));
        } else {
            armorReduce = 0.0;
        }

        final double armorToughnessReduce;
        var armorToughnessAtt = damaged.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS);
        if (armorToughnessAtt != null) {
            var armorToughnessAttValue = armorToughnessAtt.getValue();
            armorToughnessReduce = damage - (damage * (1 - (armorToughnessAttValue / (armorToughnessAttValue + 50))));
        } else {
            armorToughnessReduce = 0;
        }

        final double resistanceReduce;
        var resistanceEffect = damaged.getPotionEffect(Objects.requireNonNull(PotionEffectType.getById(11)));
        if (resistanceEffect != null) {
            resistanceReduce = (damage / 5.0) * (resistanceEffect.getAmplifier() + 1);
        } else {
            resistanceReduce = 0.0;
        }

        return damage - armorReduce - armorToughnessReduce - resistanceReduce;
    }

    @SuppressWarnings("deprecation")
    public static EntityDamageByEntityEvent genericPhysicalDamage(
            LivingEntity damager,
            LivingEntity damaged,
            EntityDamageEvent.DamageCause cause,
            double damage,
            DamageSource source
    ) {
        return new EntityDamageByEntityEvent(damager, damaged, cause, source,
                Math.max(0.00001, reduceDamageByResistance(damaged, damage))
        );
    }

    public static EntityDamageByEntityEvent genericPhysicalDamage(
            LivingEntity damager,
            LivingEntity damaged,
            EntityDamageEvent.DamageCause cause,
            double damage
    ) {
        var source = DamageSource.builder(DamageType.GENERIC)
                .withCausingEntity(damager)
                .withDirectEntity(damager)
                .build();

        return genericPhysicalDamage(damager, damaged, cause, damage, source);
    }

    public static EntityDamageByEntityEvent genericPositionedPhysicalDamage(
            LivingEntity damager,
            LivingEntity damaged,
            EntityDamageEvent.DamageCause cause,
            double damage,
            Location pos
    ) {
        var source = DamageSource.builder(DamageType.GENERIC)
                .withCausingEntity(damager)
                .withDirectEntity(damager)
                .withDamageLocation(pos)
                .build();

        return genericPhysicalDamage(damager, damaged, cause, damage, source);
    }

    public static EntityDamageEvent genericAnyPositionedPhysicalDamage(
            LivingEntity damaged,
            EntityDamageEvent.DamageCause cause,
            double damage,
            Location pos
    ) {
        var source = DamageSource.builder(DamageType.GENERIC)
                .withDamageLocation(pos)
                .build();

        return genericAnyPhysicalDamage(damaged, cause, damage, source);
    }

    public static EntityDamageEvent genericAnyPhysicalDamage(
            LivingEntity damaged,
            EntityDamageEvent.DamageCause cause,
            double damage,
            DamageSource source
    ) {
        return new EntityDamageEvent(damaged, cause, source,
                Math.max(0.00001, reduceDamageByResistance(damaged, damage))
        );
    }

    public static EntityDamageEvent genericAnyPhysicalDamage(
            LivingEntity damaged,
            EntityDamageEvent.DamageCause cause,
            double damage
    ) {
        var source = DamageSource.builder(DamageType.GENERIC).build();

        return genericAnyPhysicalDamage(damaged, cause, damage, source);
    }

    public static boolean forceGenericPhysicalDamage(
            LivingEntity damager,
            LivingEntity damaged,
            EntityDamageEvent.DamageCause cause,
            double damage
    ) {
        if (damaged.getNoDamageTicks() > 0) return false;
        return forceDamage(genericPhysicalDamage(damager, damaged, cause, damage));
    }

    public static boolean forceGenericAnyPhysicalDamage(
            LivingEntity damaged,
            EntityDamageEvent.DamageCause cause,
            double damage
    ) {
        if (damaged.getNoDamageTicks() > 0) return false;
        return forceDamage(genericAnyPhysicalDamage(damaged, cause, damage));
    }

    /**
     * @return false if damage event is canceled
     */
    public static boolean forceDamage(EntityDamageEvent e) {
        Bukkit.getPluginManager().callEvent(e);
        if (!e.isCancelled()) {
            if (e.getEntity() instanceof LivingEntity le) {
                forceDamage(e, le);
                le.damage(0.0000001);
                return true;
            }
        }
        return false;
    }

    /**
     * @return false if damage event is canceled
     */
    public static boolean forceDamage(EntityDamageByEntityEvent e) {
        Bukkit.getPluginManager().callEvent(e);
        if (!e.isCancelled()) {
            if (e.getEntity() instanceof LivingEntity le) {
                forceDamage(e, le);
                le.damage(0.0000001, e.getDamager());
                return true;
            }
        }
        return false;
    }

    private static void forceDamage(EntityDamageEvent e, LivingEntity le) {
        var damage = e.getFinalDamage();
        var absorption = le.getAbsorptionAmount();

        if (damage <= absorption) {
            le.setAbsorptionAmount(absorption - damage);
        } else {
            double healthDamage = damage - absorption;
            le.setHealth(Math.max(0, le.getHealth() - healthDamage));
            le.setAbsorptionAmount(0);
        }
    }
}
