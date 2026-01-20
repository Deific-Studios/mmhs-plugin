package com.mmhs.dungeons.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

public class DungeonRecipes {
    private final DungeonItems items;
    private final Plugin plugin;

    public DungeonRecipes(DungeonItems items, Plugin plugin) {
        this.items = items;
        this.plugin = plugin;
    }

    public void register() {
        // Soulpiercer
        registerTier1("soulpiercer_1", items.soulpiercerI(), items.stormEssence(), Material.DIAMOND_SWORD);
        registerTier2("soulpiercer_2", items.soulpiercerII(), Material.TRIDENT); // Use Material, verify in Listener
        registerTier3("soulpiercer_3", items.soulpiercerIII(), Material.TRIDENT);

        // Bulwark
        registerTier1("bulwark_1", items.bulwarkOfResolveI(), items.ancientPlating(), Material.SHIELD);
        registerTier2("bulwark_2", items.bulwarkOfResolveII(), Material.SHIELD);
        registerTier3("bulwark_3", items.bulwarkOfResolveIII(), Material.SHIELD);

        // Daggers
        registerTier1("daggers_1", items.nightveilDaggersI(), items.shadowDust(), Material.IRON_SWORD);
        registerTier2("daggers_2", items.nightveilDaggersII(), Material.DIAMOND_SWORD); // Tier 1 is Diamond
        registerTier3("daggers_3", items.nightveilDaggersIII(), Material.NETHERITE_SWORD); // Tier 2 is Netherite

        // Axe
        registerTier1("axe_1", items.stonewardenAxeI(), items.golemCore(), Material.DIAMOND_AXE);
        registerTier2("axe_2", items.stonewardenAxeII(), Material.DIAMOND_AXE);
        registerTier3("axe_3", items.stonewardenAxeIII(), Material.NETHERITE_AXE);

        // Broadsword
        registerTier1("broadsword_1", items.heroesBroadswordI(), items.heroHilt(), Material.DIAMOND_SWORD);
        registerTier2("broadsword_2", items.heroesBroadswordII(), Material.DIAMOND_SWORD);
        registerTier3("broadsword_3", items.heroesBroadswordIII(), Material.NETHERITE_SWORD);
        
        // Crown
        registerTier1("crown_1", items.crownOfMonstersI(), items.cursedGold(), Material.GOLDEN_HELMET);
        registerTier2("crown_2", items.crownOfMonstersII(), Material.GOLDEN_HELMET);
        registerTier3("crown_3", items.crownOfMonstersIII(), Material.GOLDEN_HELMET);
    }

    private void registerTier1(String keyName, ItemStack result, ItemStack bossDrop, Material baseItem) {
        NamespacedKey key = new NamespacedKey(plugin, keyName);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(" D ", " B ", " D ");
        recipe.setIngredient('D', new RecipeChoice.ExactChoice(bossDrop));
        recipe.setIngredient('B', baseItem); 
        Bukkit.addRecipe(recipe);
    }

    private void registerTier2(String keyName, ItemStack result, Material previousMat) {
        NamespacedKey key = new NamespacedKey(plugin, keyName);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("FFF", "FIF", "FFF");
        
        recipe.setIngredient('I', previousMat); // Loose check (Material only)
        recipe.setIngredient('F', new RecipeChoice.ExactChoice(items.dungeonFragment())); // Strict check
        // recipe.setIngredient('P', Material.DIAMOND);
        
        Bukkit.addRecipe(recipe);
    }

    private void registerTier3(String keyName, ItemStack result, Material previousMat) {
        NamespacedKey key = new NamespacedKey(plugin, keyName);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("SAS", "AIA", "SAS");
        
        recipe.setIngredient('I', previousMat); // Loose check
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(items.ascensionShard())); // Strict check
        recipe.setIngredient('A', new RecipeChoice.ExactChoice(items.dungeonFragment())); // Strict check
        
        Bukkit.addRecipe(recipe);
    }

    
}