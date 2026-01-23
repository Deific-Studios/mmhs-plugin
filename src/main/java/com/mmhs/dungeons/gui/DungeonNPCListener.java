package com.mmhs.dungeons.gui; // Correct package

import com.mmhs.dungeons.core.ProgressionManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class DungeonNPCListener implements Listener {

    private final ProgressionManager progressionManager;

    public DungeonNPCListener(ProgressionManager progressionManager) {
        this.progressionManager = progressionManager;
    }

    @EventHandler
    public void onNPCClick(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        // 1. Check if it is a Wither Skeleton
        if (entity.getType() != EntityType.WITHER_SKELETON) return;
        if (entity.customName() == null) return;

        // 2. Check Name
        if (entity.getCustomName().toString().contains("Dungeon Master")) {
            
            event.setCancelled(true); // Prevent entity interaction menu

            // 3. Open the "Select Dungeon" Menu
            DungeonGUI gui = new DungeonGUI(progressionManager);
            player.openInventory(gui.getInventory());
        }
    }
}