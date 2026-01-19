package com.mmhs.dungeons.generation;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.mmhs.dungeons.generation.DungeonGenerator.DoorPair;

public class PlacedRoom {
    public final RoomTemplate template;
    public final Location worldPosition;
    public final int rotation;
    
    public PlacedRoom(RoomTemplate template, Location worldPosition) {
        this(template, worldPosition, 0);
    }
    
    public PlacedRoom(RoomTemplate template, Location worldPosition, int rotation) {
        this.template = template;
        this.worldPosition = worldPosition;
        this.rotation = rotation;
    }
    
    public List<DoorPair> getWorldDoorPairs() {
        List<DoorPair> worldDoors = new ArrayList<>();
        Location offset = worldPosition.clone().subtract(template.anchor);
        
        for (DoorPair door : template.doorPairs) {
            worldDoors.add(door.offset(offset));
        }
        
        return worldDoors;
    }
    
    public List<Location> getWorldOutline() {
        List<Location> worldOutline = new ArrayList<>();
        Location offset = worldPosition.clone().subtract(template.anchor);
        
        for (Location block : template.outline) {
            worldOutline.add(block.clone().add(offset));
        }
        
        return worldOutline;
    }
    
    // Keep for backward compatibility
    public List<Location> getWorldDoors() {
        List<Location> centers = new ArrayList<>();
        for (DoorPair door : getWorldDoorPairs()) {
            centers.add(door.center);
        }
        return centers;
    }
}
