package com.mmhs.dungeons.generation;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region; // <--- This import is critical
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

        BlockVector3 dims = template.getClipboard().getDimensions();
        
        // Logical Size Swap: If rotated 90 or 270, width becomes length
        int sizeX = (rotation == 90 || rotation == 270) ? dims.getZ() : dims.getX();
        int sizeZ = (rotation == 90 || rotation == 270) ? dims.getX() : dims.getZ();

        // Calculate the bounding box for collision detection
        BlockVector3 min = origin;
        BlockVector3 max = origin.add(sizeX - 1, dims.getY() - 1, sizeZ - 1);
        
        this.occupiedRegion = new CuboidRegion(min, max);
    }

    public DungeonTemplate getTemplate() { return template; }
    public BlockVector3 getOrigin() { return origin; }
    public int getRotation() { return rotation; }
    public Region getOccupiedRegion() { return occupiedRegion; }

    public List<DungeonTemplate.DoorInfo> getRotatedDoors() {
        List<DungeonTemplate.DoorInfo> worldDoors = new ArrayList<>();
        BlockVector3 dims = template.getClipboard().getDimensions();

        for (DungeonTemplate.DoorInfo door : template.getDoors()) {
            // 1. Rotate the door position relative to (0,0)
            BlockVector3 rotPos = rotatePoint(door.position, rotation, dims.getX(), dims.getZ());
            
            // 2. Rotate the direction facing
            BlockVector3 rotDir = rotateDirection(door.direction, rotation);

            // 3. Add to the room's world origin
            worldDoors.add(new DungeonTemplate.DoorInfo(origin.add(rotPos), rotDir));
        }
        return worldDoors;
    }

    // --- INTERNAL MATH HELPERS ---
    
    private BlockVector3 rotatePoint(BlockVector3 p, int rot, int sizeX, int sizeZ) {
        int x = p.getX(); int y = p.getY(); int z = p.getZ();
        switch (rot) {
            case 90:  return BlockVector3.at(-z + sizeZ - 1, y, x);
            case 180: return BlockVector3.at(sizeX - 1 - x, y, sizeZ - 1 - z);
            case 270: return BlockVector3.at(z, y, sizeX - 1 - x);
            default:  return p;
        }
    }

    private BlockVector3 rotateDirection(BlockVector3 d, int rot) {
        int x = d.getX(); int z = d.getZ();
        switch (rot) {
            case 90:  return BlockVector3.at(-z, 0, x);
            case 180: return BlockVector3.at(-x, 0, -z);
            case 270: return BlockVector3.at(z, 0, -x);
            default:  return d;
        }
    }
}