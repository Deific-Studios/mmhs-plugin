package com.mmhs.dungeons.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;


import java.util.Random;

public class DungeonMobPopulator {

    private final Plugin plugin;
    private final DungeonInstanceManager instanceManager;
    private final Random random = new Random();

    public DungeonMobPopulator(Plugin plugin, DungeonInstanceManager instanceManager) {
        this.plugin = plugin;
        this.instanceManager = instanceManager;
        startSpawner();
    }

    private void startSpawner() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    DungeonType type = instanceManager.getTypeAtLocation(p.getLocation());
                    if (type == null) continue;

                    if (p.getWorld().getNearbyEntities(p.getLocation(), 20, 20, 20).size() < 8) {
                        spawnMob(p, type);
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 100L); // 5 seconds
    }

    private void spawnMob(Player p, DungeonType type) {
        Location loc = p.getLocation().add((random.nextDouble() * 20) - 10, 1, (random.nextDouble() * 20) - 10);
        if (loc.getBlock().getType() != Material.AIR) return;

        EntityType entityType = getEntityForDungeon(type);
        LivingEntity mob = (LivingEntity) p.getWorld().spawnEntity(loc, entityType);

        // Equip Gear
        equipMob(mob, type.tier);
    }

    private EntityType getEntityForDungeon(DungeonType type) {
        switch (type) {
            case LAB: return random.nextBoolean() ? EntityType.ZOMBIE_VILLAGER : EntityType.DROWNED;
            case DEEP_DARK: return EntityType.WARDEN; // Careful with this! Maybe generic sculk mobs
            case END_S_RANK: return EntityType.ENDERMAN;
            default: return random.nextBoolean() ? EntityType.ZOMBIE : EntityType.SKELETON;
        }
    }

    private void equipMob(LivingEntity mob, int tier) {
        if (mob.getEquipment() == null) return;

        Material weaponMat = Material.STONE_SWORD;
        Material armorMat = Material.LEATHER_CHESTPLATE;

        if (tier == 2) { // Lab
            weaponMat = Material.IRON_SWORD;
            armorMat = Material.CHAINMAIL_CHESTPLATE;
        } else if (tier >= 3) { // End/Deep Dark
            weaponMat = Material.DIAMOND_SWORD;
            armorMat = Material.IRON_CHESTPLATE;
        }

        ItemStack weapon = new ItemStack(weaponMat);
        ItemStack armor = new ItemStack(armorMat);

        // Random Enchants
        if (random.nextInt(100) < (tier * 20)) { // Tier 1 = 20%, Tier 3 = 60% chance
            weapon.addEnchantment(Enchantment.SHARPNESS, tier);
        }
        if (random.nextInt(100) < (tier * 20)) {
            armor.addEnchantment(Enchantment.PROTECTION, tier);
        }

        mob.getEquipment().setItemInMainHand(weapon);
        mob.getEquipment().setChestplate(armor);
        mob.getEquipment().setItemInMainHandDropChance(0f); // Don't drop OP loot
        mob.getEquipment().setChestplateDropChance(0f);
    }
}