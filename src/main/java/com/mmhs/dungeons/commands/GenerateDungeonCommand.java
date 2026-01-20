package com.mmhs.dungeons.commands;

import com.mmhs.dungeons.core.DungeonsPlugin;
import com.mmhs.dungeons.generation.DungeonGenerator;
import com.mmhs.dungeons.generation.DungeonTemplate;
import com.mmhs.dungeons.generation.PlacedRoom;
import com.mmhs.dungeons.generation.TemplateScanner;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.math.BlockVector3;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class GenerateDungeonCommand implements CommandExecutor {

    private final DungeonsPlugin plugin;

    public GenerateDungeonCommand(DungeonsPlugin plugin) {
        this.plugin = plugin;
    }

    private void send(CommandSender s, String msg) {
        s.sendMessage(Component.text("[Dungeons] ", NamedTextColor.GREEN)
                .append(Component.text(msg, NamedTextColor.WHITE)));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            send(sender, "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            send(player, "Usage: /gendungeon <numRooms> [radius]");
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

        plugin.getLogger().info("===== DUNGEON GENERATION STARTED =====");
        
        // 1. SCANNING (Sync - creating templates)
        send(player, "Phase 1: Scanning for room templates...");
        TemplateScanner scanner = new TemplateScanner(player.getWorld(), plugin.getLogger());
        // Scan radius, assume room height is 10 for now
        List<DungeonTemplate> templates = scanner.scan(scanRadius, 10);

        if (templates.isEmpty()) {
            send(player, "No templates found! Make sure you have Red Concrete floors and Gold Block doors.");
            return true;
        }

        // 2. GENERATION (Calculation only - fast)
        send(player, "Phase 2: Calculating layout for " + numRooms + " rooms...");
        DungeonGenerator generator = new DungeonGenerator(templates, plugin.getLogger());
        List<PlacedRoom> layout = generator.generate(numRooms, player.getLocation());
        
        if (layout.isEmpty()) {
            send(player, "Failed to generate a layout. Check console for collision errors.");
            return true;
        }

        // 3. BUILDING (Pasting Schematics)
        send(player, "Phase 3: Pasting " + layout.size() + " rooms...");
        buildDungeon(layout, player.getWorld());

        send(player, "===== GENERATION COMPLETE =====");
        return true;
    }

    /**
     * The new WorldEdit-based build method
     */
    // ... imports ...
// (Keep your imports, add java.util.HashSet and Set)

    private void buildDungeon(List<PlacedRoom> rooms, org.bukkit.World world) {
        try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            
            // Track where we placed doors so we can clear them later
            Set<BlockVector3> doorLocations = new HashSet<>();

            for (PlacedRoom room : rooms) {
                ClipboardHolder holder = new ClipboardHolder(room.getTemplate().getClipboard());

                // Apply the rotation (stored in PlacedRoom)
                AffineTransform transform = new AffineTransform().rotateY(room.getRotation());
                holder.setTransform(holder.getTransform().combine(transform));

                try {
                    Operation operation = holder
                            .createPaste(session)
                            .to(room.getOrigin())
                            .ignoreAirBlocks(false) 
                            .build();
                    Operations.complete(operation);
                    
                    // Collect world positions of all doors in this room
                    for(DungeonTemplate.DoorInfo door : room.getRotatedDoors()) {
                        doorLocations.add(door.position);
                    }

                } catch (WorldEditException e) {
                    e.printStackTrace();
                }
            }
            
            // === CLEANUP PHASE ===
            // 1. Iterate through all potential door locations
            // 2. If it is a Gold Block, delete it (AIR)
            // This handles the "Overlapping" issue perfectly.
            
            for (BlockVector3 doorPos : doorLocations) {
                // Check if it's currently a gold block (it should be, from the paste)
                if (session.getBlock(doorPos).getBlockType().getMaterial().toString().contains("GOLD_BLOCK")) {
                     // Set to AIR
                    session.setBlock(doorPos, com.sk89q.worldedit.world.block.BlockTypes.AIR.getDefaultState());
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Critical WorldEdit error:");
            e.printStackTrace();
        }
    }