package com.mmhs.dungeons.commands;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            send(sender, "Only players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 1) {
            send(sender, "Usage: /gendungeon <numRooms> [radius]");
            return true;
        }
        
        int numRooms;
        try {
            numRooms = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            send(sender, "Invalid number of rooms!");
            return true;
        }
        
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
