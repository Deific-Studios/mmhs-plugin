package com.mmhs.dungeons.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable; // <--- IMPORANT IMPORT
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class DungeonDurabilityListener implements Listener {

    private final DungeonItems dungeonItems;
    private final NamespacedKey brokenKey;

    public DungeonDurabilityListener(DungeonItems dungeonItems, Plugin plugin) {
        this.dungeonItems = dungeonItems;
        this.brokenKey = new NamespacedKey(plugin, "is_broken");
    }

    // --- 1. HANDLE BREAKING ---
    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (!dungeonItems.isDungeonItem(item)) return;

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            Damageable damageable = (Damageable) meta;
            
            // Calculate if this hit will break it
            // (Current Damage + Incoming Damage >= Max Durability)
            if (damageable.getDamage() + event.getDamage() >= item.getType().getMaxDurability()) {
                event.setCancelled(true); // Don't let the item disappear
                breakItem(item, event.getPlayer());
            }
        }
    }

    private void breakItem(ItemStack item, Player p) {
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(brokenKey, PersistentDataType.BYTE)) return;

        // 1. Mark as broken
        meta.getPersistentDataContainer().set(brokenKey, PersistentDataType.BYTE, (byte) 1);

        // 2. Visuals
        Component originalName = meta.displayName();
        if (originalName == null) originalName = Component.text(formatName(item.getType()));
        
        meta.displayName(Component.text("BROKEN - ").color(NamedTextColor.RED).append(originalName));
        
        List<Component> lore = meta.lore();
        if (lore == null) lore = new ArrayList<>();
        lore.add(0, Component.text("⚠ BROKEN ⚠").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true));
        lore.add(1, Component.text("Drag a Dungeon Fragment here to repair!").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        // 3. Remove stats (Make it useless)
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
        meta.removeAttributeModifier(Attribute.GENERIC_ARMOR);

        item.setItemMeta(meta);
        
        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
        p.sendMessage(Component.text("Your dungeon weapon shattered!").color(NamedTextColor.RED));
    }

    // --- 2. HANDLE REPAIRING ---
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (cursor == null || current == null) return;
        if (cursor.getType() == Material.AIR || current.getType() == Material.AIR) return;

        if (!dungeonItems.isDungeonItem(current)) return;
        ItemMeta meta = current.getItemMeta();
        if (!meta.getPersistentDataContainer().has(brokenKey, PersistentDataType.BYTE)) return;

        // Check Cursor ID for Fragment
        DungeonItems.Id cursorId = dungeonItems.getId(cursor);
        
        // Allow repairing with either basic Fragments or Ascension Shards
        if (cursorId == DungeonItems.Id.MAT_FRAGMENT || cursorId == DungeonItems.Id.MAT_ASCENSION) {
            
            event.setCancelled(true); // Stop normal swap
            
            // Consume 1 shard
            cursor.setAmount(cursor.getAmount() - 1);
            event.getWhoClicked().setItemOnCursor(cursor);

            // Restore Item
            DungeonItems.Id id = dungeonItems.getId(current);
            ItemStack freshItem = dungeonItems.getItemById(id);
            event.setCurrentItem(freshItem);
            
            if (event.getWhoClicked() instanceof Player p) {
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
                p.sendMessage(Component.text("Item Repaired!").color(NamedTextColor.GREEN));
            }
        }
    }
    
    private String formatName(Material mat) {
        String[] words = mat.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}