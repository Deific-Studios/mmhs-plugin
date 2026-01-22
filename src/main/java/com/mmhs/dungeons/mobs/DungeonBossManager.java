package com.mmhs.dungeons.mobs;

import com.mmhs.dungeons.items.DungeonItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class DungeonBossManager {

    private final NamespacedKey bossKey;
    private final DungeonItems dungeonItems; // 1. Add dependency

    // 2. Update Constructor
    public DungeonBossManager(Plugin plugin, DungeonItems dungeonItems) {
        this.bossKey = new NamespacedKey(plugin, "boss_type");
        this.dungeonItems = dungeonItems;
    }

    public void spawnBoss(Location loc, BossType type) {
        LivingEntity entity = (LivingEntity) loc.getWorld().spawnEntity(loc, type.type);

        // --- STATS ---
        if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(type.health);
        }
        entity.setHealth(type.health);
        
        // Note: Base damage is often overridden by the Weapon's attributes, 
        // so we rely on the custom item stats + this base value.
        if (entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(type.damage);
        }

        if (entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE) != null) {
            entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(50.0);
        }
        if (entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null) {
            entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);
        }

        // --- IDENTITY ---
        entity.customName(Component.text(type.displayName).color(NamedTextColor.RED));
        entity.setCustomNameVisible(true);
        entity.setRemoveWhenFarAway(false);
        entity.getPersistentDataContainer().set(bossKey, PersistentDataType.STRING, type.name());

        // --- EQUIPMENT ---
        applyBossEquipment(entity, type);
    }

    private void applyBossEquipment(LivingEntity entity, BossType type) {
        // Clear old junk
        entity.getEquipment().clear();

        // 3. Equip Custom Items
        switch (type) {
            case TEMPEST_CONSTRUCT:
                // Visual: Aqua Armor
                entity.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                entity.getEquipment().setChestplate(colorArmor(Material.LEATHER_CHESTPLATE, Color.AQUA));
                entity.getEquipment().setLeggings(colorArmor(Material.LEATHER_LEGGINGS, Color.AQUA));
                entity.getEquipment().setBoots(colorArmor(Material.LEATHER_BOOTS, Color.AQUA));
                
                // WEAPON: Soulpiercer III
                entity.getEquipment().setItemInMainHand(dungeonItems.getItemById(DungeonItems.Id.SOULPIERCER_III));
                break;

            case STONE_COLOSSUS:
                // Iron Golems cannot visually hold items, but the stats (damage) will still apply!
                // WEAPON: Stonewarden Axe III
                entity.getEquipment().setItemInMainHand(dungeonItems.getItemById(DungeonItems.Id.STONEWARDEN_AXE_III));
                break;

            case KNIGHT_OF_THE_VOID:
                // Visual: Dark Armor
                entity.getEquipment().setHelmet(new ItemStack(Material.WITHER_SKELETON_SKULL));
                entity.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
                entity.getEquipment().setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
                entity.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));
                
                // WEAPON: Nightveil Daggers III (or Broadsword if you prefer)
                entity.getEquipment().setItemInMainHand(dungeonItems.getItemById(DungeonItems.Id.NIGHTVEIL_DAGGERS_III));
                break;
            case THE_FORGOTTEN_KING:

                break;

            case THE_ABYSSAL_OVERLORD:

                break;

            case THE_STORMBOUND_KNIGHT:

                break;
            
        }
        
        // Vital: Ensure they don't drop your custom God-Tier items when they die!
        // The BossDeathListener handles loot. We don't want them dropping the gear itself.
        entity.getEquipment().setItemInMainHandDropChance(0f);
        entity.getEquipment().setHelmetDropChance(0f);
        entity.getEquipment().setChestplateDropChance(0f);
        entity.getEquipment().setLeggingsDropChance(0f);
        entity.getEquipment().setBootsDropChance(0f);
    }

    private ItemStack colorArmor(Material mat, Color color) {
        ItemStack item = new ItemStack(mat);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        item.setItemMeta(meta);
        return item;
    }

    public BossType getBossType(LivingEntity entity) {
        if (!entity.getPersistentDataContainer().has(bossKey, PersistentDataType.STRING)) return null;
        try {
            return BossType.valueOf(entity.getPersistentDataContainer().get(bossKey, PersistentDataType.STRING));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public NamespacedKey getBossKey() { return bossKey; }
}