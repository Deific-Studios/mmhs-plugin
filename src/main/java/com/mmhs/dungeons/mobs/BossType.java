package com.mmhs.dungeons.mobs;

import com.mmhs.dungeons.items.DungeonItems;
import org.bukkit.entity.EntityType;

public enum BossType {

    // Tier 1: The Wind Boss (Fast, Knockback)
    TEMPEST_CONSTRUCT(
        "Tempest Construct", 
        EntityType.ZOMBIE, 
        150.0, // HP
        8.0,   // Dmg
        DungeonItems.Id.MAT_STORM
    ),
    

    STONE_COLOSSUS(
        "Stone Colossus", 
        EntityType.IRON_GOLEM, 
        400.0, 
        15.0,
        DungeonItems.Id.MAT_GOLEM
    ),
    
    // The Void Boss (Magic, Withering)
    KNIGHT_OF_THE_VOID(
        "Void Knight", 
        EntityType.WITHER_SKELETON, 
        500.0, 
        25.0,
        DungeonItems.Id.MAT_SHADOW
    ),

    // B-Rank / A-Rank
    THE_FORGOTTEN_KING(
        "The Forgotten King",
        EntityType.EVOKER,
        250.0,
        12.0,
        DungeonItems.Id.RELIC_FORGOTTEN
    ),

    // S-Rank (Massive HP)
    THE_ABYSSAL_OVERLORD(
        "Abyssal Overlord",
        EntityType.WITHER,
        1000.0,
        30.0,
        DungeonItems.Id.CORE_ABYSSAL
    ),

    // Elementalist
    THE_STORMBOUND_KNIGHT(
        "Stormbound Knight",
        EntityType.STRAY,
        350.0,
        18.0,
        DungeonItems.Id.SHARD_RELIC
    );

    public final String displayName;
    public final EntityType type;
    public final double health;
    public final double damage;
    public final DungeonItems.Id dropId;

    BossType(String displayName, EntityType type, double health, double damage, DungeonItems.Id dropId) {
        this.displayName = displayName;
        this.type = type;
        this.health = health;
        this.damage = damage;
        this.dropId = dropId;
    }
}