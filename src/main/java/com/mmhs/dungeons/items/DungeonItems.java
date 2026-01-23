package com.mmhs.dungeons.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public final class DungeonItems {

    public enum Id {
        // Weapons
        SOULPIERCER_I, SOULPIERCER_II, SOULPIERCER_III,
        BULWARK_OF_RESOLVE_I, BULWARK_OF_RESOLVE_II, BULWARK_OF_RESOLVE_III,
        HEROES_BROADSWORD_I, HEROES_BROADSWORD_II, HEROES_BROADSWORD_III,
        NIGHTVEIL_DAGGERS_I, NIGHTVEIL_DAGGERS_II, NIGHTVEIL_DAGGERS_III,
        STONEWARDEN_AXE_I, STONEWARDEN_AXE_II, STONEWARDEN_AXE_III,
        CROWN_OF_MONSTERS_I, CROWN_OF_MONSTERS_II, CROWN_OF_MONSTERS_III,

        // Materials
        MAT_FRAGMENT, MAT_ASCENSION,
        MAT_STORM, MAT_PLATING, MAT_SHADOW,
        MAT_GOLEM, MAT_HILT, MAT_GOLD, 
        RELIC_FORGOTTEN, CORE_ABYSSAL, LOOT_MYTHIC, 
        ESSENCE_GREATER, SHARD_RELIC, GEAR_STORM,
        CORE_WEAPON
    }

    private final Plugin plugin;
    private final NamespacedKey idKey;
    private final NamespacedKey tierKey;

    public DungeonItems(Plugin plugin) {
        this.plugin = plugin;
        this.idKey = new NamespacedKey(plugin, "dungeon_item_id");
        this.tierKey = new NamespacedKey(plugin, "dungeon_item_tier");
    }

    /* ========================================
       ITEM FACTORY (Public Access)
       ======================================== */

    // RENAMED METHOD: Now matches your error's expectation
    public ItemStack getItemById(Id id) {
        if (id == null) return null;

        switch (id) {
            // --- WEAPONS ---
            case SOULPIERCER_I: return soulpiercerI();
            case SOULPIERCER_II: return soulpiercerII();
            case SOULPIERCER_III: return soulpiercerIII();

            case BULWARK_OF_RESOLVE_I: return bulwarkOfResolveI();
            case BULWARK_OF_RESOLVE_II: return bulwarkOfResolveII();
            case BULWARK_OF_RESOLVE_III: return bulwarkOfResolveIII();

            case HEROES_BROADSWORD_I: return heroesBroadswordI();
            case HEROES_BROADSWORD_II: return heroesBroadswordII();
            case HEROES_BROADSWORD_III: return heroesBroadswordIII();

            case NIGHTVEIL_DAGGERS_I: return nightveilDaggersI();
            case NIGHTVEIL_DAGGERS_II: return nightveilDaggersII();
            case NIGHTVEIL_DAGGERS_III: return nightveilDaggersIII();

            case STONEWARDEN_AXE_I: return stonewardenAxeI();
            case STONEWARDEN_AXE_II: return stonewardenAxeII();
            case STONEWARDEN_AXE_III: return stonewardenAxeIII();

            case CROWN_OF_MONSTERS_I: return crownOfMonstersI();
            case CROWN_OF_MONSTERS_II: return crownOfMonstersII();
            case CROWN_OF_MONSTERS_III: return crownOfMonstersIII();

            // --- MATERIALS ---
            case MAT_FRAGMENT: return dungeonFragment();
            case MAT_ASCENSION: return ascensionShard();
            case MAT_STORM: return stormEssence();
            case MAT_PLATING: return ancientPlating();
            case MAT_SHADOW: return shadowDust();
            case MAT_GOLEM: return golemCore();
            case MAT_HILT: return heroHilt();
            case MAT_GOLD: return cursedGold();
            case RELIC_FORGOTTEN: return forgottenRelic();
            case CORE_ABYSSAL: return bossCore();
            case LOOT_MYTHIC: return mythicLoot();
            case ESSENCE_GREATER: return greaterEssence();
            case SHARD_RELIC: return relicShard();
            case GEAR_STORM: return stormComponent();
            case CORE_WEAPON: return weaponCore();

            default: return new ItemStack(Material.AIR);
        }
    }

    /* ========================================
       WEAPONS (Balanced Stats)
       ======================================== */

    // --- SOULPIERCER ---
    private Material getSpearMaterial() {
        Material mat = Material.getMaterial("DIAMOND_SPEAR");
        return (mat != null) ? mat : Material.TRIDENT; // Fallback
    }

    public ItemStack soulpiercerI() {
        return createWeapon(getSpearMaterial(), "Soulpiercer", NamedTextColor.AQUA, Id.SOULPIERCER_I, 1, 3.0, -0.1,
                "Right-click to dash forward", "Cooldown: 2s");
    }
    public ItemStack soulpiercerII() {
        return createWeapon(getSpearMaterial(), "Awakened Soulpiercer", NamedTextColor.AQUA, Id.SOULPIERCER_II, 2, 4.0, -0.1,
                "Right-click to dash forward", "Cooldown: 1.5s", "+10% Ability Damage");
    }
    public ItemStack soulpiercerIII() {
        return createWeapon(getSpearMaterial(), "Ascended Soulpiercer", NamedTextColor.AQUA, Id.SOULPIERCER_III, 3, 5.0, -0.1,
                "Right-click for flight burst", "Cooldown: 1s", "+20% Damage vs Airborne");
    }

    // --- NIGHTVEIL DAGGERS ---
    public ItemStack nightveilDaggersI() {
        return createWeapon(Material.IRON_SWORD, "Nightveil Daggers", NamedTextColor.DARK_PURPLE, Id.NIGHTVEIL_DAGGERS_I, 1, 2.0, 2.0,
                "Backstab deals bonus damage", "Right-click to cloak (5s)");
    }
    public ItemStack nightveilDaggersII() {
        return createWeapon(Material.DIAMOND_SWORD, "Awakened Nightveil", NamedTextColor.DARK_PURPLE, Id.NIGHTVEIL_DAGGERS_II, 2, 3.0, 2.2,
                "Backstab deals bonus damage", "Right-click to cloak (7s)");
    }
    public ItemStack nightveilDaggersIII() {
        return createWeapon(Material.NETHERITE_SWORD, "Ascended Nightveil", NamedTextColor.DARK_PURPLE, Id.NIGHTVEIL_DAGGERS_III, 3, 4.0, 2.4,
                "Backstab deals massive damage", "Right-click to cloak (10s)", "Applies Poison on hit");
    }

    // --- HERO'S BROADSWORD ---
    public ItemStack heroesBroadswordI() {
        return createWeapon(Material.IRON_SWORD, "Hero's Broadsword", NamedTextColor.RED, Id.HEROES_BROADSWORD_I, 1, 5.0, -0.9,
                "Hits deal AoE damage (3.5m)", "Low attack speed");
    }
    public ItemStack heroesBroadswordII() {
        return createWeapon(Material.DIAMOND_SWORD, "Awakened Hero's Blade", NamedTextColor.RED, Id.HEROES_BROADSWORD_II, 2, 6.0, -0.8,
                "Hits deal AoE damage (4.0m)", "Low attack speed");
    }
    public ItemStack heroesBroadswordIII() {
        return createWeapon(Material.NETHERITE_SWORD, "Ascended Champion's Blade", NamedTextColor.RED, Id.HEROES_BROADSWORD_III, 3, 7.0, -0.7,
                "Hits deal AoE damage (4.5m)", "Low attack speed", "AoE applies Knockback");
    }

    // --- STONEWARDEN AXE ---
    public ItemStack stonewardenAxeI() {
        return createTankWeapon(Material.IRON_AXE, "Stonewarden Axe", Id.STONEWARDEN_AXE_I, 1, 5.0, 0.8, 2.0);
    }
    public ItemStack stonewardenAxeII() {
        return createTankWeapon(Material.DIAMOND_AXE, "Awakened Stonewarden", Id.STONEWARDEN_AXE_II, 2, 6.0, 0.9, 3.0);
    }
    public ItemStack stonewardenAxeIII() {
        return createTankWeapon(Material.NETHERITE_AXE, "Ascended Stonewarden", Id.STONEWARDEN_AXE_III, 3, 7.0, 1.0, 4.0);
    }

    // --- BULWARK (Shield) ---
    public ItemStack bulwarkOfResolveI() { return createShield("Bulwark of Resolve", Id.BULWARK_OF_RESOLVE_I, 1); }
    public ItemStack bulwarkOfResolveII() { return createShield("Awakened Bulwark", Id.BULWARK_OF_RESOLVE_II, 2); }
    public ItemStack bulwarkOfResolveIII() { return createShield("Ascended Bulwark", Id.BULWARK_OF_RESOLVE_III, 3); }

    // --- CROWN (Head) ---
    public ItemStack crownOfMonstersI() { return createCrown("Crown of Monsters", Id.CROWN_OF_MONSTERS_I, 1); }
    public ItemStack crownOfMonstersII() { return createCrown("Awakened Crown", Id.CROWN_OF_MONSTERS_II, 2); }
    public ItemStack crownOfMonstersIII() { return createCrown("Ascended Crown", Id.CROWN_OF_MONSTERS_III, 3); }

    /* ========================================
       CRAFTING MATERIALS
       ======================================== */

    public ItemStack dungeonFragment() { return createMaterial(Material.PRISMARINE_SHARD, "Dungeon Fragment", "A shard of condensed magic found in dungeons.", Id.MAT_FRAGMENT, 100); }
    public ItemStack ascensionShard() { return createMaterial(Material.AMETHYST_SHARD, "Ascension Shard", "Used to upgrade equipment to its highest potential.", Id.MAT_ASCENSION, 101); }

    public ItemStack stormEssence() { return createMaterial(Material.FEATHER, "Storm Essence", "The concentrated power of a wind spirit.", Id.MAT_STORM, 102); }
    public ItemStack ancientPlating() { return createMaterial(Material.NETHERITE_SCRAP, "Ancient Plating", "Armor scrap from a forgotten hero.", Id.MAT_PLATING, 103); }
    public ItemStack shadowDust() { return createMaterial(Material.GUNPOWDER, "Shadow Dust", "Remains of a creature that lived in darkness.", Id.MAT_SHADOW, 104); }
    public ItemStack golemCore() { return createMaterial(Material.IRON_INGOT, "Golem Core", "The beating heart of a stone construct.", Id.MAT_GOLEM, 105); }
    public ItemStack heroHilt() { return createMaterial(Material.TRIAL_KEY, "Hero's Hilt", "The broken handle of a legendary blade.", Id.MAT_HILT, 106); }
    public ItemStack cursedGold() { return createMaterial(Material.GOLD_INGOT, "Cursed Gold", "Gold that feels cold to the touch.", Id.MAT_GOLD, 107); }
    public ItemStack forgottenRelic() { return createMaterial(Material.TOTEM_OF_UNDYING, "Forgotten Relic", "Pulses with ancient authority.", Id.RELIC_FORGOTTEN, 108); }
    public ItemStack bossCore() { return createMaterial(Material.NETHER_STAR, "Abyssal Core", "Cold to the touch.", Id.CORE_ABYSSAL, 109); }
    public ItemStack mythicLoot() { return createMaterial(Material.ENDER_EYE, "Mythic Cache", "Contains unknown power.", Id.LOOT_MYTHIC, 110); }
    public ItemStack greaterEssence() { return createMaterial(Material.DRAGON_BREATH, "Greater Essence", "Concentrated magic.", Id.ESSENCE_GREATER, 111); }
    public ItemStack relicShard() { return createMaterial(Material.AMETHYST_SHARD, "Relic Shard", "Fragment of a storm.", Id.SHARD_RELIC, 112); }
    public ItemStack stormComponent() { return createMaterial(Material.BREEZE_ROD, "Storm Component", "Sparks with electricity.", Id.GEAR_STORM, 113); }
    public ItemStack weaponCore() { return createMaterial(Material.NAUTILUS_SHELL, "Lancer's Core", "Sharp enough to cut glass.", Id.CORE_WEAPON, 114); }

    /* ========================================
       INTERNAL HELPERS
       ======================================== */

    private ItemStack createWeapon(Material mat, String name, NamedTextColor color, Id id, int tier, double dmg, double speed, String... abilityLore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).color(color).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier " + roman(tier) + " ✦").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        for (String line : abilityLore) {
            lore.add(Component.text("• " + line).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);

        addAttr(meta, Attribute.GENERIC_ATTACK_DAMAGE, dmg);
        addAttr(meta, Attribute.GENERIC_ATTACK_SPEED, speed);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(true);
        tag(meta, id, tier);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createTankWeapon(Material mat, String name, Id id, int tier, double dmg, double speed, double armor) {
        ItemStack item = createWeapon(mat, name, NamedTextColor.GRAY, id, tier, dmg, speed, "+" + (int) armor + " Armor when held");
        ItemMeta meta = item.getItemMeta();
        addAttr(meta, Attribute.GENERIC_ARMOR, armor);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createShield(String name, Id id, int tier) {
        ItemStack item = new ItemStack(Material.SHIELD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).color(NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("✦ Tier " + roman(tier) + " ✦").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        meta.setUnbreakable(true);
        tag(meta, id, tier);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCrown(String name, Id id, int tier) {
        ItemStack item = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false));
        tag(meta, id, tier);
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createMaterial(Material mat, String name, String description, Id id, int modelData) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text(description).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        meta.setCustomModelData(modelData);
        tag(meta, id, 0);
        item.setItemMeta(meta);
        return item;
    }

    // --- Utilities ---
    public boolean isDungeonItem(ItemStack item) { return getId(item) != null; }

    public Id getId(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String raw = pdc.get(idKey, PersistentDataType.STRING);
        if (raw == null) return null;
        try { return Id.valueOf(raw); } catch (IllegalArgumentException ex) { return null; }
    }

    public int getTier(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return 0;
        Integer tier = item.getItemMeta().getPersistentDataContainer().get(tierKey, PersistentDataType.INTEGER);
        return tier == null ? 0 : tier;
    }

    private void tag(ItemMeta meta, Id id, int tier) {
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, id.name());
        meta.getPersistentDataContainer().set(tierKey, PersistentDataType.INTEGER, tier);
    }

    private void addAttr(ItemMeta meta, Attribute attribute, double amount) {
        NamespacedKey key = new NamespacedKey(plugin, "dungeon_" + attribute.name().toLowerCase());
        AttributeModifier mod = new AttributeModifier(
                key,
                amount,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.HAND
        );
        meta.addAttributeModifier(attribute, mod);
    }

    private String roman(int n) { return n == 1 ? "I" : n == 2 ? "II" : "III"; }
}