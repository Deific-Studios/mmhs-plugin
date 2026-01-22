package com.mmhs.dungeons.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DungeonInstanceManager {

    private final Plugin plugin;
    private final PartyManager partyManager;
    
    // Tracks how many dungeons we've made so we never overlap
    private final AtomicInteger instanceCounter = new AtomicInteger(0);
    
    // Maps Instance Index -> Difficulty Tier (1, 2, 3)
    // Useful for the MobPopulator to know how hard to make the mobs
    private final Map<Integer, Integer> activeDungeonTiers = new HashMap<>();
    
    // Config: How far apart dungeons are
    private static final int INSTANCE_SPACING = 2000; 
    private static final String TARGET_WORLD_NAME = "dungeon_hub"; 

    public DungeonInstanceManager(Plugin plugin, PartyManager partyManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
    }

    public void startDungeon(Player leader, int tier) {
        // 1. Get the Party
        List<Player> party = partyManager.getOnlineMembers(leader);

        // 2. Determine World
        World dungeonWorld = Bukkit.getWorld(TARGET_WORLD_NAME);
        if (dungeonWorld == null) {
            leader.sendMessage(Component.text("Error: World '" + TARGET_WORLD_NAME + "' not found. Using current world.").color(NamedTextColor.RED));
            dungeonWorld = leader.getWorld();
        }

        // 3. Calculate Coordinates (Uninterrupted area)
        int index = instanceCounter.getAndIncrement();
        int x = index * INSTANCE_SPACING;
        int y = 100; // Keep height uniform
        int z = 0;   // Line them up on the Z axis
        
        // Store the tier for this instance index
        activeDungeonTiers.put(index, tier);

        // 4. Select Structure based on Tier
        // You can change these structure names to match what you saved with structure blocks
        String structureName = "lab:starter";
        if (tier == 2) structureName = "lab:starter";
        if (tier == 3) structureName = "lab:starter";

        // 5. Generate the Structure
        // We use console commands to trigger the vanilla Jigsaw engine
        String command = String.format("place structure %s %d %d %d", structureName, x, y, z);
        leader.sendMessage(Component.text("Generating Dungeon Tier " + tier + " (Instance #" + index + ")...").color(NamedTextColor.YELLOW));
        
        // Execute generation in the specific world context if needed, but /place uses player pos or explicit coords.
        // Since /place doesn't support world specifier easily without /execute, we assume command runs in main world 
        // OR we execute relative to a dummy entity. 
        // safest approach: Execute as console, but we might need to be careful about which world it places in.
        // FIX: Use /execute in <world> run place ...
        String fullCommand = String.format("execute in %s run place structure %s %d %d %d", dungeonWorld.getKey().toString(), structureName, x, y, z);
        
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), fullCommand);

        // 6. Teleport the Team (Wait 3 seconds for generation to settle)
        World finalDungeonWorld = dungeonWorld;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location spawnLoc = new Location(finalDungeonWorld, x, y + 1, z);
            
            for (Player p : party) {
                p.teleport(spawnLoc);
                p.setGameMode(GameMode.SURVIVAL);
                p.setHealth(p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
                p.setFoodLevel(20);
                
                // Titles
                String title = tier == 1 ? "The Catacombs" : tier == 2 ? "The Forgotten Keep" : "The Abyss";
                NamedTextColor color = tier == 1 ? NamedTextColor.GREEN : tier == 2 ? NamedTextColor.GOLD : NamedTextColor.RED;
                
                p.sendTitlePart(net.kyori.adventure.title.TitlePart.TITLE, Component.text(title).color(color));
                p.sendTitlePart(net.kyori.adventure.title.TitlePart.SUBTITLE, Component.text("Difficulty: Tier " + tier).color(NamedTextColor.GRAY));
                p.playSound(p.getLocation(), org.bukkit.Sound.EVENT_RAID_HORN, 1f, 1f);
            }
        }, 60L); // 3 second delay (60 ticks)
    }
    
    /**
     * Helper to get the tier of a location (useful for Mob Spawning logic later)
     */
    public int getTierAtLocation(Location loc) {
        if (Math.abs(loc.getZ()) > 500) return 0; // Out of bounds
        // Reverse engineer the index from X coordinate
        int index = (int) Math.round((double) loc.getBlockX() / INSTANCE_SPACING);
        return activeDungeonTiers.getOrDefault(index, 1); // Default to 1 if unknown
    }
}