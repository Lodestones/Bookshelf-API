package to.lodestone.bookshelfapi.api.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import to.lodestone.bookshelfapi.api.util.MiniMessageUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class ItemBuilder {

    private ItemStack itemStack = null;
    private Material material = null;
    private int amount = 1;

    private Component title = null;
    private int modelData = 0;

    private OfflinePlayer skullPlayer;
    private PotionData potionData;
    private Color potionColor;
    private String base64Skull;
    private ItemFlag[] flags = new ItemFlag[0];
    private boolean isUnbreakable = false;
    private String leatherColor;

    private List<NamespacedKey> tags = new ArrayList<>();
    private List<Component> lore;
    private final ArrayList<PotionEffect> potionEffects = new ArrayList<>();
    private final HashMap<Enchantment, Integer> enchantments = new HashMap<>();
    private final HashMap<Enchantment, Integer> bookEnchantments = new HashMap<>();
    private final HashMap<EquipmentSlot, Double> attackSpeed = new HashMap<>();
    private final HashMap<EquipmentSlot, Double> attackDamage = new HashMap<>();
    private final HashMap<EquipmentSlot, Double> armor = new HashMap<>();
    private final HashMap<EquipmentSlot, Double> armorToughness = new HashMap<>();
    private final HashMap<EquipmentSlot, Double> knockbackResistance = new HashMap<>();
    public ItemBuilder(Material material) {
        this.material = material;
        this.skullPlayer = null;
        this.base64Skull = null;
        this.potionColor = Color.WHITE;
        this.leatherColor = "#A06540";
    }

    public ItemBuilder knockbackResistance(EquipmentSlot slot, double knockbackResistance) {
        this.knockbackResistance.put(slot, knockbackResistance);
        return this;
    }

    public ItemBuilder armorToughness(EquipmentSlot slot, double armorToughness) {
        this.armorToughness.put(slot, armorToughness);
        return this;
    }

    public ItemBuilder armor(EquipmentSlot slot, double armor) {
        this.armor.put(slot, armor);
        return this;
    }

    public String leatherColor() {
        return this.leatherColor;
    }

    public ItemBuilder leatherColor(String color) {
        this.leatherColor = color;
        return this;
    }

    public String titleString() {
        return MiniMessage.miniMessage().serialize(this.title);
    }

    public Component title() {
        return this.title;
    }

    public ItemBuilder(ItemStack itemStack) {
        this.material = itemStack.getType();
        this.itemStack = itemStack;
        this.skullPlayer = null;
        this.base64Skull = null;
        this.amount = itemStack.getAmount();
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;
        if (meta.lore() != null)
            this.lore = meta.lore();
        this.modelData = meta.hasCustomModelData() ? meta.getCustomModelData() : 0;
        this.title = meta.displayName();
        this.isUnbreakable = meta.isUnbreakable();
        this.flags = meta.getItemFlags().toArray(ItemFlag[]::new);
        ItemMeta itemMeta1 = itemStack.getItemMeta();
        if (itemMeta1 instanceof PotionMeta potionMeta) {
            this.potionData = potionMeta.getBasePotionData();
            this.potionColor = potionMeta.getColor();
        }
        if (itemMeta1 instanceof LeatherArmorMeta leatherArmorMeta) {
            this.leatherColor = String.format("#%02x%02x%02x", leatherArmorMeta.getColor().getRed(), leatherArmorMeta.getColor().getGreen(), leatherArmorMeta.getColor().getBlue());
        }
        itemMeta1 = itemStack.getItemMeta();
        if (itemMeta1 instanceof SkullMeta skullMeta) {
            this.skullPlayer = skullMeta.getOwningPlayer();
        }
    }

    public ItemBuilder attackSpeed(EquipmentSlot slot, double attackSpeed) {
        this.attackSpeed.put(slot, attackSpeed);
        return this;
    }

    public ItemBuilder attackDamage(EquipmentSlot slot, double attackDamage) {
        this.attackDamage.put(slot, attackDamage);
        return this;
    }

    public ItemBuilder potionColor(Color potionColor) {
        this.potionColor = potionColor;
        return this;
    }

    public Color potionColor() {
        return this.potionColor;
    }

    public ItemBuilder potionData(PotionData potionData) {
        this.potionData = potionData;
        return this;
    }

    public PotionData potionData() {
        return this.potionData;
    }

    public ItemBuilder enchantment(Enchantment enchantment, int level) {
        if (level <= 0)
            return this;
        this.enchantments.put(enchantment, level);
        return this;
    }

    public ItemBuilder title(String title) {
        if (title == null)
            return this;
        this.title = MiniMessageUtil.deserialize(title).decoration(TextDecoration.ITALIC, false);
        return this;
    }

    public ItemBuilder title(Component title) {
        if (title == null)
            return this;
        this.title = title.decoration(TextDecoration.ITALIC, false);
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.isUnbreakable = unbreakable;
        return this;
    }

    public ItemBuilder modelData(int modelData) {
        this.modelData = modelData;
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder type(Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder tag(NamespacedKey ...key) {
        this.tags.addAll(Arrays.stream(key).toList());
        return this;
    };

    public ItemBuilder lore(List<Component> lores) {
        this.lore = lores.stream().map(c -> c.decoration(TextDecoration.ITALIC, false)).toList();
        return this;
    }

    public ItemBuilder lore(String... lores) {
        this.lore = Arrays.stream(lores).map(MiniMessageUtil::deserialize).map(c -> c.decoration(TextDecoration.ITALIC, false)).toList();
        return this;
    }

    public List<Component> lore() {
        return this.lore;
    }

    public String loreString() {
        return this.lore.stream().map(MiniMessageUtil::serialize).collect(Collectors.joining("\n"));
    }

    public ItemBuilder flags() {
        this.flags = ItemFlag.values();
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        this.flags = flags;
        return this;
    }

    public ItemBuilder skull(Player player) {
        if (player == null) {
            this.skullPlayer = null;
        } else {
            this.skullPlayer = getOfflinePlayer(player.getName());
        }
        return this;
    }

    public ItemBuilder skull(OfflinePlayer player) {
        this.skullPlayer = player;
        return this;
    }

    public ItemBuilder skull(String base64) {
        this.base64Skull = base64;
        return this;
    }

    private OfflinePlayer getOfflinePlayer(String playerName) {
        if (playerName == null)
            return null;
        return Bukkit.getOfflinePlayer(playerName);
    }

    public ItemBuilder addPotionEffect(PotionEffect potionEffect) {
        this.potionEffects.add(potionEffect);
        return this;
    }

    public ItemBuilder addBookEnchant(Enchantment enchantment, int level) {
        this.bookEnchantments.put(enchantment, level);
        return this;
    }

    public ItemStack build() {
        if (this.material == null)
            this.material = Material.STICK;
        if (this.itemStack == null || !this.itemStack.getType().equals(this.material))
            this.itemStack = new ItemStack(this.material);
        this.itemStack.setAmount(this.amount);
        ItemMeta meta = this.itemStack.getItemMeta();
        if (this.modelData > 0)
            meta.setCustomModelData(this.modelData);
        meta.displayName((this.title != null) ? this.title : null);
        meta.setUnbreakable(this.isUnbreakable);
        meta.addItemFlags(this.flags);
        if (meta instanceof PotionMeta potionMeta) {
            if (this.potionData != null)
                potionMeta.setBasePotionData(this.potionData);
            if (this.potionColor != null)
                potionMeta.setColor(this.potionColor);
            this.potionEffects.forEach(effect -> potionMeta.addCustomEffect(effect, true));
        }
        if (meta instanceof EnchantmentStorageMeta enchantmentStorageMeta) {
            for (Map.Entry<Enchantment, Integer> bookEnchantment : this.bookEnchantments.entrySet())
                enchantmentStorageMeta.addStoredEnchant(bookEnchantment.getKey(), bookEnchantment.getValue(), true);
        }
        if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
            TextColor color = TextColor.fromHexString(this.leatherColor);
            if (color != null)
                leatherArmorMeta.setColor(Color.fromRGB(color.red(), color.green(), color.blue()));
        }
        if (meta instanceof SkullMeta skullMeta) {
            if (this.base64Skull != null) {
                GameProfile profile = new GameProfile(UUID.randomUUID(), "Apollo30");
                profile.getProperties().put("textures", new Property("textures", this.base64Skull));
                try {
                    Field profileField = skullMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(skullMeta, profile);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else if (this.skullPlayer != null) {
                skullMeta.setOwningPlayer(this.skullPlayer);
            }
        }

        tags.forEach(key -> meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true));
        if (this.attackSpeed.size() > 0) {
            for (Map.Entry<EquipmentSlot, Double> entry : attackSpeed.entrySet()) {
                meta.removeAttributeModifier(entry.getKey());
                meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.attack_speed", entry.getValue(), AttributeModifier.Operation.ADD_NUMBER, entry.getKey()));
            }
        }
        if (this.armor.size() > 0) {
            for (Map.Entry<EquipmentSlot, Double> entry : armor.entrySet()) {
                meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor", entry.getValue(), AttributeModifier.Operation.ADD_NUMBER, entry.getKey()));
            }
        }
        if (this.armorToughness.size() > 0) {
            for (Map.Entry<EquipmentSlot, Double> entry : armorToughness.entrySet()) {
                meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor_toughness", entry.getValue(), AttributeModifier.Operation.ADD_NUMBER, entry.getKey()));
            }
        }
        if (this.knockbackResistance.size() > 0) {
            for (Map.Entry<EquipmentSlot, Double> entry : knockbackResistance.entrySet()) {
                meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.knockback_resistance", entry.getValue(), AttributeModifier.Operation.ADD_NUMBER, entry.getKey()));
            }
        }
        if (this.attackDamage.size() > 0) {
            for (Map.Entry<EquipmentSlot, Double> entry : attackDamage.entrySet()) {
                meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.attack_damage", entry.getValue(), AttributeModifier.Operation.ADD_NUMBER, entry.getKey()));
            }
        }
        meta.lore(this.lore);
        this.itemStack.setItemMeta(meta);
        this.itemStack.addUnsafeEnchantments(this.enchantments);
        return this.itemStack;
    }

    public ItemBuilder() {
    }
}
