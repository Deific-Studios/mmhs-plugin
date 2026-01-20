package com.mmhs.dungeons.generation;

import java.util.List;

import org.bukkit.Location;

import com.mmhs.dungeons.generation.DungeonGenerator.DoorPair;

public class RoomTemplate {
    public final int id;
    public final Location anchor;
    public final List<Location> outline;
    public final List<DoorPair> doorPairs;
    public final boolean isStarter;
    
    public RoomTemplate(int id, Location anchor, List<Location> outline, 
                       List<DoorPair> doorPairs, boolean isStarter) {
        this.id = id;
        this.anchor = anchor;
        this.outline = outline;
        this.doorPairs = doorPairs;
        this.isStarter = isStarter;
    }
}
