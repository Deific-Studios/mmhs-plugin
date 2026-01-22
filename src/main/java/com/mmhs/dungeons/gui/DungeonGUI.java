package com.mmhs.dungeons.gui;

import com.mmhs.dungeons.core.ProgressionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DungeonGUI implements InventoryHolder {

    private final ProgressionManager progressionManager;
    private final Inventory inventory;
    private final boolean isDifficultySelection;

    // View 1: Select Dungeon
    public DungeonGUI(ProgressionManager progressionManager) {
        this.progressionManager = progressionManager;
        this.isDifficultySelection = false;
        this.inventory = Bukkit.createInventory(this, 27, Component.text("Select a Dungeon").color(NamedTextColor.DARK_GRAY));
        
        initDungeonSelector();
    }

    // View 2: Select Difficulty
    public DungeonGUI(ProgressionManager progressionManager, Player player) {
        this.progressionManager = progressionManager;
        this.isDifficultySelection = true;
        this.inventory = Bukkit.createInventory(this, 27, Component.text("Select Difficulty").color(NamedTextColor.DARK_GRAY));
        
        initDifficultySelector(player);
    }

    private void initDungeonSelector() {
        // Slot 11: The Catacombs (Starter)
        ItemStack icon = new ItemStack(Material.WITHER_SKELETON_SKULL);
        ItemMeta meta = icon.getItemMeta();
        meta.displayName(Component.text("The Catacombs").color(NamedTextColor.RED).decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
            Component.text("The ancient crypts beneath the city.").color(NamedTextColor.GRAY),
            Component.text(""),
            Component.text("▶ Click to Select").color(NamedTextColor.YELLOW)
        ));
        icon.setItemMeta(meta);
        inventory.setItem(11, icon); // Center-ish

        // Slot 15: Coming Soon
        ItemStack locked = new ItemStack(Material.GRAY_DYE);
        ItemMeta lockMeta = locked.getItemMeta();
        lockMeta.displayName(Component.text("???").color(NamedTextColor.DARK_GRAY));
        locked.setItemMeta(lockMeta);
        inventory.setItem(15, locked);
    }

    private void initDifficultySelector(Player player) {
        int progress = progressionManager.getUnlockedTier(player);

        // Tier 1 (Always Unlocked)
        inventory.setItem(11, createDifficultyItem(1, true, progress >= 1));

        // Tier 2 (Requires T1 completion)
        boolean t2Unlocked = progress >= 1;
        inventory.setItem(13, createDifficultyItem(2, t2Unlocked, progress >= 2));

        // Tier 3 (Requires T2 completion)
        boolean t3Unlocked = progress >= 2;
        inventory.setItem(15, createDifficultyItem(3, t3Unlocked, false));
        
        // Back Button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.displayName(Component.text("Back").color(NamedTextColor.RED));
        back.setItemMeta(meta);
        inventory.setItem(22, back);
    }

    private ItemStack createDifficultyItem(int tier, boolean unlocked, boolean completed) {
        Material mat = unlocked ? (tier == 1 ? Material.LIME_CONCRETE : tier == 2 ? Material.YELLOW_CONCRETE : Material.RED_CONCRETE) : Material.RED_STAINED_GLASS_PANE;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        String name = tier == 1 ? "Normal" : tier == 2 ? "Hard" : "Nightmare";
        NamedTextColor color = tier == 1 ? NamedTextColor.GREEN : tier == 2 ? NamedTextColor.YELLOW : NamedTextColor.RED;

        meta.displayName(Component.text("Tier " + tier + ": " + name).color(color).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new java.util.ArrayList<>();
        lore.add(Component.text(""));
        if (unlocked) {
            lore.add(Component.text("Rewards:").color(NamedTextColor.GRAY));
            lore.add(Component.text("- " + (tier * 100) + "% XP").color(NamedTextColor.WHITE));
            lore.add(Component.text("- Tier " + tier + " Loot").color(NamedTextColor.WHITE));
            lore.add(Component.text(""));
            if (completed) {
                lore.add(Component.text("✔ COMPLETED").color(NamedTextColor.GREEN));
            } else {
                lore.add(Component.text("▶ Click to Start").color(NamedTextColor.YELLOW));
            }
        } else {
            lore.add(Component.text("LOCKED").color(NamedTextColor.RED).decoration(TextDecoration.BOLD, true));
            lore.add(Component.text("Complete previous tier to unlock.").color(NamedTextColor.GRAY));
        }
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
    
    public boolean isDifficultySelection() { return isDifficultySelection; }
}