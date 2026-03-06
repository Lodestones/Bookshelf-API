package gg.lode.bookshelfapi.api.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Applies armor-and-enchantment-scaled "true damage" via setHealth.
 * <p>
 * Damage is calibrated against a baseline gear set (default: full Protection III diamond).
 * A player wearing that gear loses exactly the specified hearts; players with less
 * protection take proportionally more, and players with more take less.
 * <p>
 * Example scaling for 5 hearts of true damage (default baseline):
 * <pre>
 *   Full Prot III Diamond  →  5.00 hearts (baseline)
 *   Diamond, no enchants   →  9.62 hearts
 *   Iron, no enchants      → 19.23 hearts
 *   Naked                  → 38.46 hearts
 * </pre>
 * The baseline can be adjusted at runtime via the static setters.
 */
public final class TrueDamageHelper {

    // Full Protection III Diamond Armor baseline
    private static double BASELINE_ARMOR = 20.0;
    private static double BASELINE_TOUGHNESS = 8.0;
    private static int BASELINE_PROTECTION_LEVEL = 12; // 4 pieces x Protection III

    private TrueDamageHelper() {}

    public static void setBaselineArmor(double baselineArmor) {
        BASELINE_ARMOR = baselineArmor;
    }

    public static void setBaselineToughness(double baselineToughness) {
        BASELINE_TOUGHNESS = baselineToughness;
    }

    public static void setBaselineProtectionLevel(int baselineProtectionLevel) {
        BASELINE_PROTECTION_LEVEL = baselineProtectionLevel;
    }

    public static void applyScaledTrueDamage(LivingEntity target, double targetHearts) {
        applyScaledTrueDamage(target, null, targetHearts);
    }

    /**
     * Applies scaled true damage directly via setHealth.
     * <p>
     * The targetHearts parameter is calibrated so that a player in full Protection III
     * diamond armor loses exactly that many hearts. Players with less armor/enchantments
     * take proportionally more damage; players with more take less.
     *
     * @param target The player to damage
     * @param damager The damager
     * @param targetHearts Hearts to lose on a full Protection III diamond player
     */
    public static void applyScaledTrueDamage(LivingEntity target, @Nullable LivingEntity damager, double targetHearts) {
        applyScaledTrueDamage(target, damager, targetHearts, 0);
    }

    public static void applyScaledTrueDamage(LivingEntity target, @Nullable LivingEntity damager, double targetHearts, int noDamageTicks) {
        if (target.getNoDamageTicks() > 0) return;

        double damage = calculateScaledDamage(target, targetHearts);
        if (damager != null) target.damage(0.01, damager);
        target.setNoDamageTicks(noDamageTicks);
        target.setHealth(Math.max(0.0, target.getHealth() - damage));
    }

    /**
     * Applies scaled true damage, consuming absorption hearts first before reducing health.
     * Behaves identically to {@link #applyScaledTrueDamage} but any absorption the target
     * has will absorb damage before it reaches their actual health pool.
     *
     * @param target       The entity to damage
     * @param damager      The damager (nullable)
     * @param targetHearts Hearts to lose on a baseline-geared player
     */
    public static void applyScaledTrueDamageWithAbsorption(LivingEntity target, @Nullable LivingEntity damager, double targetHearts) {
        applyScaledTrueDamageWithAbsorption(target, damager, targetHearts, 0);
    }

    public static void applyScaledTrueDamageWithAbsorption(LivingEntity target, @Nullable LivingEntity damager, double targetHearts, int noDamageTicks) {
        if (target.getNoDamageTicks() > 0) return;

        double damage = calculateScaledDamage(target, targetHearts);
        if (damager != null) target.damage(0.01, damager);
        target.setNoDamageTicks(noDamageTicks);

        double absorption = target.getAbsorptionAmount();
        if (absorption > 0) {
            double remaining = damage - absorption;
            if (remaining <= 0) {
                target.setAbsorptionAmount(absorption - damage);
                return;
            }
            target.setAbsorptionAmount(0);
            damage = remaining;
        }

        target.setHealth(Math.max(0.0, target.getHealth() - damage));
    }

    private static double calculateScaledDamage(LivingEntity target, double targetHearts) {
        double baselineReduction = calculateTotalReduction(BASELINE_ARMOR, BASELINE_TOUGHNESS, BASELINE_PROTECTION_LEVEL, targetHearts);

        double armor = Objects.requireNonNull(target.getAttribute(Attribute.GENERIC_ARMOR)).getValue();
        double toughness = Objects.requireNonNull(target.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS)).getValue();
        int protectionLevel = getTotalProtectionLevel(target);
        double actualReduction = calculateTotalReduction(armor, toughness, protectionLevel, targetHearts);

        return targetHearts * (1.0 - actualReduction) / (1.0 - baselineReduction);
    }

    private static double calculateTotalReduction(double armor, double toughness, int protectionLevel, double referenceDamage) {
        double armorReduction = calculateArmorReduction(armor, toughness, referenceDamage);
        armorReduction = Math.min(armorReduction, 0.80); // vanilla cap

        double enchantReduction = Math.min(20, protectionLevel) * 0.04;

        return 1.0 - (1.0 - armorReduction) * (1.0 - enchantReduction);
    }

    private static double calculateArmorReduction(double armor, double toughness, double damage) {
        double effectiveArmor = Math.min(
                20.0,
                Math.max(
                        armor / 5.0,
                        armor - damage / (2.0 + toughness / 4.0)
                )
        );

        return effectiveArmor / 25.0;
    }

    private static int getTotalProtectionLevel(LivingEntity target) {
        int total = 0;
        EntityEquipment equipment = target.getEquipment();
        if (equipment == null) return 0;
        for (ItemStack item : equipment.getArmorContents()) {
            if (item != null) {
                total += item.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
            }
        }
        return total;
    }
}
