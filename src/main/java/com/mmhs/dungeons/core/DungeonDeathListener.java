package com.mmhs.dungeons.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.List;

public class DungeonDeathListener implements Listener {

    private final PartyManager partyManager;
    private final Plugin plugin;

    public DungeonDeathListener(PartyManager partyManager, Plugin plugin) {
        this.partyManager = partyManager;
        this.plugin = plugin;
        startTetherTask();
    }

    // 1. Handle "Fake" Death (Prevents item loss & Respawn Screen)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Only apply in the Dungeon World
        if (!player.getWorld().getName().equals("world")) return; 
        
        // If already spectating, cancel damage (God mode)
        if (player.getGameMode() == GameMode.SPECTATOR) {
            event.setCancelled(true);
            return;
        }

        // Check if this damage will kill them
        if (player.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true); // Stop them from actually dying
            handleDungeonDeath(player);
        }
    }

    private void handleDungeonDeath(Player player) {
        // 1. Heal & Extinguish (So they don't die later)
        player.setHealth(20);
        player.setFireTicks(0);
        player.getActivePotionEffects().clear(); // Optional: Clear buffs

        // 2. Switch to Spectator (Items remain in inventory, just hidden)
        player.setGameMode(GameMode.SPECTATOR);

        // 3. Effects & Titles (Adventure API)
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 0.5f);
        
        Title title = Title.title(
            Component.text("YOU DIED").color(NamedTextColor.DARK_RED),
            Component.text("Spectating Party...").color(NamedTextColor.GRAY),
            Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(1000))
        );
        player.showTitle(title);

        // 4. Teleport slightly up to avoid floor clipping
        player.teleport(player.getLocation().add(0, 1, 0));
        
        // Note: Items are NOT cleared. They stay in the inventory.
        // When you set them back to SURVIVAL later, the items will still be there.
    }

    // 2. Tether Task (Keeps ghosts near the party)
    private void startTetherTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    // Check if player is a Spectator in the Dungeon
                    if (p.getGameMode() == GameMode.SPECTATOR && p.getWorld().getName().equals("world")) {
                        tetherSpectator(p);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 40L); // Check every 2 seconds
    }

    private void tetherSpectator(Player spectator) {
        List<Player> party = partyManager.getOnlineMembers(spectator);
        Player nearestAlive = null;
        double minDistance = Double.MAX_VALUE;

        // Find nearest LIVING party member
        for (Player member : party) {
            if (member.equals(spectator)) continue;
            if (member.getGameMode() != GameMode.SURVIVAL) continue;
            if (!member.getWorld().equals(spectator.getWorld())) continue;

            double dist = member.getLocation().distanceSquared(spectator.getLocation());
            if (dist < minDistance) {
                minDistance = dist;
                nearestAlive = member;
            }
        }

        // If found, ensure spectator stays close (Tether range: 30 blocks)
        if (nearestAlive != null) {
            if (minDistance > 900) { // 30^2 = 900
                spectator.teleport(nearestAlive.getLocation());
                spectator.sendMessage(Component.text("You drifted too far! Tethering to " + nearestAlive.getName())
                        .color(NamedTextColor.YELLOW));
            }
        } else {
            // Optional: If NO ONE is alive, logic for "Dungeon Failed" goes here
        }
    }
}