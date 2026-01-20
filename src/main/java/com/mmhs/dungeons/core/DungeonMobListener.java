package com.mmhs.dungeons.core;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class DungeonMobListener implements Listener {

    // You would set this variable when you generate the dungeon
    public static DungeonDifficulty CURRENT_TIER = DungeonDifficulty.TIER_1;

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster monster)) return;
        
        // Simple check: Is this world a dungeon? 
        // (You might want a more robust check later)
        if (!monster.getWorld().getName().contains("dungeon")) return;

        applyBuffs(monster, CURRENT_TIER);
    }

    private void applyBuffs(Monster monster, DungeonDifficulty tier) {
        // 1. Health
        AttributeInstance maxHealth = monster.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            double newHealth = maxHealth.getBaseValue() * tier.healthMult;
            maxHealth.setBaseValue(newHealth);
            monster.setHealth(newHealth); // Heal to new max
        }

        // 2. Damage
        AttributeInstance damage = monster.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(damage.getBaseValue() * tier.damageMult);
        }
    }
}