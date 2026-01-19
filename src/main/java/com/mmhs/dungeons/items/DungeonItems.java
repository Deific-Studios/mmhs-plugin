package com.mmhs.dungeons.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class DungeonItems {

    public enum Id {
        // Tier I
        SOULPIERCER_I,
        BULWARK_OF_RESOLVE_I,
        HEROES_BROADSWORD_I,
        NIGHTVEIL_DAGGERS_I,
        STONEWARDEN_AXE_I,
        CROWN_OF_MONSTERS_I,
        
        // Tier II
        SOULPIERCER_II,
        BULWARK_OF_RESOLVE_II,
        HEROES_BROADSWORD_II,
        NIGHTVEIL_DAGGERS_II,
        STONEWARDEN_AXE_II,
        CROWN_OF_MONSTERS_II,
        
        // Tier III
        SOULPIERCER_III,
        BULWARK_OF_RESOLVE_III,
        HEROES_BROADSWORD_III,
        NIGHTVEIL_DAGGERS_III,
        STONEWARDEN_AXE_III,
        CROWN_OF_MONSTERS_III
        
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
       SOULPIERCER (Wind Spear)
       ======================================== */

    public ItemStack soulpiercerI() {
        Material spearMaterial = Material.matchMaterial("DIAMOND_SPEAR");
        if (spearMaterial == null) spearMaterial = Material.TRIDENT;
        
        ItemStack item = new ItemStack(spearMaterial);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Soulpiercer")
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier I ✦").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("A spear infused with wind magic")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Abilities:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• Wind Charge Boost").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Right-click to propel forward").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Cooldown: 2 seconds").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("• Impaling Strike").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Hits spawn downward wind charge").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        addAttr(meta, Attribute.GENERIC_ATTACK_DAMAGE, 5.0);
        addAttr(meta, Attribute.GENERIC_ATTACK_SPEED, -0.1);

        tag(meta, Id.SOULPIERCER_I, 1);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack soulpiercerII() {
        Material spearMaterial = Material.matchMaterial("DIAMOND_SPEAR");
        if (spearMaterial == null) spearMaterial = Material.TRIDENT;
        
        ItemStack item = new ItemStack(spearMaterial);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Awakened Soulpiercer")
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier II ✦").color(TextColor.color(100, 200, 255))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Wind magic surges through the blade")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Abilities:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        // Tier I ability (improved)
        lore.add(Component.text("• Enhanced Wind Boost").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Right-click to propel forward").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Cooldown: 1.5 seconds").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        
        // Tier I ability (same)
        lore.add(Component.text("• Impaling Strike").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Hits spawn downward wind charge").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  +10% damage").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Stronger knockback").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        addAttr(meta, Attribute.GENERIC_ATTACK_DAMAGE, 6.0);
        addAttr(meta, Attribute.GENERIC_ATTACK_SPEED, -0.05);

        tag(meta, Id.SOULPIERCER_II, 2);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack soulpiercerIII() {
        Material spearMaterial = Material.matchMaterial("DIAMOND_SPEAR");
        if (spearMaterial == null) spearMaterial = Material.TRIDENT;
        
        ItemStack item = new ItemStack(spearMaterial);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Ascended Soulpiercer")
                .color(TextColor.color(0, 255, 255))
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier III ✦").color(TextColor.color(255, 215, 0))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("The wind itself bows to your will")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Abilities:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        // Tier I ability (fully mastered)
        lore.add(Component.text("• Mastered Wind Boost").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Right-click for flight burst").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Cooldown: 1 second").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        
        // Tier I ability + Tier II improvements + Tier III new effects
        lore.add(Component.text("• Piercing Gale").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Hits spawn downward wind charge").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Hits pierce through enemies").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  +20% damage vs airborne targets").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        addAttr(meta, Attribute.GENERIC_ATTACK_DAMAGE, 7.5);
        addAttr(meta, Attribute.GENERIC_ATTACK_SPEED, 0.0);

        tag(meta, Id.SOULPIERCER_III, 3);
        item.setItemMeta(meta);
        return item;
    }

    /* ========================================
       STONEWARDEN AXE (Tank Weapon)
       ======================================== */

    public ItemStack stonewardenAxeI() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Stonewarden Axe")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier I ✦").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Forged from ancient stone")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Passive:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• Warden's Protection").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  +2 armor while wielding").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        addAttr(meta, Attribute.GENERIC_ATTACK_DAMAGE, 6.0);
        addAttr(meta, Attribute.GENERIC_ATTACK_SPEED, 1);
        addAttr(meta, Attribute.GENERIC_ARMOR, 2.0);

        tag(meta, Id.STONEWARDEN_AXE_I, 1);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack stonewardenAxeII() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Awakened Stonewarden")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier II ✦").color(TextColor.color(150, 150, 150))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Stone infused with ancient resolve")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Passive:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        // Tier I ability (enhanced)
        lore.add(Component.text("• Greater Protection").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  +4 armor while wielding").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  +10% damage reduction").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        addAttr(meta, Attribute.GENERIC_ATTACK_DAMAGE, 7.0);
        addAttr(meta, Attribute.GENERIC_ATTACK_SPEED, 1);
        addAttr(meta, Attribute.GENERIC_ARMOR, 4.0);

        tag(meta, Id.STONEWARDEN_AXE_II, 2);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack stonewardenAxeIII() {
        ItemStack item = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Ascended Stonewarden")
                .color(TextColor.color(200, 200, 200))
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier III ✦").color(TextColor.color(255, 215, 0))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Unbreakable as the mountains")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Passive:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        // Tier I + II + III all combined
        lore.add(Component.text("• Eternal Fortress").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  +6 armor while wielding").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  +20% damage reduction").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Thorns II effect").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        addAttr(meta, Attribute.GENERIC_ATTACK_DAMAGE, 8.5);
        addAttr(meta, Attribute.GENERIC_ATTACK_SPEED, 1);
        addAttr(meta, Attribute.GENERIC_ARMOR, 6.0);

        tag(meta, Id.STONEWARDEN_AXE_III, 3);
        item.setItemMeta(meta);
        return item;
    }

    /* ========================================
       NIGHTVEIL DAGGERS (Assassin)
       ======================================== */

    public ItemStack nightveilDaggersI() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Nightveil Daggers")
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier I ✦").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Twin blades shrouded in shadow")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Abilities:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• Shadow Cloak").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Right-click for invisibility").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Duration: 5 seconds").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Speed II while active").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Cooldown: 15 seconds").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("• Backstab").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  +4 damage from behind").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        addAttr(meta, Attribute.GENERIC_ATTACK_DAMAGE, 3.5);
        addAttr(meta, Attribute.GENERIC_ATTACK_SPEED, 2.0);

        tag(meta, Id.NIGHTVEIL_DAGGERS_I, 1);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack nightveilDaggersII() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Awakened Nightveil")
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier II ✦").color(TextColor.color(138, 43, 226))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Shadows bend to your command")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Abilities:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        // Tier I ability (enhanced)
        lore.add(Component.text("• Enhanced Shadow Cloak").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Right-click for invisibility").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Duration: 7 seconds").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Speed II while active").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Cooldown: 15 seconds").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        
        // Tier I ability (enhanced)
        lore.add(Component.text("• Lethal Backstab").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  +6 damage from behind").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        addAttr(meta, Attribute.GENERIC_ATTACK_DAMAGE, 4.5);
        addAttr(meta, Attribute.GENERIC_ATTACK_SPEED, 2.2);

        tag(meta, Id.NIGHTVEIL_DAGGERS_II, 2);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack nightveilDaggersIII() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Ascended Nightveil")
                .color(TextColor.color(186, 85, 211))
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier III ✦").color(TextColor.color(255, 215, 0))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Death walks in your shadow")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Abilities:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        // Tier I ability (fully mastered)
        lore.add(Component.text("• Mastered Shadow Cloak").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Right-click for invisibility").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Duration: 10 seconds").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Speed II while active").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Cooldown: 15 seconds").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        
        // Tier I + II + III all combined
        lore.add(Component.text("• Assassinate").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  +10 damage from behind").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Applies Poison I for 3s").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        addAttr(meta, Attribute.GENERIC_ATTACK_DAMAGE, 6.0);
        addAttr(meta, Attribute.GENERIC_ATTACK_SPEED, 2.4);

        tag(meta, Id.NIGHTVEIL_DAGGERS_III, 3);
        item.setItemMeta(meta);
        return item;
    }

    /* ========================================
       BULWARK OF RESOLVE (Tank Shield)
       ======================================== */

    public ItemStack bulwarkOfResolveI() {
        ItemStack item = new ItemStack(Material.SHIELD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Bulwark of Resolve")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier I ✦").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("An unbreakable shield of heroes")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Ability:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• Shield Bash").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  When blocking, knockback attackers").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  1.8x knockback strength").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        tag(meta, Id.BULWARK_OF_RESOLVE_I, 1);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack bulwarkOfResolveII() {
        ItemStack item = new ItemStack(Material.SHIELD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Awakened Bulwark")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier II ✦").color(TextColor.color(255, 215, 0))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Reinforced with ancient magic")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Ability:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        // Tier I ability (enhanced)
        lore.add(Component.text("• Enhanced Shield Bash").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  When blocking, knockback attackers").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  2.2x knockback strength").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Grants Resistance I for 3s").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        tag(meta, Id.BULWARK_OF_RESOLVE_II, 2);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack bulwarkOfResolveIII() {
        ItemStack item = new ItemStack(Material.SHIELD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Ascended Bulwark")
                .color(TextColor.color(255, 223, 0))
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier III ✦").color(TextColor.color(255, 215, 0))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Impenetrable fortress of the champion")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Ability:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        // Tier I + II + III all combined
        lore.add(Component.text("• Mastered Shield Bash").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  When blocking, knockback attackers").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  2.8x knockback strength").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Grants Resistance II for 5s").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Reflects 20% damage back").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        tag(meta, Id.BULWARK_OF_RESOLVE_III, 3);
        item.setItemMeta(meta);
        return item;
    }

    /* ========================================
       HERO'S BROADSWORD (AoE DPS)
       ======================================== */

    public ItemStack heroesBroadswordI() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Hero's Broadsword")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier I ✦").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("A legendary blade of immense power")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Ability:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• Sweeping Strike").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Hits damage nearby enemies").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  6 damage in 3.5 block radius").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        addAttr(meta, Attribute.GENERIC_ATTACK_DAMAGE, 7.0);
        addAttr(meta, Attribute.GENERIC_ATTACK_SPEED, -0.9);

        tag(meta, Id.HEROES_BROADSWORD_I, 1);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack heroesBroadswordII() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Awakened Hero's Blade")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier II ✦").color(TextColor.color(220, 20, 60))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Reforged in the fires of valor")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Ability:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        // Tier I ability (enhanced)
        lore.add(Component.text("• Enhanced Sweep").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Hits damage nearby enemies").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  8 damage in 4.0 block radius").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        addAttr(meta, Attribute.GENERIC_ATTACK_DAMAGE, 8.5);
        addAttr(meta, Attribute.GENERIC_ATTACK_SPEED, -0.8);

        tag(meta, Id.HEROES_BROADSWORD_II, 2);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack heroesBroadswordIII() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Ascended Champion's Blade")
                .color(TextColor.color(255, 69, 0))
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier III ✦").color(TextColor.color(255, 215, 0))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("The blade of a true champion")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Ability:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        // Tier I + II + III all combined
        lore.add(Component.text("• Mastered Sweep").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Hits damage nearby enemies").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  12 damage in 4.5 block radius").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Applies Knockback I").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        addAttr(meta, Attribute.GENERIC_ATTACK_DAMAGE, 10.0);
        addAttr(meta, Attribute.GENERIC_ATTACK_SPEED, -0.7);

        tag(meta, Id.HEROES_BROADSWORD_III, 3);
        item.setItemMeta(meta);
        return item;
    }

    /* ========================================
       CROWN OF MONSTERS (Collection Item)
       ======================================== */

    public ItemStack crownOfMonstersI() {
        ItemStack item = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Crown of Monsters")
                .color(NamedTextColor.DARK_RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier I ✦").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("A cursed crown that absorbs monster essence")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Abilities:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• Monster Essence (0/3)").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Left-click to manage mob heads").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Grants effects while wearing").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(true);

        tag(meta, Id.CROWN_OF_MONSTERS_I, 1);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack crownOfMonstersII() {
        ItemStack item = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Awakened Crown")
                .color(NamedTextColor.DARK_RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier II ✦").color(TextColor.color(139, 0, 0))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("The crown hungers for more power")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Abilities:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        // Tier I ability (enhanced)
        lore.add(Component.text("• Enhanced Essence (0/4)").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Left-click to manage mob heads").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Stronger effects, 4 slots").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(true);

        tag(meta, Id.CROWN_OF_MONSTERS_II, 2);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack crownOfMonstersIII() {
        ItemStack item = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Ascended Crown")
                .color(TextColor.color(178, 34, 34))
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("✦ Tier III ✦").color(TextColor.color(255, 215, 0))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Master of all monsters")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(Component.text("Abilities:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        // Tier I ability (fully mastered)
        lore.add(Component.text("• Mastered Essence (0/5)").color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Left-click to manage mob heads").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Maximum effects, 5 slots").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(true);

        tag(meta, Id.CROWN_OF_MONSTERS_III, 3);
        item.setItemMeta(meta);
        return item;
    }

    /* ========================================
       Detection helpers
       ======================================== */

    public boolean isDungeonItem(ItemStack item) {
        return getId(item) != null;
    }

    public Id getId(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String raw = pdc.get(idKey, PersistentDataType.STRING);
        if (raw == null) return null;

        try {
            return Id.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public int getTier(ItemStack item) {
        if (item == null) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        Integer tier = meta.getPersistentDataContainer().get(tierKey, PersistentDataType.INTEGER);
        return tier == null ? 0 : tier;
    }

    /* ========================================
       Internal utilities
       ======================================== */

    private void tag(ItemMeta meta, Id id, int tier) {
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, id.name());
        meta.getPersistentDataContainer().set(tierKey, PersistentDataType.INTEGER, tier);
    }

    private void addAttr(ItemMeta meta, Attribute attribute, double amount) {
        AttributeModifier mod = new AttributeModifier(
                UUID.randomUUID(),
                "dungeonitems_" + attribute.name().toLowerCase(),
                amount,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.HAND
        );
        meta.addAttributeModifier(attribute, mod);
    }
}
