package com.mmhs.dungeons.generation;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;
import java.util.*;
import java.util.logging.Logger;

public class DungeonGenerator {
    private final List<DungeonTemplate> templates;
    private final Logger logger;

    public DungeonGenerator(List<DungeonTemplate> templates, Logger logger) {
        this.templates = templates;
        this.logger = logger;
    }

    public List<PlacedRoom> generate(int numRooms, Location startLocation) {
        List<PlacedRoom> layout = new ArrayList<>();
        
        // 1. Setup Starter
        DungeonTemplate starter = templates.stream()
                .filter(DungeonTemplate::isStarter)
                .findFirst()
                .orElse(templates.get(0));

        BlockVector3 startVec = BlockVector3.at(startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ());
        PlacedRoom startRoom = new PlacedRoom(starter, startVec, 0);
        layout.add(startRoom);

        // List of available door connections
        List<DungeonTemplate.DoorInfo> openDoors = new ArrayList<>(startRoom.getRotatedDoors());

        int attempts = 0;
        int maxAttempts = numRooms * 50; 

        // 2. Main Generation Loop
        while (layout.size() < numRooms && !openDoors.isEmpty() && attempts < maxAttempts) {
            attempts++;
            
            // Pick a random open door to extend from
            int doorIndex = new Random().nextInt(openDoors.size());
            DungeonTemplate.DoorInfo targetDoor = openDoors.get(doorIndex);
            
            boolean roomPlaced = false;
            Collections.shuffle(templates);

            for (DungeonTemplate candidate : templates) {
                if (roomPlaced) break;
                if (candidate.isStarter()) continue; 

                // Try rotations
                List<Integer> rotations = Arrays.asList(0, 90, 180, 270);
                Collections.shuffle(rotations);

                for (int rot : rotations) {
                    List<DungeonTemplate.DoorInfo> candidateDoors = getRelativeRotatedDoors(candidate, rot);
                    
                    for (DungeonTemplate.DoorInfo candDoor : candidateDoors) {
                        // Check if doors face each other
                        if (!areDirectionsOpposite(targetDoor.direction, candDoor.direction)) continue;

                        // Calculate Origin so doors overlap perfectly
                        BlockVector3 newOrigin = targetDoor.position.subtract(candDoor.position);

                        PlacedRoom proposal = new PlacedRoom(candidate, newOrigin, rot);
                        
                        if (!checkCollision(proposal, layout)) {
                            layout.add(proposal);
                            
                            // Remove the used door from the open list
                            openDoors.remove(doorIndex);

                            // Add the new room's doors (except the one connecting back)
                            for (DungeonTemplate.DoorInfo newDoor : proposal.getRotatedDoors()) {
                                if (!newDoor.position.equals(targetDoor.position)) {
                                    openDoors.add(newDoor);
                                }
                            }
                            
                            roomPlaced = true;
                            break; 
                        }
                    }
                    if (roomPlaced) break;
                }
            }
        }
        
        if (attempts >= maxAttempts) {
            logger.warning("Generation stopped: Max attempts reached.");
        }
        
        return layout;
    }

    private boolean checkCollision(PlacedRoom proposal, List<PlacedRoom> existing) {
        Region regA = proposal.getOccupiedRegion();
        for (PlacedRoom room : existing) {
            if (regionsIntersect(regA, room.getOccupiedRegion())) {
                return true; 
            }
        }
        return false;
    }

    private boolean regionsIntersect(Region a, Region b) {
        BlockVector3 minA = a.getMinimumPoint();
        BlockVector3 maxA = a.getMaximumPoint();
        BlockVector3 minB = b.getMinimumPoint();
        BlockVector3 maxB = b.getMaximumPoint();

        // Shrink check by 1 block to allow walls to touch
        return minA.getX() < maxB.getX() && maxA.getX() > minB.getX() &&
               minA.getY() < maxB.getY() && maxA.getY() > minB.getY() &&
               minA.getZ() < maxB.getZ() && maxA.getZ() > minB.getZ();
    }

    private boolean areDirectionsOpposite(BlockVector3 d1, BlockVector3 d2) {
        return d1.add(d2).equals(BlockVector3.ZERO);
    }

    private List<DungeonTemplate.DoorInfo> getRelativeRotatedDoors(DungeonTemplate temp, int rot) {
        List<DungeonTemplate.DoorInfo> list = new ArrayList<>();
        
        BlockVector3 dims = temp.getClipboard().getDimensions();
        int sizeX = dims.getX();
        int sizeZ = dims.getZ();

        for (DungeonTemplate.DoorInfo d : temp.getDoors()) {
            // Use RotationMath to fix offset issues
            BlockVector3 newPos = RotationMath.rotate(d.position, rot, sizeX, sizeZ);
            
            // Rotate Direction
            BlockVector3 newDir;
            int dx = d.direction.getX();
            int dz = d.direction.getZ();
            
            if (rot == 90) newDir = BlockVector3.at(-dz, 0, dx);
            else if (rot == 180) newDir = BlockVector3.at(-dx, 0, -dz);
            else if (rot == 270) newDir = BlockVector3.at(dz, 0, -dx);
            else newDir = d.direction;

            list.add(new DungeonTemplate.DoorInfo(newPos, newDir));
        }
        return list;
    }
}