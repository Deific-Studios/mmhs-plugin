package com.mmhs.dungeons.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MessageManager {
    
    private final PartyManager partyManager;
    
    // Map<Receiver, Sender> - Stores who last messaged you
    private final Map<UUID, UUID> lastMessager = new HashMap<>();
    
    // Players who have toggled Party Chat ON
    private final Set<UUID> partyChatToggled = new HashSet<>();

    public MessageManager(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    /* ========================
       PRIVATE MESSAGING
       ======================== */
    
    public void sendPrivateMessage(Player sender, Player target, String message) {
        // Update reply history for both
        lastMessager.put(target.getUniqueId(), sender.getUniqueId());
        lastMessager.put(sender.getUniqueId(), target.getUniqueId());

        // Format: [You -> Player] msg
        sender.sendMessage(Component.text("You -> " + target.getName() + ": ")
                .color(NamedTextColor.GOLD)
                .append(Component.text(message).color(NamedTextColor.GRAY)));

        // Format: [Player -> You] msg
        target.sendMessage(Component.text(sender.getName() + " -> You: ")
                .color(NamedTextColor.GOLD)
                .append(Component.text(message).color(NamedTextColor.GRAY)));
        
        // Play subtle sound for receiver
        target.playSound(target.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
    }

    public void reply(Player sender, String message) {
        UUID targetId = lastMessager.get(sender.getUniqueId());
        if (targetId == null) {
            sender.sendMessage(Component.text("You have nobody to reply to.").color(NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayer(targetId);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(Component.text("That player is no longer online.").color(NamedTextColor.RED));
            return;
        }

        sendPrivateMessage(sender, target, message);
    }

    /* ========================
       PARTY CHAT
       ======================== */

    public void sendPartyMessage(Player sender, String message) {
        if (!partyManager.isInParty(sender)) {
            sender.sendMessage(Component.text("You are not in a party!").color(NamedTextColor.RED));
            return;
        }

        Component format = Component.text("[Party] ").color(NamedTextColor.BLUE)
                .append(Component.text(sender.getName() + ": ").color(NamedTextColor.AQUA))
                .append(Component.text(message).color(NamedTextColor.WHITE));

        for (Player member : partyManager.getOnlineMembers(sender)) {
            member.sendMessage(format);
        }
    }

    public void togglePartyChat(Player player) {
        if (!partyManager.isInParty(player)) {
            player.sendMessage(Component.text("You must be in a party to toggle chat.").color(NamedTextColor.RED));
            return;
        }

        if (partyChatToggled.contains(player.getUniqueId())) {
            partyChatToggled.remove(player.getUniqueId());
            player.sendMessage(Component.text("Party Chat: DISABLED (Talking in Global)").color(NamedTextColor.YELLOW));
        } else {
            partyChatToggled.add(player.getUniqueId());
            player.sendMessage(Component.text("Party Chat: ENABLED (Talking to Party)").color(NamedTextColor.GREEN));
        }
    }

    public boolean isPartyChatToggled(Player player) {
        return partyChatToggled.contains(player.getUniqueId());
    }
}