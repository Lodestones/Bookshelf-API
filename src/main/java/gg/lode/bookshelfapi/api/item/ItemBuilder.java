package gg.lode.bookshelfapi.api.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import gg.lode.bookshelfapi.api.util.MiniMessageHelper;
import gg.lode.bookshelfapi.api.util.PaperCapabilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nonnegative;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class ItemBuilder {

    private final List<Consumer<ItemStack>> onBuildConsumers = new ArrayList<>();
    private final List<NamespacedKey> tags = new ArrayList<>();
    private final HashMap<NamespacedKey, String> stringTags = new HashMap<>();
    private final ArrayList<PotionEffect> potionEffects = new ArrayList<>();
    private final HashMap<Enchantment, Integer> enchantments = new HashMap<>();
    private final HashMap<Enchantment, Integer> bookEnchantments = new HashMap<>();
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
    private TrimPattern trimPattern = null;
    private TrimMaterial trimMaterial = null;
    private List<Component> lore;
    private NamespacedKey itemModelKey;
    private Integer maxStackSize;
    private Boolean hideTooltip;
    private Boolean glintOverride;
    private NamespacedKey tooltipStyle;
    private Boolean glider;
    private Object rarity;


    public ItemBuilder(Material material) {
        this.material = material;
        this.skullPlayer = null;
        this.base64Skull = null;
        this.potionColor = Color.WHITE;
        this.leatherColor = "#A06540";
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

    public ItemBuilder() {
    }

    // Attribute-based methods removed for modern versions

    public String leatherColor() {
        return this.leatherColor;
    }

    public ItemBuilder leatherColor(String color) {
        this.leatherColor = color;
        return this;
    }

    public void addOnBuildConsumer(Consumer<ItemStack> consumer) {
        this.onBuildConsumers.add(consumer);
    }

    public List<Consumer<ItemStack>> buildConsumers() {
        return this.onBuildConsumers;
    }

    public String titleString() {
        return MiniMessage.miniMessage().serialize(this.title);
    }

    public Component title() {
        return this.title;
    }

    // Attribute-based methods removed for modern versions

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
        this.title = MiniMessageHelper.deserialize(title).decoration(TextDecoration.ITALIC, false);
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

    public ItemBuilder itemModel(NamespacedKey modelKey) {
        this.itemModelKey = modelKey;
        return this;
    }

    public ItemBuilder maxStackSize(int size) {
        this.maxStackSize = size;
        return this;
    }

    public ItemBuilder hideTooltip(boolean hide) {
        this.hideTooltip = hide;
        return this;
    }

    public ItemBuilder enchantmentGlintOverride(boolean override) {
        this.glintOverride = override;
        return this;
    }

    public ItemBuilder tooltipStyle(NamespacedKey styleKey) {
        this.tooltipStyle = styleKey;
        return this;
    }

    public ItemBuilder glider(boolean glider) {
        this.glider = glider;
        return this;
    }

    public ItemBuilder rarity(Object itemRarityOrName) {
        this.rarity = itemRarityOrName;
        return this;
    }

    public TrimPattern trimPattern() {
        return this.trimPattern;
    }

    public ItemBuilder trimPattern(TrimPattern trimPattern) {
        this.trimPattern = trimPattern;
        return this;
    }

    public TrimMaterial trimMaterial() {
        return this.trimMaterial;
    }

    public ItemBuilder trimMaterial(TrimMaterial trimMaterial) {
        this.trimMaterial = trimMaterial;
        return this;
    }

    public ItemBuilder amount(@Nonnegative int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder type(Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder tag(NamespacedKey... key) {
        this.tags.addAll(Arrays.stream(key).toList());
        return this;
    }

    public ItemBuilder tag(NamespacedKey key, String value) {
        this.stringTags.put(key, value);
        return this;
    }

    public ItemBuilder lore(List<Component> lores) {
        this.lore = lores.stream().map(c -> c.decoration(TextDecoration.ITALIC, false)).toList();
        return this;
    }

    public ItemBuilder lore(String... lores) {
        this.lore = Arrays.stream(lores).map(MiniMessageHelper::deserialize).map(c -> c.decoration(TextDecoration.ITALIC, false)).toList();
        return this;
    }

    public List<Component> lore() {
        return this.lore;
    }

    public String loreString() {
        return this.lore.stream().map(MiniMessageHelper::serialize).collect(Collectors.joining("\n"));
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
        if (this.itemModelKey != null) {
            PaperCapabilities.setItemModelIfSupported(meta, this.itemModelKey);
        }
        if (this.maxStackSize != null) {
            PaperCapabilities.setMaxStackSizeIfSupported(meta, this.maxStackSize);
        }
        if (this.hideTooltip != null) {
            PaperCapabilities.setHideTooltipIfSupported(meta, this.hideTooltip);
        }
        if (this.glintOverride != null) {
            PaperCapabilities.setEnchantmentGlintOverrideIfSupported(meta, this.glintOverride);
        }
        if (this.tooltipStyle != null) {
            PaperCapabilities.setTooltipStyleIfSupported(meta, this.tooltipStyle);
        }
        if (this.glider != null) {
            PaperCapabilities.setGliderIfSupported(meta, this.glider);
        }
        if (this.rarity != null) {
            PaperCapabilities.setRarityIfSupported(meta, this.rarity);
        }
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
        if (meta instanceof ArmorMeta armorMeta) {
            if (this.trimPattern != null && this.trimMaterial != null)
                armorMeta.setTrim(new ArmorTrim(this.trimMaterial, this.trimPattern));
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

        stringTags.forEach((key, value) -> meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value));
        tags.forEach(key -> meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true));
        // Attribute modifiers removed for modern versions
        meta.lore(this.lore);
        this.itemStack.setItemMeta(meta);
        this.itemStack.addUnsafeEnchantments(this.enchantments);

        for (Consumer<ItemStack> consumer : this.onBuildConsumers) {
            consumer.accept(this.itemStack);
        }

        return this.itemStack;
    }

}
