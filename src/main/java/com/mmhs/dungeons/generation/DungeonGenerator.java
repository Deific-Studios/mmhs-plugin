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
        logger.info("[Gen] --- STARTING GENERATION (" + numRooms + " rooms) ---");
        List<PlacedRoom> layout = new ArrayList<>();
        
        // 1. Setup Starter
        DungeonTemplate starter = templates.stream()
                .filter(DungeonTemplate::isStarter)
                .findFirst()
                .orElse(templates.get(0));

        logger.info("[Gen] Selected Starter Template: " + starter.getId());
        
        BlockVector3 startVec = BlockVector3.at(startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ());
        PlacedRoom startRoom = new PlacedRoom(starter, startVec, 0);
        layout.add(startRoom);

        List<DungeonTemplate.DoorInfo> openDoors = new ArrayList<>(startRoom.getRotatedDoors());
        logger.info("[Gen] Starter placed. Open doors: " + openDoors.size());

        int attempts = 0;
        int maxAttempts = numRooms * 200; // High limit to prevent instant fail

        // 2. Main Loop
        while (layout.size() < numRooms && !openDoors.isEmpty() && attempts < maxAttempts) {
            attempts++;
            
            // Pick a random door
            int doorIndex = new Random().nextInt(openDoors.size());
            DungeonTemplate.DoorInfo targetDoor = openDoors.get(doorIndex);
            
            // logger.info("[Gen] Attempt " + attempts + ": Trying to extend from door at " + targetDoor.position);

            boolean placed = false;
            List<DungeonTemplate> shuffledTemplates = new ArrayList<>(templates);
            Collections.shuffle(shuffledTemplates);

            for (DungeonTemplate candidate : shuffledTemplates) {
                if (placed) break;
                if (candidate.isStarter()) continue;

                // Try Rotations
                List<Integer> rotations = Arrays.asList(0, 90, 180, 270);
                Collections.shuffle(rotations);

                for (int rot : rotations) {
                    // Create dummy to check local doors
                    PlacedRoom dummy = new PlacedRoom(candidate, BlockVector3.ZERO, rot);
                    
                    for (DungeonTemplate.DoorInfo candDoor : dummy.getRotatedDoors()) {
                        
                        // CHECK 1: DIRECTIONS
                        if (!areDirectionsOpposite(targetDoor.direction, candDoor.direction)) {
                            // logger.info("   -> Fail: Directions not opposite (" + targetDoor.direction + " vs " + candDoor.direction + ")");
                            continue;
                        }

                        // CHECK 2: COLLISION
                        BlockVector3 newOrigin = targetDoor.position.subtract(candDoor.position);
                        PlacedRoom proposal = new PlacedRoom(candidate, newOrigin, rot);

                        if (!checkCollision(proposal, layout)) {
                            logger.info("[Gen] SUCCESS! Placed " + candidate.getId() + " at " + newOrigin + " (Rot: " + rot + ")");
                            layout.add(proposal);
                            
                            // Update Door List
                            openDoors.remove(doorIndex); // Remove used connection
                            
                            // Add new doors
                            for (DungeonTemplate.DoorInfo newDoor : proposal.getRotatedDoors()) {
                                if (!newDoor.position.equals(targetDoor.position)) {
                                    openDoors.add(newDoor);
                                }
                            }
                            placed = true;
                            break;
                        } else {
                            // logger.info("   -> Fail: Collision detected for " + candidate.getId());
                        }
                    }
                    if (placed) break;
                }
            }

            if (!placed) {
                // logger.info("[Gen] Could not fit ANY room on door at " + targetDoor.position + ". Marking dead end.");
                openDoors.remove(doorIndex);
            }
        }
        
        logger.info("[Gen] Finished. Total Rooms: " + layout.size());
        if (layout.size() < numRooms) {
            logger.warning("[Gen] FAILED to reach target room count! (Ran out of space or valid connections)");
        }
        
        return layout;
    }

    private boolean checkCollision(PlacedRoom proposal, List<PlacedRoom> existing) {
        Region regA = proposal.getOccupiedRegion();
        // Shrink A slightly to allow "touching" walls
        BlockVector3 minA = regA.getMinimumPoint().add(1, 1, 1);
        BlockVector3 maxA = regA.getMaximumPoint().subtract(1, 1, 1);
        
        for (PlacedRoom room : existing) {
            Region regB = room.getOccupiedRegion();
            // Simple AABB Intersection
            BlockVector3 minB = regB.getMinimumPoint();
            BlockVector3 maxB = regB.getMaximumPoint();

            boolean intersect = minA.getX() <= maxB.getX() && maxA.getX() >= minB.getX() &&
                                minA.getY() <= maxB.getY() && maxA.getY() >= minB.getY() &&
                                minA.getZ() <= maxB.getZ() && maxA.getZ() >= minB.getZ();
            
            if (intersect) return true;
        }
        return false;
    }

    private boolean areDirectionsOpposite(BlockVector3 d1, BlockVector3 d2) {
        return d1.add(d2).equals(BlockVector3.ZERO);
    }
}