package com.mmhs.dungeons.core;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class ProgressionManager {
    
    private final NamespacedKey progressKey;

    public ProgressionManager(Plugin plugin) {
        this.progressKey = new NamespacedKey(plugin, "dungeon_tier_progress");
    }

    /**
     * @return 0 = No clear, 1 = Tier 1 cleared (Unlocked T2), 2 = Tier 2 cleared (Unlocked T3)
     */
    public int getUnlockedTier(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        return data.getOrDefault(progressKey, PersistentDataType.INTEGER, 0); // Default to 0 (Only T1 unlocked)
    }

    public void completeTier(Player player, int tierCleared) {
        int currentProgress = getUnlockedTier(player);
        
        // If they just cleared Tier 1, they now have access level 1 (which allows T2)
        // If they cleared Tier 2, they get level 2 (allows T3)
        if (tierCleared > currentProgress) {
            player.getPersistentDataContainer().set(progressKey, PersistentDataType.INTEGER, tierCleared);
            // Optionally play a sound or send a message here
        }
    }
    
    public boolean canAccess(Player player, int targetTier) {
        // Tier 1 is always open
        if (targetTier == 1) return true;
        
        // To enter Tier 2, you must have cleared Tier 1 (progress >= 1)
        // To enter Tier 3, you must have cleared Tier 2 (progress >= 2)
        return getUnlockedTier(player) >= (targetTier - 1);
    }
}