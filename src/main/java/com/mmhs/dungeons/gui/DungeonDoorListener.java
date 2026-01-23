package com.mmhs.dungeons.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.mmhs.dungeons.core.DungeonInstanceManager;
import com.mmhs.dungeons.core.DungeonType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DungeonDoorListener implements Listener {

    private final Plugin plugin;
    private final DungeonInstanceManager instanceManager;
    private final Random random = new Random();

    private static final Material BARRIER_TYPE = Material.OBSIDIAN;

    public DungeonDoorListener(Plugin plugin, DungeonInstanceManager instanceManager) {
        this.plugin = plugin;
        this.instanceManager = instanceManager;
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != BARRIER_TYPE) return;

        DungeonType type = instanceManager.getTypeAtLocation(clickedBlock.getLocation());
        if (type == null) return; 

        Player player = event.getPlayer();
        
        // Check state of the SPECIFIC clicked block
        int state = 0;
        if (clickedBlock.hasMetadata("barrier_state")) {
            state = clickedBlock.getMetadata("barrier_state").get(0).asInt();
        }

        // --- ACTIVATE THE ROOM ---
        if (state == 0) {
            // 1. Find the whole 4x3 door structure
            List<Block> doorBlocks = findConnectedBarrier(clickedBlock);
            
            // Safety: If it's just a random single block, ignore it
            if (doorBlocks.size() < 6) return; 

            // 2. Lock Message
            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1f, 0.5f);
            player.sendMessage(Component.text("The seal activates! Defeat the guardians!").color(NamedTextColor.DARK_PURPLE));

            // 3. Mark ALL blocks in the door as active (State 1)
            for (Block b : doorBlocks) {
                setBarrierState(b, 1);
            }

            // 4. Spawn Hologram (Try to center it based on average location)
            Location center = getAverageLocation(doorBlocks).add(0.5, 1.5, 0.5);
            ArmorStand hologram = spawnHologram(center);
            
            // 5. Spawn Mobs BEHIND player
            List<LivingEntity> mobs = spawnRoomGuardians(player, type);
            
            // 6. Monitor Battle
            startBattleMonitor(doorBlocks, hologram, mobs);
        }
        // --- ALREADY ACTIVE ---
        else if (state == 1) {
            player.sendMessage(Component.text("The door remains sealed...").color(NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1f, 1f);
        }
    }

    private void startBattleMonitor(List<Block> doorBlocks, ArmorStand hologram, List<LivingEntity> mobs) {
        new BukkitRunnable() {
            @Override
            public void run() {
                mobs.removeIf(e -> e.isDead() || !e.isValid());
                int remaining = mobs.size();

                if (remaining > 0) {
                    hologram.customName(Component.text("⛔ " + remaining + " Enemies Remain ⛔").color(NamedTextColor.RED));
                } else {
                    // VICTORY
                    hologram.remove();
                    
                    // Play sound at the door location
                    if (!doorBlocks.isEmpty()) {
                        doorBlocks.get(0).getWorld().playSound(doorBlocks.get(0).getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1f, 0.5f);
                    }

                    collapseWall(doorBlocks);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    // --- STRUCTURAL LOGIC ---

    /**
     * Scans for connected Obsidian blocks to find the whole 4x3 wall.
     */
    private List<Block> findConnectedBarrier(Block start) {
        List<Block> result = new ArrayList<>();
        List<Block> toCheck = new ArrayList<>();
        toCheck.add(start);
        
        // Simple flood fill with a limit (max 20 blocks to prevent lag)
        int safetyLimit = 0;
        
        while (!toCheck.isEmpty() && safetyLimit < 20) {
            Block current = toCheck.remove(0);
            
            if (!result.contains(current) && current.getType() == BARRIER_TYPE) {
                result.add(current);
                safetyLimit++;
                
                // Add neighbors (Up, Down, Left, Right, Forward, Back)
                // We check a small radius to catch the whole 4x3
                checkNeighbor(current, 1, 0, 0, result, toCheck);
                checkNeighbor(current, -1, 0, 0, result, toCheck);
                checkNeighbor(current, 0, 1, 0, result, toCheck);
                checkNeighbor(current, 0, -1, 0, result, toCheck);
                checkNeighbor(current, 0, 0, 1, result, toCheck);
                checkNeighbor(current, 0, 0, -1, result, toCheck);
            }
        }
        return result;
    }

    private void checkNeighbor(Block origin, int x, int y, int z, List<Block> result, List<Block> toCheck) {
        Block neighbor = origin.getRelative(x, y, z);
        if (neighbor.getType() == BARRIER_TYPE && !result.contains(neighbor) && !toCheck.contains(neighbor)) {
            toCheck.add(neighbor);
        }
    }

    private void collapseWall(List<Block> blocks) {
        for (Block b : blocks) {
            b.setType(Material.AIR);
            
            // Visual falling block
            FallingBlock fb = b.getWorld().spawnFallingBlock(b.getLocation().add(0.5, 0, 0.5), BARRIER_TYPE.createBlockData());
            fb.setDropItem(false);
            
            // Fling outward from center
            fb.setVelocity(new Vector((random.nextDouble()-0.5)*0.4, 0.2, (random.nextDouble()-0.5)*0.4));
        }
    }
    
    private Location getAverageLocation(List<Block> blocks) {
        if (blocks.isEmpty()) return null;
        double x = 0, y = 0, z = 0;
        for (Block b : blocks) {
            x += b.getX();
            y += b.getY();
            z += b.getZ();
        }
        return new Location(blocks.get(0).getWorld(), x / blocks.size(), y / blocks.size(), z / blocks.size());
    }

    // --- MOBS & SPAWNING ---

    private List<LivingEntity> spawnRoomGuardians(Player player, DungeonType type) {
        List<LivingEntity> spawned = new ArrayList<>();
        int mobCount = 4 + random.nextInt(3); // 4-6 mobs
        Location playerLoc = player.getLocation();
        
        for (int i = 0; i < mobCount; i++) {
            // Spawn in circle around player
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = 4 + random.nextDouble() * 4;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location spawnLoc = playerLoc.clone().add(x, 1, z);
            
            if (spawnLoc.getBlock().getType() != Material.AIR) continue;
            
            playerLoc.getWorld().spawnParticle(Particle.PORTAL, spawnLoc, 15, 0.5, 1, 0.5, 0.1);
            EntityType entityType = (type == DungeonType.END_S_RANK) ? EntityType.ENDERMAN : 
                                   (type == DungeonType.LAB ? EntityType.ZOMBIE_VILLAGER : EntityType.ZOMBIE);
            
            LivingEntity mob = (LivingEntity) playerLoc.getWorld().spawnEntity(spawnLoc, entityType);
            equipMob(mob, type.tier);
            spawned.add(mob);
        }
        return spawned;
    }

    private ArmorStand spawnHologram(Location loc) {
        ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        as.setGravity(false);
        as.setVisible(false);
        as.setCustomNameVisible(true);
        as.setMarker(true);
        as.customName(Component.text("Seal Active").color(NamedTextColor.DARK_PURPLE));
        return as;
    }

    private void equipMob(LivingEntity mob, int tier) {
        if (mob.getEquipment() == null) return;
        Material weapon = (tier >= 3) ? Material.DIAMOND_SWORD : (tier == 2) ? Material.IRON_SWORD : Material.STONE_SWORD;
        Material armor = (tier >= 3) ? Material.IRON_CHESTPLATE : (tier == 2) ? Material.CHAINMAIL_CHESTPLATE : Material.LEATHER_CHESTPLATE;
        
        ItemStack w = new ItemStack(weapon);
        if (tier > 1) w.addEnchantment(Enchantment.SHARPNESS, tier);
        mob.getEquipment().setItemInMainHand(w);
        mob.getEquipment().setChestplate(new ItemStack(armor));
        mob.getEquipment().setItemInMainHandDropChance(0f);
        mob.getEquipment().setChestplateDropChance(0f);
    }

    private void setBarrierState(Block b, int state) {
        b.setMetadata("barrier_state", new FixedMetadataValue(plugin, state));
    }
}