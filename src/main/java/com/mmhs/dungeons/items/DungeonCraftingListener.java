package com.mmhs.dungeons.items;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class DungeonCraftingListener implements Listener {

    private final DungeonItems dungeonItems;

    public DungeonCraftingListener(DungeonItems dungeonItems) {
        this.dungeonItems = dungeonItems;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe == null) return;
        if (!(recipe instanceof ShapedRecipe shaped)) return;

        NamespacedKey key = shaped.getKey();
        if (!key.getNamespace().equals("mmhsdungeons")) return; // Only check our recipes
        // (If your plugin name in plugin.yml is different, change this string)

        CraftingInventory inv = event.getInventory();
        String id = key.getKey(); // e.g., "soulpiercer_2"

        // Logic: If it is a Tier 2/3 recipe, validate the center item
        if (id.endsWith("_2") || id.endsWith("_3")) {
            validateUpgrade(inv, id);
        }
    }

    private void validateUpgrade(CraftingInventory inv, String recipeId) {
        // The center item is at slot 5 (in a 3x3 grid: 0 1 2 / 3 4 5 / 6 7 8)
        // Wait! In Spigot, the matrix array indices are 0-8. Center is index 4.
        ItemStack centerItem = inv.getMatrix()[4];

        if (centerItem == null) {
            inv.setResult(null);
            return;
        }

        DungeonItems.Id itemId = dungeonItems.getId(centerItem);
        if (itemId == null) {
            // It's a vanilla item trying to act as a custom item -> Deny
            inv.setResult(null);
            return;
        }

        // Strict Check: Ensure the input matches the expected previous tier
        // Example: recipe "soulpiercer_2" requires "SOULPIERCER_I"
        String expectedInput = getExpectedPrevTier(recipeId);
        if (!itemId.name().equals(expectedInput)) {
            inv.setResult(null);
        }
    }

    private String getExpectedPrevTier(String recipeId) {
        // Simple string manipulation to guess the required ID
        // soulpiercer_2 -> SOULPIERCER_I
        // soulpiercer_3 -> SOULPIERCER_II
        
        String[] parts = recipeId.split("_"); // [soulpiercer, 2]
        String name = parts[0].toUpperCase();
        int tier = Integer.parseInt(parts[1]);
        
        String roman = (tier == 2) ? "I" : "II";
        
        // Special case handling for naming mismatches if any
        if (name.equals("DAGGERS")) name = "NIGHTVEIL_DAGGERS";
        if (name.equals("BROADSWORD")) name = "HEROES_BROADSWORD";
        if (name.equals("AXE")) name = "STONEWARDEN_AXE";
        if (name.equals("CROWN")) name = "CROWN_OF_MONSTERS";
        if (name.equals("BULWARK")) name = "BULWARK_OF_RESOLVE";

        return name + "_" + roman;
    }
}