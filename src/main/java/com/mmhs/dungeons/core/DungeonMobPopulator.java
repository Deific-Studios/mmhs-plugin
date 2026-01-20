package com.mmhs.dungeons.core;


import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Random;

public class DungeonMobPopulator implements Listener {

    private final Plugin plugin;
    private final Random random = new Random();
    private final NamespacedKey processedKey;

    public DungeonMobPopulator(Plugin plugin) {
        this.plugin = plugin;
        this.processedKey = new NamespacedKey(plugin, "marker_processed");
        
        // Initial scan for reloads (in case server restarts while dungeon is loaded)
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                scanChunk(chunk);
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // Delay slightly to let entities load in fully
        Bukkit.getScheduler().runTaskLater(plugin, () -> scanChunk(event.getChunk()), 5L);
    }

    private void scanChunk(Chunk chunk) {
        if (!chunk.getWorld().getName().equals("world")) return; // Update with your dungeon world name

        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof ArmorStand stand) {
                
                // Check if we already processed this marker (prevents double spawning)
                if (stand.getPersistentDataContainer().has(processedKey, PersistentDataType.BYTE)) continue;
                
                String name = stand.getCustomName();
                if (name != null && name.startsWith("MOB:")) {
                    processMarker(stand, name);
                }
            }
        }
    }

    private void processMarker(ArmorStand marker, String data) {
        // Format: "MOB:TYPE:CHANCE" (e.g., "MOB:ZOMBIE:50")
        String[] parts = data.split(":");
        if (parts.length < 2) return;

        String mobType = parts[1].toUpperCase();
        int chance = parts.length > 2 ? Integer.parseInt(parts[2]) : 100;

        // 1. Roll for spawn
        if (random.nextInt(100) < chance) {
            spawnDungeonMob(marker, mobType);
        }

        // 2. Delete the marker so it doesn't spawn again
        marker.remove();
    }

    private void spawnDungeonMob(ArmorStand loc, String type) {
        World world = loc.getWorld();
        
        try {
            EntityType entityType = EntityType.valueOf(type);
            Entity spawned = world.spawnEntity(loc.getLocation(), entityType);

            if (spawned instanceof LivingEntity mob) {
                // --- CUSTOMIZE MOBS HERE ---
                applyDungeonStats(mob);
            }
            
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid mob type in structure: " + type);
        }
    }

    private void applyDungeonStats(LivingEntity mob) {
        // Example: Buff Zombies based on your Difficulty Tier (Simple version)
        if (mob instanceof Zombie) {
            mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40); // Double Health
            mob.setHealth(40);
            
            // Give them a weapon?
            mob.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        }
        
        // You can hook into your DungeonDifficulty enum here!
    }
}