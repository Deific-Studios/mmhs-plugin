package com.mmhs.dungeons.generation;

<<<<<<< HEAD
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import java.util.ArrayList;
import java.util.List;

public class PlacedRoom {
    private final DungeonTemplate template;
    private final BlockVector3 origin;
    private final int rotation;
    private final Region occupiedRegion;

    public PlacedRoom(DungeonTemplate template, BlockVector3 origin, int rotation) {
        this.template = template;
        this.origin = origin;
        this.rotation = rotation;

        // --- FIXED COLLISION LOGIC ---
        // Instead of transforming the region object, we just calculate the new dimensions manually.
        BlockVector3 dims = template.getClipboard().getDimensions();
        
        int sizeX = dims.getX();
        int sizeY = dims.getY();
        int sizeZ = dims.getZ();

        // If rotated 90 or 270 degrees, swap Width (X) and Length (Z)
        if (rotation == 90 || rotation == 270) {
            sizeX = dims.getZ();
            sizeZ = dims.getX();
        }

        // Calculate the world bounding box
        // Since our clipboard origin is at the bottom-min corner (set in Scanner),
        // the region extends from 'origin' to 'origin + size'.
        BlockVector3 min = origin;
        BlockVector3 max = origin.add(sizeX - 1, sizeY - 1, sizeZ - 1);
        
        this.occupiedRegion = new CuboidRegion(min, max);
    }

    public DungeonTemplate getTemplate() { return template; }
    public BlockVector3 getOrigin() { return origin; }
    public int getRotation() { return rotation; }
    public Region getOccupiedRegion() { return occupiedRegion; }

    public List<DungeonTemplate.DoorInfo> getRotatedDoors() {
        List<DungeonTemplate.DoorInfo> worldDoors = new ArrayList<>();
        // Create the transform for the door vectors
        AffineTransform transform = new AffineTransform().rotateY(rotation);

        for (DungeonTemplate.DoorInfo door : template.getDoors()) {
            // Apply rotation to the relative position
            BlockVector3 rotPos = transform.apply(door.position.toVector3()).toBlockPoint();
            // Apply rotation to the direction
            BlockVector3 rotDir = transform.apply(door.direction.toVector3()).toBlockPoint();
            
            // Add to the room's world origin
            BlockVector3 worldPos = origin.add(rotPos);
            
            worldDoors.add(new DungeonTemplate.DoorInfo(worldPos, rotDir));
        }
        return worldDoors;
    }
}
=======
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
>>>>>>> 6bbb4a113b073abc9b3e889ebf500a416a5b1c3c
