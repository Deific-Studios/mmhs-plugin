package com.mmhs.dungeons.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PartyManager {
    // Maps Player UUID -> Leader UUID
    private final Map<UUID, UUID> playerToLeader = new HashMap<>();
    // Maps Leader UUID -> Set of Members (including leader)
    private final Map<UUID, Set<UUID>> parties = new HashMap<>();
    // Pending invites: Invitee -> Leader
    private final Map<UUID, UUID> invites = new HashMap<>();

    public void createParty(Player leader) {
        if (isInParty(leader)) {
            leader.sendMessage(Component.text("You are already in a party!").color(NamedTextColor.RED));
            return;
        }
        UUID id = leader.getUniqueId();
        Set<UUID> members = new HashSet<>();
        members.add(id);
        
        parties.put(id, members);
        playerToLeader.put(id, id);
        leader.sendMessage(Component.text("Party created! Use /party invite <name>").color(NamedTextColor.GREEN));
    }

    public void invitePlayer(Player leader, Player target) {
        if (!isLeader(leader)) {
            leader.sendMessage(Component.text("Only the party leader can invite!").color(NamedTextColor.RED));
            return;
        }
        if (isInParty(target)) {
            leader.sendMessage(Component.text(target.getName() + " is already in a party.").color(NamedTextColor.RED));
            return;
        }
        
        invites.put(target.getUniqueId(), leader.getUniqueId());
        target.sendMessage(Component.text(leader.getName() + " invited you to a party! Type /party join").color(NamedTextColor.GREEN));
        leader.sendMessage(Component.text("Invited " + target.getName()).color(NamedTextColor.GREEN));
    }

    public void joinParty(Player player) {
        if (!invites.containsKey(player.getUniqueId())) {
            player.sendMessage(Component.text("You have no pending invites.").color(NamedTextColor.RED));
            return;
        }
        
        UUID leaderId = invites.remove(player.getUniqueId());
        if (!parties.containsKey(leaderId)) {
            player.sendMessage(Component.text("That party no longer exists.").color(NamedTextColor.RED));
            return;
        }

        Set<UUID> members = parties.get(leaderId);
        members.add(player.getUniqueId());
        playerToLeader.put(player.getUniqueId(), leaderId);

        // Notify everyone
        for (UUID memberId : members) {
            Player p = Bukkit.getPlayer(memberId);
            if (p != null) p.sendMessage(Component.text(player.getName() + " joined the party!").color(NamedTextColor.GREEN));
        }
    }

    public void disband(Player player) {
        // Simple logic: If leader leaves, disband. If member leaves, just remove them.
        UUID id = player.getUniqueId();
        if (!playerToLeader.containsKey(id)) return;

        UUID leaderId = playerToLeader.get(id);
        if (leaderId.equals(id)) {
            // Leader is disbanding
            Set<UUID> members = parties.remove(id);
            if (members != null) {
                for (UUID memberId : members) {
                    playerToLeader.remove(memberId);
                    Player p = Bukkit.getPlayer(memberId);
                    if (p != null) p.sendMessage(Component.text("The party was disbanded.").color(NamedTextColor.RED));
                }
            }
        } else {
            // Member leaving
            Set<UUID> members = parties.get(leaderId);
            if (members != null) {
                members.remove(id);
                playerToLeader.remove(id);
                // Notify others
                for (UUID memberId : members) {
                    Player p = Bukkit.getPlayer(memberId);
                    if (p != null) p.sendMessage(Component.text(player.getName() + " left the party.").color(NamedTextColor.YELLOW));
                }
            }
            player.sendMessage(Component.text("You left the party.").color(NamedTextColor.YELLOW));
        }
    }

    public List<Player> getOnlineMembers(Player player) {
        UUID leaderId = playerToLeader.get(player.getUniqueId());
        if (leaderId == null) return List.of(player); // Return self if no party

        List<Player> online = new ArrayList<>();
        Set<UUID> members = parties.get(leaderId);
        if (members != null) {
            for (UUID id : members) {
                Player p = Bukkit.getPlayer(id);
                if (p != null && p.isOnline()) online.add(p);
            }
        }
        return online;
    }

    public boolean isInParty(Player p) { return playerToLeader.containsKey(p.getUniqueId()); }
    public boolean isLeader(Player p) { return parties.containsKey(p.getUniqueId()); }
}