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
    private final AtomicInteger instanceCounter = new AtomicInteger(0);
    
    // NEW: Maps the Instance Index -> DungeonType (instead of just Integer)
    private final Map<Integer, DungeonType> activeDungeons = new HashMap<>();

    private static final int INSTANCE_SPACING = 5000; 
    private static final String DUNGEON_WORLD_NAME = "dungeon_world"; 

    public DungeonInstanceManager(Plugin plugin, PartyManager partyManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
    }

    // UPDATED: Now takes DungeonType instead of int tier
    public void startDungeon(Player leader, DungeonType type) {
        List<Player> party = partyManager.getOnlineMembers(leader);
        World world = Bukkit.getWorld(DUNGEON_WORLD_NAME);
        
        if (world == null) {
            leader.sendMessage(Component.text("Error: World '" + DUNGEON_WORLD_NAME + "' not loaded!").color(NamedTextColor.RED));
            return;
        }

        // 1. Calculate Location
        int index = instanceCounter.getAndIncrement();
        int x = index * INSTANCE_SPACING;
        int z = 0;
        int y = 64;

        // Store the type so MobPopulator knows what to spawn
        activeDungeons.put(index, type);

        // 2. Generate Structure
        // Uses /execute in <world> run place structure ...
        String cmd = String.format("execute in %s run place structure %s %d %d %d", 
            DUNGEON_WORLD_NAME, type.structureName, x, y, z);
        
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        leader.sendMessage(Component.text("Generating " + type.displayName + "...").color(NamedTextColor.YELLOW));

        // 3. Teleport Sequence (Delayed)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location spawn = new Location(world, x, y + 1, z);
            for (Player p : party) {
                p.teleport(spawn);
                p.setGameMode(GameMode.SURVIVAL);
                p.setHealth(p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
                
                // Titles
                p.sendTitlePart(net.kyori.adventure.title.TitlePart.TITLE, Component.text(type.displayName).color(NamedTextColor.GOLD));
                p.sendTitlePart(net.kyori.adventure.title.TitlePart.SUBTITLE, Component.text("Tier " + type.tier + " Instance").color(NamedTextColor.GRAY));
            }
        }, 100L); // 5 seconds delay
    }

    // NEW METHOD: Returns DungeonType (Fixes your error)
    public DungeonType getTypeAtLocation(Location loc) {
        if (loc.getWorld() == null || !loc.getWorld().getName().equals(DUNGEON_WORLD_NAME)) return null;
        
        // Reverse engineer index from X coordinate
        int index = (int) Math.round((double) loc.getBlockX() / INSTANCE_SPACING);
        
        return activeDungeons.get(index);
    }
}