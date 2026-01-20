package com.mmhs.dungeons.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DungeonInstanceManager {

    private final Plugin plugin;
    private final PartyManager partyManager;
    
    // Tracks how many dungeons we've made so we never overlap
    private final AtomicInteger instanceCounter = new AtomicInteger(0);
    
    // Config: How far apart dungeons are (1000 blocks is usually safe)
    private static final int INSTANCE_SPACING = 1000;
    private static final String DUNGEON_STRUCTURE = "dungeon:starter";
    private static final String DUNGEON_WORLD_NAME = "world"; // Change if you have a specific world

    public DungeonInstanceManager(Plugin plugin, PartyManager partyManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
    }

    public void startDungeon(Player leader) {
        // 1. Get the Party
        List<Player> party = partyManager.getOnlineMembers(leader);

        // 2. Calculate Coordinates
        int index = instanceCounter.getAndIncrement();
        int x = index * INSTANCE_SPACING;
        int y = 100; // Keep it uniform
        int z = 0;   // Line them up on the Z axis

        // 3. Generate the Structure
        // We use console commands because the Spigot API for Jigsaws is nonexistent/complex.
        String command = String.format("place structure %s %d %d %d", DUNGEON_STRUCTURE, x, y, z);
        
        leader.sendMessage(Component.text("Generating Dungeon Instance #" + index + "...").color(NamedTextColor.YELLOW));
        
        // Run generation on the main thread
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        // 4. Teleport the Team (Wait 2 seconds for generation to finish)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location spawnLoc = new Location(Bukkit.getWorld(DUNGEON_WORLD_NAME), x, y + 1, z);
            
            for (Player p : party) {
                p.teleport(spawnLoc);
                p.setGameMode(GameMode.SURVIVAL);
                p.setHealth(20);
                p.setFoodLevel(20);
                
                p.sendTitlePart(net.kyori.adventure.title.TitlePart.TITLE, Component.text("THE DUNGEON").color(NamedTextColor.RED));
                p.sendTitlePart(net.kyori.adventure.title.TitlePart.SUBTITLE, Component.text("Instance ID: " + index).color(NamedTextColor.GRAY));
                p.playSound(p.getLocation(), org.bukkit.Sound.EVENT_RAID_HORN, 1f, 1f);
            }
        }, 40L); // 2 second delay (40 ticks)
    }
}