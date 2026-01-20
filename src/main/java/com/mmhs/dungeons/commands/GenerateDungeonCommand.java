package com.mmhs.dungeons.commands;

<<<<<<< HEAD
import com.mmhs.dungeons.core.DungeonsPlugin;
import com.mmhs.dungeons.generation.DungeonGenerator;
import com.mmhs.dungeons.generation.DungeonTemplate;
import com.mmhs.dungeons.generation.PlacedRoom;
import com.mmhs.dungeons.generation.RotationMath;
import com.mmhs.dungeons.generation.TemplateScanner;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockTypes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
=======
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
>>>>>>> 6bbb4a113b073abc9b3e889ebf500a416a5b1c3c
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

<<<<<<< HEAD
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenerateDungeonCommand implements CommandExecutor {

    private final DungeonsPlugin plugin;

    public GenerateDungeonCommand(DungeonsPlugin plugin) {
        this.plugin = plugin;
    }

    private void send(CommandSender s, String msg) {
        s.sendMessage(Component.text("[Dungeons] ", NamedTextColor.GREEN)
                .append(Component.text(msg, NamedTextColor.WHITE)));
    }

=======
import com.mmhs.dungeons.core.DungeonsPlugin;
import com.mmhs.dungeons.generation.DungeonGenerator;
import com.mmhs.dungeons.generation.PlacedRoom;
import com.mmhs.dungeons.generation.RoomTemplate;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GenerateDungeonCommand implements CommandExecutor {
    
    private final DungeonsPlugin plugin;
    
    public GenerateDungeonCommand(DungeonsPlugin plugin) {
        this.plugin = plugin;
    }
    
    private void send(CommandSender s, String msg) {
        s.sendMessage(
            Component.text("[Dungeons] ", NamedTextColor.GREEN)
                .append(Component.text(msg, NamedTextColor.WHITE))
        );
    }
    
>>>>>>> 6bbb4a113b073abc9b3e889ebf500a416a5b1c3c
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            send(sender, "Only players can use this command!");
            return true;
        }
<<<<<<< HEAD

        Player player = (Player) sender;

        if (args.length < 1) {
            send(player, "Usage: /gendungeon <numRooms> [radius]");
            return true;
        }

=======
        
        Player player = (Player) sender;
        
        if (args.length < 1) {
            send(sender, "Usage: /gendungeon <numRooms> [radius]");
            return true;
        }
        
>>>>>>> 6bbb4a113b073abc9b3e889ebf500a416a5b1c3c
        int numRooms;
        try {
            numRooms = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            send(sender, "Invalid number of rooms!");
            return true;
        }
<<<<<<< HEAD

        int scanRadius = args.length >= 2 ? Integer.parseInt(args[1]) : 200;

        plugin.getLogger().info("===== DUNGEON GENERATION STARTED =====");
        
        // --- PHASE 1: SCANNING ---
        send(player, "Phase 1: Scanning for room templates...");
        TemplateScanner scanner = new TemplateScanner(player.getWorld(), plugin.getLogger());
        // Set height to 15 to ensure we catch tall ceilings/chandeliers
        List<DungeonTemplate> templates = scanner.scan(scanRadius, 15);

        if (templates.isEmpty()) {
            send(player, "No templates found! Ensure you have Red Concrete floors and Gold Block doors.");
            return true;
        }

        // --- PHASE 2: GENERATION (MATH) ---
        send(player, "Phase 2: Calculating layout for " + numRooms + " rooms...");
        DungeonGenerator generator = new DungeonGenerator(templates, plugin.getLogger());
        List<PlacedRoom> layout = generator.generate(numRooms, player.getLocation());
        
        if (layout.isEmpty()) {
            send(player, "Failed to generate layout.");
            return true;
        }

        // --- PHASE 3: PASTING (WORLDEDIT) ---
        send(player, "Phase 3: Pasting " + layout.size() + " rooms...");
        buildDungeon(layout, player.getWorld());

        send(player, "===== GENERATION COMPLETE =====");
        return true;
    }

    private void buildDungeon(List<PlacedRoom> rooms, org.bukkit.World world) {
        try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            
            Set<BlockVector3> doorLocations = new HashSet<>();

            for (PlacedRoom room : rooms) {
                ClipboardHolder holder = new ClipboardHolder(room.getTemplate().getClipboard());
                BlockVector3 dims = room.getTemplate().getClipboard().getDimensions();

                AffineTransform transform = new AffineTransform().rotateY(room.getRotation());
                BlockVector3 offset = RotationMath.getTranslationOffset(room.getRotation(), dims.getX(), dims.getZ());
                transform = transform.translate(offset.toVector3());

                holder.setTransform(holder.getTransform().combine(transform));

                try {
                    Operation operation = holder
                            .createPaste(session)
                            .to(room.getOrigin())
                            .ignoreAirBlocks(false)
                            .copyEntities(true) // CRITICAL: Ensure we paste entities we copied
                            .build();

                    Operations.complete(operation);
                    
                    for (DungeonTemplate.DoorInfo door : room.getRotatedDoors()) {
                        doorLocations.add(door.position);
                    }

                } catch (WorldEditException e) {
                    plugin.getLogger().severe("Failed to paste room " + room.getTemplate().getId());
                    e.printStackTrace();
                }
            }
            
            // CLEANUP: Remove Gold Blocks
            for (BlockVector3 doorPos : doorLocations) {
                if (session.getBlock(doorPos).getBlockType().equals(BlockTypes.GOLD_BLOCK)) {
                    session.setBlock(doorPos, BlockTypes.AIR.getDefaultState());
                }
            }
            
        } catch (Exception e) {
             plugin.getLogger().severe("Critical WorldEdit error:");
             e.printStackTrace();
        }
    }
}
=======
        
        int scanRadius = args.length >= 2 ? Integer.parseInt(args[1]) : 200;

        send(player, "Scanning for room templates (red concrete + gold block doors)...");

        DungeonGenerator generator = new DungeonGenerator(player.getWorld());

        try {
            // Scan from (0,-60,0) to (+radius, -60, +radius)
            generator.scanForRedConcrete(scanRadius);
            
            List<RoomTemplate> templates = generator.scanRoomTemplates(Material.GOLD_BLOCK);
            
            if (templates.isEmpty()) {
                send(player, "No templates found! Build rooms with red concrete floors and gold block door markers.");
                return true;
            }
            
            // Count statistics
            int totalDoors = 0;
            int totalOutlineBlocks = 0;
            RoomTemplate starter = null;
            
            for (RoomTemplate template : templates) {
                totalDoors += template.doorPairs.size();
                totalOutlineBlocks += template.outline.size();
                if (template.isStarter) {
                    starter = template;
                }
            }
            
            send(player, "=== Template Scan Results ===");
            send(player, "Found " + templates.size() + " room templates");
            send(player, "Total doors: " + totalDoors);
            send(player, "Total outline blocks: " + totalOutlineBlocks);
            send(player, "Starter room: " + (starter != null ? "ID " + starter.id : "None"));
            
            send(player, "Generating layout with " + numRooms + " rooms...");
            List<PlacedRoom> layout = generator.generateLayout(numRooms, player.getLocation(), player);
            
            send(player, "=== Generation Results ===");
            send(player, "Rooms placed: " + layout.size() + " / " + numRooms + " requested");
            
            buildDungeon(layout, player);
            
        } catch (Exception e) {
            send(player, "Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
    
        private void buildDungeon(List<PlacedRoom> rooms, Player player) {
            int blocksPlaced = 0;
            
            for (PlacedRoom room : rooms) {
                // Find bounding box from red concrete outline
                int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
                int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
                int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
                
                for (Location loc : room.template.outline) {
                    minX = Math.min(minX, loc.getBlockX());
                    maxX = Math.max(maxX, loc.getBlockX());
                    minY = Math.min(minY, loc.getBlockY());
                    maxY = Math.max(maxY, loc.getBlockY());
                    minZ = Math.min(minZ, loc.getBlockZ());
                    maxZ = Math.max(maxZ, loc.getBlockZ());
                }
                
                maxY += 10; // Room height
                
                Location anchor = room.template.anchor;
                Location offset = room.worldPosition.clone().subtract(anchor);
                
                // Copy and rotate blocks
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            Location templateLoc = new Location(player.getWorld(), x, y, z);
                            
                            // Rotate around anchor
                            int dx = x - anchor.getBlockX();
                            int dz = z - anchor.getBlockZ();
                            int newDx, newDz;
                            
                            switch (room.rotation % 4) {
                                case 1: // 90°
                                    newDx = -dz;
                                    newDz = dx;
                                    break;
                                case 2: // 180°
                                    newDx = -dx;
                                    newDz = -dz;
                                    break;
                                case 3: // 270°
                                    newDx = dz;
                                    newDz = -dx;
                                    break;
                                default: // 0°
                                    newDx = dx;
                                    newDz = dz;
                                    break;
                            }
                            
                            Location targetLoc = new Location(player.getWorld(),
                                anchor.getBlockX() + newDx + offset.getBlockX(),
                                y + offset.getBlockY(),
                                anchor.getBlockZ() + newDz + offset.getBlockZ());
                            
                            Block source = player.getWorld().getBlockAt(templateLoc);
                            Block target = player.getWorld().getBlockAt(targetLoc);
                            
                            target.setType(source.getType());
                            target.setBlockData(source.getBlockData());
                            blocksPlaced++;
                        }
                    }
                }
            }
            
            send(player, "Placed " + blocksPlaced + " blocks across " + rooms.size() + " rooms.");
        }



}
>>>>>>> 6bbb4a113b073abc9b3e889ebf500a416a5b1c3c
