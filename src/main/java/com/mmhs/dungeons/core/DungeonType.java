package com.mmhs.dungeons.core;

public enum DungeonType {
    MOSS("Moss Dungeon", "moss:main", 1),
    LAB("The Laboratory", "lab:main", 2),
    DEEP_DARK("The Deep Dark", "deep:main", 3),
    END_S_RANK("Void Citadel", "end:s_rank_start", 4);

    public final String displayName;
    public final String structureName;
    public final int tier;

    DungeonType(String displayName, String structureName, int tier) {
        this.displayName = displayName;
        this.structureName = structureName;
        this.tier = tier;
    }
}