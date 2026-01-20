package com.mmhs.dungeons.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class DungeonNPCListener implements Listener {

    private final DungeonInstanceManager instanceManager;
    private final PartyManager partyManager;

    public DungeonNPCListener(DungeonInstanceManager instanceManager, PartyManager partyManager) {
        this.instanceManager = instanceManager;
        this.partyManager = partyManager;
    }

    @EventHandler
    public void onClickNPC(PlayerInteractEntityEvent event) {
        // Only run on main hand (prevents double firing)
        if (event.getHand() != EquipmentSlot.HAND) return;

        Entity entity = event.getRightClicked();
        
        // Logic: Is this the Dungeon Master?
        // You can check name, or add a specific tag to the entity using /summon
        if (entity.customName() == null) return;
        
        String name = ((Component) entity.customName()).toString(); // Simplified check
        // Ideally check plain text:
        // if (PlainTextComponentSerializer.plainText().serialize(entity.customName()).equals("Dungeon Master")) ...
        
        // For simplicity, let's just check if it's a Villager for now (or check a Tag)
        if (entity instanceof Villager && entity.getScoreboardTags().contains("DUNGEON_MASTER")) {
            
            event.setCancelled(true); // Don't trade
            Player p = event.getPlayer();

            // Only Leader can start
            if (partyManager.isInParty(p) && !partyManager.isLeader(p)) {
                p.sendMessage(Component.text("Only the Party Leader can enter the dungeon!").color(NamedTextColor.RED));
                return;
            }

            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            instanceManager.startDungeon(p);
        }
    }
}