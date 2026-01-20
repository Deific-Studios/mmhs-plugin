package com.mmhs.dungeons.core;

public enum DungeonDifficulty {
    TIER_1(1.0, 1.0, 0),
    TIER_2(1.5, 1.2, 1), // 50% more HP, 20% more Dmg
    TIER_3(2.5, 1.5, 3); // 150% more HP, 50% more Dmg, High Loot Bonus

    public final double healthMult;
    public final double damageMult;
    public final int lootLevelBonus;

    DungeonDifficulty(double healthMult, double damageMult, int lootLevelBonus) {
        this.healthMult = healthMult;
        this.damageMult = damageMult;
        this.lootLevelBonus = lootLevelBonus;
    }
}