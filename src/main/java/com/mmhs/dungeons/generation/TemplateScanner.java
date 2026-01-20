package com.mmhs.dungeons.generation;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import java.util.*;
import java.util.logging.Logger;

public class TemplateScanner {
    private final World world;
    private final Logger logger;
    private final Set<BlockVector3> processedBlocks = new HashSet<>();

    public TemplateScanner(World world, Logger logger) {
        this.world = world;
        this.logger = logger;
    }

    public List<DungeonTemplate> scan(int radius, int roomHeight) {
        logger.info("[Scanner] Starting scan...");
        List<DungeonTemplate> templates = new ArrayList<>();
        processedBlocks.clear();

        List<Location> seeds = findSeeds(radius);
        logger.info("[Scanner] Found " + seeds.size() + " floor seeds (Red Concrete).");

        int count = 0;
        for (Location seed : seeds) {
            BlockVector3 vector = BukkitAdapter.asBlockVector(seed);
            if (processedBlocks.contains(vector)) continue;

            Set<BlockVector3> floorLayout = floodFillFloor(vector);
            if (floorLayout.size() < 10) continue; 

            processedBlocks.addAll(floorLayout);

            try {
                // Assume first room found is starter, or set manually
                boolean isStarter = (count == 0); 
                DungeonTemplate template = captureRoom(floorLayout, count++, roomHeight, isStarter);
                templates.add(template);
                logger.info("[Scanner] ✓ Captured " + template.getId() + " with " + template.getDoors().size() + " doors.");
            } catch (Exception e) {
                logger.warning("[Scanner] Failed to capture room: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return templates;
    }

    private List<Location> findSeeds(int radius) {
        List<Location> seeds = new ArrayList<>();
        // Scanning from -60 to 100 Y level
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -60; y <= 100; y++) {
                   if (world.getBlockAt(x, y, z).getType() == Material.RED_CONCRETE) {
                       seeds.add(new Location(world, x, y, z));
                   }
                }
            }
        }
        return seeds;
    }

    private Set<BlockVector3> floodFillFloor(BlockVector3 start) {
        Set<BlockVector3> floor = new HashSet<>();
        Queue<BlockVector3> queue = new LinkedList<>();
        queue.add(start);
        floor.add(start);

        int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};

        while (!queue.isEmpty()) {
            BlockVector3 curr = queue.poll();
            for (int[] d : dirs) {
                BlockVector3 next = curr.add(d[0], 0, d[1]);
                if (!floor.contains(next) && !processedBlocks.contains(next)) {
                    if (world.getBlockAt(next.getBlockX(), next.getBlockY(), next.getBlockZ())
                            .getType() == Material.RED_CONCRETE) {
                        floor.add(next);
                        queue.add(next);
                    }
                }
            }
        }
        return floor;
    }

    private DungeonTemplate captureRoom(Set<BlockVector3> floor, int id, int height, boolean isStarter) throws Exception {
        // Calculate Bounds
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockVector3 v : floor) {
            minX = Math.min(minX, v.getBlockX());
            maxX = Math.max(maxX, v.getBlockX());
            minY = Math.min(minY, v.getBlockY());
            maxY = Math.max(maxY, v.getBlockY());
            minZ = Math.min(minZ, v.getBlockZ());
            maxZ = Math.max(maxZ, v.getBlockZ());
        }
        maxY += height;

        BlockVector3 minPoint = BlockVector3.at(minX, minY, minZ);
        BlockVector3 maxPoint = BlockVector3.at(maxX, maxY, maxZ);
        CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(world), minPoint, maxPoint);

        // Copy to Clipboard
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(minPoint); 

        try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            ForwardExtentCopy copy = new ForwardExtentCopy(session, region, clipboard, region.getMinimumPoint());
            copy.setCopyingEntities(true);
            copy.setCopyingBiomes(true);
            Operations.complete(copy);
        }

        // --- SCAN DOORS (Gold Blocks Above Border) ---
        List<DungeonTemplate.DoorInfo> doors = new ArrayList<>();
        
        // Scan the volume for Gold Blocks
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) { // Check up to max height
                for (int z = minZ; z <= maxZ; z++) {
                    
                    if (world.getBlockAt(x, y, z).getType() == Material.GOLD_BLOCK) {
                        // Found a gold block. Now determine direction.
                        BlockVector3 goldPos = BlockVector3.at(x, y, z);
                        
                        // Pass the 'floor' set to help verify inside vs outside
                        BlockVector3 dir = getDoorDirection(goldPos, floor);
                        
                        // Only add if valid direction found
                        if (!dir.equals(BlockVector3.ZERO)) {
                            BlockVector3 relPos = goldPos.subtract(minPoint);
                            doors.add(new DungeonTemplate.DoorInfo(relPos, dir));
                        }
                    }
                }
            }
        }

        return new DungeonTemplate("room_" + id, clipboard, doors, isStarter);
    }

    private BlockVector3 getDoorDirection(BlockVector3 goldPos, Set<BlockVector3> floor) {
        int[][] checkDirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        
        for (int[] d : checkDirs) {
            // Check the block neighbor
            BlockVector3 neighbor = goldPos.add(d[0], 0, d[1]);
            
            // Check BENEATH the neighbor (Y-1)
            // Since Gold is ABOVE the border, the floor should be at GoldY - 1.
            BlockVector3 floorCheck = neighbor.withY(goldPos.getBlockY() - 1);
            
            // Logic:
            // IF floorCheck is NOT in the 'floor' set -> It implies "Outside" / "Void"
            // THEN this direction is the way OUT of the room.
            if (!floor.contains(floorCheck)) {
                 return BlockVector3.at(d[0], 0, d[1]);
            }
        }
        
        // Return ZERO if we couldn't determine (e.g., gold block in middle of room)
        return BlockVector3.ZERO; 
    }
}