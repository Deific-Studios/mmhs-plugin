package com.mmhs.dungeons.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class CrownManager {
    private final Plugin plugin;
    private final NamespacedKey head1Key;
    private final NamespacedKey head2Key;
    private final NamespacedKey head3Key;
    private final NamespacedKey head4Key;
    private final NamespacedKey head5Key;

    public CrownManager(Plugin plugin) {
        this.plugin = plugin;
        this.head1Key = new NamespacedKey(plugin, "crown_head_1");
        this.head2Key = new NamespacedKey(plugin, "crown_head_2");
        this.head3Key = new NamespacedKey(plugin, "crown_head_3");
        this.head4Key = new NamespacedKey(plugin, "crown_head_4");
        this.head5Key = new NamespacedKey(plugin, "crown_head_5");
    }

    // Store a mob type in the crown
    public boolean storeMobHead(ItemStack crown, int slot, EntityType mobType) {
        if (slot < 0 || slot > 4) return false;
        
        ItemMeta meta = crown.getItemMeta();
        if (meta == null) return false;
        
        NamespacedKey key = getKeyForSlot(slot);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, mobType.name());
        
        updateCrownLore(meta);
        crown.setItemMeta(meta);
        return true;
    }

    // Remove a head from a slot
    public void removeMobHead(ItemStack crown, int slot) {
        if (slot < 0 || slot > 4) return;
        
        ItemMeta meta = crown.getItemMeta();
        if (meta == null) return;
        
        NamespacedKey key = getKeyForSlot(slot);
        meta.getPersistentDataContainer().remove(key);
        
        updateCrownLore(meta);
        crown.setItemMeta(meta);
    }

    // Get all stored mob types
    public List<EntityType> getStoredHeads(ItemStack crown) {
        List<EntityType> heads = new ArrayList<>();
        ItemMeta meta = crown.getItemMeta();
        if (meta == null) return heads;
        
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        
        for (int i = 0; i < 5; i++) {
            String mobName = pdc.get(getKeyForSlot(i), PersistentDataType.STRING);
            if (mobName != null) {
                try {
                    heads.add(EntityType.valueOf(mobName));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        return heads;
    }

    // Extract mob type from a player head item
    public EntityType getMobTypeFromHead(ItemStack head) {
        if (head == null || head.getType() != Material.PLAYER_HEAD) return null;
        
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return null;
        
        Component displayName = meta.displayName();
        if (displayName == null) return null;
        
        String name = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(displayName).toUpperCase();
        
        // Try to match common head naming patterns
        for (EntityType type : EntityType.values()) {
            if (name.contains(type.name().replace("_", " "))) {
                return type;
            }
            if (name.contains(type.name().replace("_", ""))) {
                return type;
            }
        }
        
        return null;
    }

    // Apply effects based on stored heads
    public void applyEffects(Player player, List<EntityType> heads) {
        for (EntityType mob : heads) {
            applyMobEffect(player, mob);
        }
    }

    private void applyMobEffect(Player player, EntityType mob) {
        int duration = 100; // 5 seconds, refreshed continuously
        
        switch (mob) {
            // UNDEAD
            case ZOMBIE:
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, duration, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, 0, false, false));
                break;
            case SKELETON:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 0, false, false));
                break;
            case ZOMBIE_VILLAGER:
                player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, duration, 0, false, false));
                break;
            case HUSK:
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0, false, false));
                break;
            case DROWNED:
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, duration, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, duration, 0, false, false));
                break;
            case STRAY:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, 1, false, false));
                break;
            case WITHER_SKELETON:
                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, duration, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 0, false, false));
                break;
            case PHANTOM:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, duration, 0, false, false));
                break;
                
            // ARTHROPODS
            case SPIDER:
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration, 1, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 0, false, false));
                break;
            case CAVE_SPIDER:
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, duration, 0, false, false));
                break;
            case SILVERFISH:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration, 0, false, false));
                break;
            case ENDERMITE:
                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, 0, false, false));
                break;
                
            // EXPLOSIVE/RANGED
            case CREEPER:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration, 2, false, false));
                break;
            case GHAST:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, duration, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0, false, false));
                break;
            case BLAZE:
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 0, false, false));
                break;
                
            // ENDERMAN
            case ENDERMAN:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, duration, 0, false, false));
                break;
                
            // ILLAGERS
            case PILLAGER:
            case VINDICATOR:
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 0, false, false));
                break;
            case EVOKER:
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 0, false, false));
                break;
            case RAVAGER:
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, 1, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 1, false, false));
                break;
            case VEX:
                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, 0, false, false));
                break;
                
            // WITCH
            case WITCH:
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, duration, 0, false, false));
                break;
                
            // GUARDIANS
            case GUARDIAN:
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, duration, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, 0, false, false));
                break;
            case ELDER_GUARDIAN:
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, duration, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, duration, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 1, false, false));
                break;
                
            // NETHER
            case MAGMA_CUBE:
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration, 1, false, false));
                break;
            case ZOMBIFIED_PIGLIN:
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 0, false, false));
                break;
            case PIGLIN:
            case PIGLIN_BRUTE:
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 1, false, false));
                break;
            case HOGLIN:
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 1, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, 0, false, false));
                break;
                
            // BOSSES
            case WITHER:
                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, duration, 1, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 2, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, 2, false, false));
                break;
            case ENDER_DRAGON:
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 2, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 2, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, 2, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, duration, 0, false, false));
                break;
                
            // WARDENS & SCULK
            case WARDEN:
                player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, duration, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 2, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, 2, false, false));
                break;
                
            // SLIMES
            case SLIME:
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration, 2, false, false));
                break;
                
            // SHULKER
            case SHULKER:
                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, 1, false, false));
                break;
                
            // BREEZE (1.21+)
            case BREEZE:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 2, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration, 1, false, false));
                break;
                
            default:
                // Generic hostile mob effect
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 0, false, false));
                break;
        }
    }

    private void updateCrownLore(ItemMeta meta) {
        List<EntityType> stored = new ArrayList<>();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        
        for (int i = 0; i < 5; i++) {
            String mobName = pdc.get(getKeyForSlot(i), PersistentDataType.STRING);
            if (mobName != null) {
                try {
                    stored.add(EntityType.valueOf(mobName));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        // Get existing lore and update only the slot count
        List<Component> lore = meta.lore();
        if (lore == null || lore.isEmpty()) return;
        
        // Find and update the slot count line (usually index 4 or 5)
        for (int i = 0; i < lore.size(); i++) {
            String text = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                    .serialize(lore.get(i));
            if (text.contains("Monster Essence") || text.contains("Enhanced Essence") || text.contains("Mastered Essence")) {
                // Determine max slots from the original text
                int maxSlots = 3;
                if (text.contains("0/4")) maxSlots = 4;
                if (text.contains("0/5")) maxSlots = 5;
                
                String baseName = text.contains("Mastered") ? "Mastered Essence" : 
                                 text.contains("Enhanced") ? "Enhanced Essence" : "Monster Essence";
                
                lore.set(i, Component.text("• " + baseName + " (" + stored.size() + "/" + maxSlots + ")")
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false));
                break;
            }
        }
        
        // Add active effects if any
        if (!stored.isEmpty()) {
            // Remove old active effects section if it exists
            int effectStartIndex = -1;
            for (int i = 0; i < lore.size(); i++) {
                String text = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                        .serialize(lore.get(i));
                if (text.contains("Active Effects:")) {
                    effectStartIndex = i;
                    break;
                }
            }
            
            if (effectStartIndex != -1) {
                // Remove old effects
                while (lore.size() > effectStartIndex) {
                    lore.remove(effectStartIndex);
                }
            }
            
            lore.add(Component.empty());
            lore.add(Component.text("Active Effects:")
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            for (EntityType mob : stored) {
                lore.add(Component.text("  • " + formatMobName(mob))
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }
        
        meta.lore(lore);
    }

    private String formatMobName(EntityType type) {
        String[] words = type.name().toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            formatted.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }
        return formatted.toString().trim();
    }

    private NamespacedKey getKeyForSlot(int slot) {
        return switch (slot) {
            case 0 -> head1Key;
            case 1 -> head2Key;
            case 2 -> head3Key;
            case 3 -> head4Key;
            case 4 -> head5Key;
            default -> head1Key;
        };
    }
}
