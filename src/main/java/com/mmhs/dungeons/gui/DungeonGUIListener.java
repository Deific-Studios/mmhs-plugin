package com.mmhs.dungeons.gui;

import com.mmhs.dungeons.core.DungeonInstanceManager;
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
        event.setCancelled(true); // Stop taking items
        
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        // Logic split based on which screen we are on
        if (!gui.isDifficultySelection()) {
            handleDungeonSelect(player, event.getSlot());
        } else {
            handleDifficultySelect(player, event.getSlot());
        }
    }

    private void handleDungeonSelect(Player p, int slot) {
        if (slot == 11) { // The Catacombs
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            // Open Difficulty Menu
            p.openInventory(new DungeonGUI(progressionManager, p).getInventory());
        } else if (slot == 15) {
            p.sendMessage(Component.text("This dungeon is coming soon!").color(NamedTextColor.RED));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
        }
    }

    private void handleDifficultySelect(Player p, int slot) {
        int tier = -1;
        if (slot == 11) tier = 1;
        if (slot == 13) tier = 2;
        if (slot == 15) tier = 3;
        
        if (slot == 22) { // Back button
            p.openInventory(new DungeonGUI(progressionManager).getInventory());
            return;
        }

        if (tier != -1) {
            if (progressionManager.canAccess(p, tier)) {
                p.closeInventory();
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.5f);
                // START THE DUNGEON
                instanceManager.startDungeon(p, tier);
            } else {
                p.sendMessage(Component.text("You must complete the previous tier first!").color(NamedTextColor.RED));
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        }
    }
}