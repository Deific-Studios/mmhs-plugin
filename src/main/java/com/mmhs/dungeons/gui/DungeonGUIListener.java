package com.mmhs.dungeons.gui;

import com.mmhs.dungeons.core.DungeonInstanceManager;
import com.mmhs.dungeons.core.DungeonType;
import com.mmhs.dungeons.core.ProgressionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

public class DungeonGUIListener implements Listener {

    private final DungeonInstanceManager instanceManager;
    private final ProgressionManager progressionManager;
    private final Plugin plugin;

    public DungeonGUIListener(Plugin plugin, DungeonInstanceManager instanceManager, ProgressionManager progressionManager) {
        this.plugin = plugin;
        this.instanceManager = instanceManager;
        this.progressionManager = progressionManager;
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof DungeonGUI gui)) return;
        event.setCancelled(true); 
        
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        if (!gui.isDifficultySelection()) {
            handleDungeonSelect(player, event.getSlot());
        } else {
            handleDifficultySelect(player, event.getSlot());
        }
    }

    private void handleDungeonSelect(Player p, int slot) {
        if (slot == 11) { // The Catacombs (Moss)
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            p.openInventory(new DungeonGUI(progressionManager, p).getInventory());
        } else if (slot == 15) {
            p.sendMessage(Component.text("This dungeon is coming soon!").color(NamedTextColor.RED));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
        }
    }

    private void handleDifficultySelect(Player p, int slot) {
        // Map slots to Dungeon Types
        DungeonType selectedType = null;
        
        if (slot == 11) selectedType = DungeonType.MOSS;     // Tier 1
        if (slot == 13) selectedType = DungeonType.LAB;      // Tier 2
        if (slot == 15) selectedType = DungeonType.DEEP_DARK; // Tier 3
        
        if (slot == 22) { // Back button
            p.openInventory(new DungeonGUI(progressionManager).getInventory());
            return;
        }

        if (selectedType != null) {
            // Check if player has unlocked this tier
            if (progressionManager.canAccess(p, selectedType.tier)) {
                p.closeInventory();
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.5f);
                
                // FIXED LINE: Now sends the Enum (selectedType) instead of just a number
                instanceManager.startDungeon(p, selectedType);
                
            } else {
                p.sendMessage(Component.text("You must complete the previous tier first!").color(NamedTextColor.RED));
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        }
    }
}