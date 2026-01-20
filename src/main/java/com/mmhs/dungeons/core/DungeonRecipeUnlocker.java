package com.mmhs.dungeons.core;

import com.mmhs.dungeons.items.DungeonRecipes;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class DungeonRecipeUnlocker implements Listener {

    private final Plugin plugin;
    // List of all our custom recipe keys
    private final List<NamespacedKey> recipeKeys = new ArrayList<>();

    public DungeonRecipeUnlocker(Plugin plugin) {
        this.plugin = plugin;
        loadKeys();
    }

    private void loadKeys() {
        // We must manually list the keys we defined in DungeonRecipes.java
        // (Alternatively, you could make DungeonRecipes store them in a static list, 
        // but this is safer and easier to read).
        
        String[] recipeNames = {
            // Soulpiercer
            "soulpiercer_1", "soulpiercer_2", "soulpiercer_3",
            // Bulwark
            "bulwark_1", "bulwark_2", "bulwark_3",
            // Daggers
            "daggers_1", "daggers_2", "daggers_3",
            // Axe
            "axe_1", "axe_2", "axe_3",
            // Broadsword
            "broadsword_1", "broadsword_2", "broadsword_3",
            // Crown
            "crown_1", "crown_2", "crown_3"
        };

        for (String name : recipeNames) {
            recipeKeys.add(new NamespacedKey(plugin, name));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Give the player the recipe book entry for every dungeon item
        player.discoverRecipes(recipeKeys);
    }
}