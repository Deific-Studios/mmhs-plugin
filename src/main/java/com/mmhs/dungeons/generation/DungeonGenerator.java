package com.mmhs.dungeons.generation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DungeonGenerator {
    private final World world;
    private final List<Location> redConcretePositions;
    private final Set<Location> indexedRooms;
    private final List<RoomTemplate> rooms;
    
    // Inner class for door pairs
    public static class DoorPair {
        public final Location block1;
        public final Location block2;
        public final Location center;
        
        public DoorPair(Location block1, Location block2) {
            this.block1 = block1;
            this.block2 = block2;
            this.center = block1.clone().add(
                (block2.getX() - block1.getX()) * 0.5,
                (block2.getY() - block1.getY()) * 0.5,
                (block2.getZ() - block1.getZ()) * 0.5
            );
        }
        
        public DoorPair rotate(Location anchor, int rotation) {
            Location rotated1 = rotateLocation(block1, anchor, rotation);
            Location rotated2 = rotateLocation(block2, anchor, rotation);
            return new DoorPair(rotated1, rotated2);
        }
        
        private static Location rotateLocation(Location loc, Location anchor, int rotation) {
            int dx = loc.getBlockX() - anchor.getBlockX();
            int dz = loc.getBlockZ() - anchor.getBlockZ();
            
            int newDx, newDz;
            switch (rotation % 4) {
                case 1:
                    newDx = -dz;
                    newDz = dx;
                    break;
                case 2:
                    newDx = -dx;
                    newDz = -dz;
                    break;
                case 3:
                    newDx = dz;
                    newDz = -dx;
                    break;
                default:
                    newDx = dx;
                    newDz = dz;
                    break;
            }
            
            return new Location(loc.getWorld(), 
                anchor.getBlockX() + newDx, 
                loc.getBlockY(), 
                anchor.getBlockZ() + newDz);
        }
        
        public DoorPair offset(Location offset) {
            return new DoorPair(
                block1.clone().add(offset),
                block2.clone().add(offset)
            );
        }
    }
    
    public DungeonGenerator(World world) {
        this.world = world;
        this.redConcretePositions = new ArrayList<>();
        this.indexedRooms = new HashSet<>();
        this.rooms = new ArrayList<>();
    }
    
    public void scanForRedConcrete(int radius) {
        int y = -60;
        
        for (int x = 0; x <= radius; x++) {
            for (int z = 0; z <= radius; z++) {
                Block block = world.getBlockAt(x, y, z);
                if (block.getType() == Material.RED_CONCRETE) {
                    redConcretePositions.add(block.getLocation());
                }
            }
        }
    }
    
    private Location findClosestRedConcrete(Location origin) {
        Location closest = null;
        double minDist = Double.MAX_VALUE;
        
        for (Location pos : redConcretePositions) {
            if (indexedRooms.contains(pos)) continue;
            
            double dist = manhattanDistance(pos, origin);
            if (dist < minDist) {
                minDist = dist;
                closest = pos;
            }
        }
        
        return closest;
    }
    
    private List<Location> traceRoomOutline(Location start) {
        List<Location> outline = new ArrayList<>();
        Set<Location> visited = new HashSet<>();
        Queue<Location> queue = new LinkedList<>();
        
        queue.add(start);
        visited.add(start);
        
        int maxSize = 500;
        
        while (!queue.isEmpty() && outline.size() < maxSize) {
            Location current = queue.poll();
            outline.add(current);
            
            int[][] directions = {{1,0,0}, {-1,0,0}, {0,1,0}, {0,-1,0}, {0,0,1}, {0,0,-1}};
            
            for (int[] dir : directions) {
                Location next = current.clone().add(dir[0], dir[1], dir[2]);
                
                if (redConcretePositions.contains(next) && 
                    !visited.contains(next) && 
                    !indexedRooms.contains(next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        
        indexedRooms.addAll(visited);
        return outline;
    }
    
    /**
     * Find door pairs (2 adjacent gold blocks) above red concrete
     */
    private List<DoorPair> findDoorPairsAboveOutline(List<Location> outline, Material indicatorMaterial) {
        List<DoorPair> doorPairs = new ArrayList<>();
        Set<Location> processedIndicators = new HashSet<>();
        
        for (Location pos : outline) {
            for (int yOffset = 1; yOffset <= 3; yOffset++) {
                Location above = pos.clone().add(0, yOffset, 0);
                Block block = world.getBlockAt(above);
                
                if (block.getType() == indicatorMaterial && !processedIndicators.contains(above)) {
                    int[][] horizontal = {{1,0,0}, {-1,0,0}, {0,0,1}, {0,0,-1}};
                    
                    for (int[] dir : horizontal) {
                        Location adjacent = above.clone().add(dir[0], dir[1], dir[2]);
                        Block adjacentBlock = world.getBlockAt(adjacent);
                        
                        if (adjacentBlock.getType() == indicatorMaterial && 
                            !processedIndicators.contains(adjacent)) {
                            
                            // Create door pair with floor positions
                            Location floor1 = pos;
                            Location floor2 = pos.clone().add(dir[0], 0, dir[2]);
                            
                            DoorPair doorPair = new DoorPair(floor1, floor2);
                            doorPairs.add(doorPair);
                            
                            processedIndicators.add(above);
                            processedIndicators.add(adjacent);
                            break;
                        }
                    }
                    break;
                }
            }
        }
        
        return doorPairs;
    }
    
    public List<RoomTemplate> scanRoomTemplates(Material doorIndicator) {
        if (redConcretePositions.isEmpty()) {
            throw new IllegalStateException("No red concrete markers found!");
        }
        
        Location origin = new Location(world, 0, 0, 0);
        
        while (true) {
            Location currentAnchor = findClosestRedConcrete(origin);
            
            if (currentAnchor == null) {
                break;
            }
            
            List<Location> outline = traceRoomOutline(currentAnchor);
            
            if (outline.isEmpty()) break;
            
            List<DoorPair> doorPairs = findDoorPairsAboveOutline(outline, doorIndicator);
            
            RoomTemplate room = new RoomTemplate(rooms.size(), currentAnchor, outline, doorPairs, false);
            rooms.add(room);
        }
        
        if (!rooms.isEmpty()) {
            RoomTemplate closest = rooms.get(0);
            double minDist = Double.MAX_VALUE;
            
            for (RoomTemplate room : rooms) {
                double dist = manhattanDistance(room.anchor, origin);
                if (dist < minDist) {
                    minDist = dist;
                    closest = room;
                }
            }
            
            rooms.remove(closest);
            List<DoorPair> starterDoors = closest.doorPairs.size() > 1 ? 
                closest.doorPairs.subList(0, 1) : closest.doorPairs;
            RoomTemplate starter = new RoomTemplate(closest.id, closest.anchor, 
                closest.outline, starterDoors, true);
            rooms.add(closest.id, starter);
        }
        
        return rooms;
    }
    
    /**
     * Check if two door pairs align exactly
     */
    private boolean doorsAlign(DoorPair exitDoor, DoorPair entranceDoor) {
        return exitDoor.block1.getBlockX() == entranceDoor.block1.getBlockX() &&
               exitDoor.block1.getBlockY() == entranceDoor.block1.getBlockY() &&
               exitDoor.block1.getBlockZ() == entranceDoor.block1.getBlockZ() &&
               exitDoor.block2.getBlockX() == entranceDoor.block2.getBlockX() &&
               exitDoor.block2.getBlockY() == entranceDoor.block2.getBlockY() &&
               exitDoor.block2.getBlockZ() == entranceDoor.block2.getBlockZ();
    }
    
    public List<PlacedRoom> generateLayout(int numRooms, Location startLocation, Player player) {
        if (rooms.isEmpty()) {
            throw new IllegalStateException("No room templates available!");
        }
        
        List<PlacedRoom> placedRooms = new ArrayList<>();
        List<DoorPair> availableDoors = new ArrayList<>();
        Map<DoorPair, PlacedRoom> doorToRoomMap = new HashMap<>();
        Random random = new Random();
        
        // Place starter room at 0 degrees
        RoomTemplate starter = rooms.stream()
            .filter(r -> r.isStarter)
            .findFirst()
            .orElse(rooms.get(0));
        
        PlacedRoom placedStarter = new PlacedRoom(starter, startLocation, 0);
        placedRooms.add(placedStarter);
        
        List<DoorPair> starterDoors = placedStarter.getWorldDoorPairs();
        availableDoors.addAll(starterDoors);
        for (DoorPair door : starterDoors) {
            doorToRoomMap.put(door, placedStarter);
        }
        
        List<RoomTemplate> availableTemplates = new ArrayList<>(rooms);
        
        for (int i = 1; i < numRooms; i++) {
            if (availableDoors.isEmpty()) {
                if (player != null) {
                    player.sendMessage(Component.text("[Dungeons] ", NamedTextColor.YELLOW)
                        .append(Component.text("No more available doors!", NamedTextColor.WHITE)));
                }
                break;
            }
            
            boolean placed = false;
            int attempts = 0;
            int maxAttempts = 1000;
            
            if (availableTemplates.isEmpty()) {
                availableTemplates.addAll(rooms);
            }
            
            while (!placed && attempts < maxAttempts) {
                attempts++;
                
                RoomTemplate baseTemplate = availableTemplates.get(random.nextInt(availableTemplates.size()));
                if (baseTemplate.doorPairs.isEmpty()) continue;
                
                DoorPair targetDoor = availableDoors.get(random.nextInt(availableDoors.size()));
                
                // Try all 4 rotations
                for (int rotation = 0; rotation < 4; rotation++) {
                    // Rotate template first
                    RoomTemplate rotatedTemplate = rotateRoomTemplate(baseTemplate, rotation);
                    
                    // Try each door in the rotated template
                    for (DoorPair roomDoor : rotatedTemplate.doorPairs) {
                        // Calculate room position to align door centers
                        double dx = roomDoor.center.getX() - rotatedTemplate.anchor.getX();
                        double dy = roomDoor.center.getY() - rotatedTemplate.anchor.getY();
                        double dz = roomDoor.center.getZ() - rotatedTemplate.anchor.getZ();
                        
                        Location roomPosition = new Location(
                            world,
                            targetDoor.center.getX() - dx,
                            targetDoor.center.getY() - dy,
                            targetDoor.center.getZ() - dz
                        );
                        
                        // Calculate where the room's door would be in world space
                        Location offsetToWorld = roomPosition.clone().subtract(rotatedTemplate.anchor);
                        DoorPair worldRoomDoor = new DoorPair(
                            roomDoor.block1.clone().add(offsetToWorld),
                            roomDoor.block2.clone().add(offsetToWorld)
                        );
                        
                        // Check exact alignment
                        if (!doorsAlign(targetDoor, worldRoomDoor)) {
                            continue;
                        }
                        
                        // Check collision
                        if (!checkRoomCollision(rotatedTemplate, roomPosition, placedRooms, null)) {
                            PlacedRoom newRoom = new PlacedRoom(rotatedTemplate, roomPosition, rotation);
                            placedRooms.add(newRoom);
                            
                            List<DoorPair> newDoors = newRoom.getWorldDoorPairs();
                            availableDoors.addAll(newDoors);
                            for (DoorPair door : newDoors) {
                                doorToRoomMap.put(door, newRoom);
                            }
                            
                            availableTemplates.remove(baseTemplate);
                            placed = true;
                            
                            if (player != null) {
                                String rotMsg = rotation > 0 ? " (" + (rotation * 90) + "°)" : "";
                                player.sendMessage(Component.text("[Dungeons] ", NamedTextColor.GREEN)
                                    .append(Component.text("Placed room " + i + "/" + numRooms + rotMsg + 
                                        " (attempt " + attempts + ")", NamedTextColor.WHITE)));
                            }
                            break;
                        }
                    }
                    
                    if (placed) break;
                }
                
                if (placed) break;
            }
            
            if (!placed) {
                if (player != null) {
                    player.sendMessage(Component.text("[Dungeons] ", NamedTextColor.RED)
                        .append(Component.text("Failed to place room " + i + " after " + 
                            attempts + " attempts", NamedTextColor.WHITE)));
                }
                break;
            }
        }
        
        return placedRooms;
    }

    
    private RoomTemplate rotateRoomTemplate(RoomTemplate template, int rotation) {
        if (rotation == 0) return template;
        
        List<Location> rotatedOutline = new ArrayList<>();
        for (Location loc : template.outline) {
            rotatedOutline.add(DoorPair.rotateLocation(loc, template.anchor, rotation));
        }
        
        List<DoorPair> rotatedDoors = new ArrayList<>();
        for (DoorPair door : template.doorPairs) {
            rotatedDoors.add(door.rotate(template.anchor, rotation));
        }
        
        return new RoomTemplate(template.id, template.anchor, rotatedOutline, 
            rotatedDoors, template.isStarter);
    }
    
    private boolean checkRoomCollision(RoomTemplate template, Location position, 
                                    List<PlacedRoom> placedRooms, Player player) {
        for (PlacedRoom existing : placedRooms) {
            if (checkSingleRoomCollision(template, position, existing, player)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean checkSingleRoomCollision(RoomTemplate template, Location position, 
                                            PlacedRoom existing, Player player) {
        Location offset1 = position.clone().subtract(template.anchor);
        Location offset2 = existing.worldPosition.clone().subtract(existing.template.anchor);
        
        int minX1 = Integer.MAX_VALUE, maxX1 = Integer.MIN_VALUE;
        int minY1 = Integer.MAX_VALUE, maxY1 = Integer.MIN_VALUE;
        int minZ1 = Integer.MAX_VALUE, maxZ1 = Integer.MIN_VALUE;
        
        for (Location loc : template.outline) {
            minX1 = Math.min(minX1, loc.getBlockX());
            maxX1 = Math.max(maxX1, loc.getBlockX());
            minY1 = Math.min(minY1, loc.getBlockY());
            maxY1 = Math.max(maxY1, loc.getBlockY());
            minZ1 = Math.min(minZ1, loc.getBlockZ());
            maxZ1 = Math.max(maxZ1, loc.getBlockZ());
        }
        maxY1 += 10;
        
        int minX2 = Integer.MAX_VALUE, maxX2 = Integer.MIN_VALUE;
        int minY2 = Integer.MAX_VALUE, maxY2 = Integer.MIN_VALUE;
        int minZ2 = Integer.MAX_VALUE, maxZ2 = Integer.MIN_VALUE;
        
        for (Location loc : existing.template.outline) {
            minX2 = Math.min(minX2, loc.getBlockX());
            maxX2 = Math.max(maxX2, loc.getBlockX());
            minY2 = Math.min(minY2, loc.getBlockY());
            maxY2 = Math.max(maxY2, loc.getBlockY());
            minZ2 = Math.min(minZ2, loc.getBlockZ());
            maxZ2 = Math.max(maxZ2, loc.getBlockZ());
        }
        maxY2 += 10;
        
        int worldMinX1 = minX1 + offset1.getBlockX();
        int worldMaxX1 = maxX1 + offset1.getBlockX();
        int worldMinY1 = minY1 + offset1.getBlockY();
        int worldMaxY1 = maxY1 + offset1.getBlockY();
        int worldMinZ1 = minZ1 + offset1.getBlockZ();
        int worldMaxZ1 = maxZ1 + offset1.getBlockZ();
        
        int worldMinX2 = minX2 + offset2.getBlockX();
        int worldMaxX2 = maxX2 + offset2.getBlockX();
        int worldMinY2 = minY2 + offset2.getBlockY();
        int worldMaxY2 = maxY2 + offset2.getBlockY();
        int worldMinZ2 = minZ2 + offset2.getBlockZ();
        int worldMaxZ2 = maxZ2 + offset2.getBlockZ();
        
        boolean overlaps = !(worldMaxX1 < worldMinX2 || worldMinX1 > worldMaxX2 ||
                            worldMaxY1 < worldMinY2 || worldMinY1 > worldMaxY2 ||
                            worldMaxZ1 < worldMinZ2 || worldMinZ1 > worldMaxZ2);
        
        if (!overlaps) {
            return false;
        }
        
        for (int x = Math.max(worldMinX1, worldMinX2); x <= Math.min(worldMaxX1, worldMaxX2); x++) {
            for (int y = Math.max(worldMinY1, worldMinY2); y <= Math.min(worldMaxY1, worldMaxY2); y++) {
                for (int z = Math.max(worldMinZ1, worldMinZ2); z <= Math.min(worldMaxZ1, worldMaxZ2); z++) {
                    Location loc = new Location(world, x, y, z);
                    Block block = world.getBlockAt(loc);
                    Material type = block.getType();
                    
                    if (type != Material.AIR && 
                        type != Material.RED_CONCRETE && 
                        type != Material.GOLD_BLOCK) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private double manhattanDistance(Location a, Location b) {
        return Math.abs(a.getX() - b.getX()) + 
               Math.abs(a.getY() - b.getY()) + 
               Math.abs(a.getZ() - b.getZ());
    }
    
    public List<RoomTemplate> getRoomTemplates() {
        return rooms;
    }
}
