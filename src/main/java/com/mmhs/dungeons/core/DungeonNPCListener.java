package com.mmhs.dungeons.core;

import com.mmhs.dungeons.gui.DungeonGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;
import java.util.Random;

public class DungeonNPCListener implements Listener {

    private final DungeonInstanceManager instanceManager;
    private final PartyManager partyManager;
    private final ProgressionManager progressionManager; // New Dependency
    private final Random random = new Random();

    // Fun quotes
    private final List<String> quotes = List.of(
        "Are you prepared to face the abyss?",
        "Many have entered... few have returned.",
        "The storms are restless today.",
        "Do not forget your potions.",
        "The King awaits his next challenger."
    );

    // CONSTRUCTOR (This fixes the "Not Initialized" error)
    public DungeonNPCListener(DungeonInstanceManager instanceManager, PartyManager partyManager, ProgressionManager progressionManager) {
        this.instanceManager = instanceManager;
        this.partyManager = partyManager;
        this.progressionManager = progressionManager;
    }

    @EventHandler
    public void onClickNPC(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Entity entity = event.getRightClicked();
        
        // Check for Tag "DUNGEON_MASTER"
        if (entity.getScoreboardTags().contains("DUNGEON_MASTER")) {
            event.setCancelled(true); 
            Player p = event.getPlayer();

            // 1. Play Sound & Effect
            p.playSound(entity.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1f, 1f);
            p.getWorld().spawnParticle(Particle.ENCHANT, entity.getLocation().add(0, 2, 0), 15, 0.5, 0.5, 0.5, 0.1);

            // 2. Random Dialogue
            String quote = quotes.get(random.nextInt(quotes.size()));
            p.sendMessage(Component.text("Dungeon Master: ").color(NamedTextColor.GOLD)
                .append(Component.text(quote).color(NamedTextColor.YELLOW)));

            // 3. Logic Check: Only Leader can open
            if (partyManager.isInParty(p) && !partyManager.isLeader(p)) {
                p.sendMessage(Component.text("Only the Party Leader can select a dungeon!").color(NamedTextColor.RED));
                return;
            }

            // 4. Open GUI (Uses the progressionManager we added)
            p.openInventory(new DungeonGUI(progressionManager).getInventory());
        }
    }
}