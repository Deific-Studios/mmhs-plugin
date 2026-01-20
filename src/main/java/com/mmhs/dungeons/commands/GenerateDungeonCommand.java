package com.mmhs.dungeons.commands;

import com.mmhs.dungeons.core.DungeonsPlugin;
import com.mmhs.dungeons.generation.*;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenerateDungeonCommand implements CommandExecutor {
    private final DungeonsPlugin plugin;
    public GenerateDungeonCommand(DungeonsPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (args.length < 1) { player.sendMessage("Usage: /gd <rooms>"); return true; }

        int numRooms = Integer.parseInt(args[0]);
        int scanRadius = 100;

        TemplateScanner scanner = new TemplateScanner(player.getWorld(), plugin.getLogger());
        List<DungeonTemplate> templates = scanner.scan(scanRadius, 10);
        
        if (templates.isEmpty()) {
            player.sendMessage(Component.text("No templates found!", NamedTextColor.RED));
            return true;
        }

        DungeonGenerator generator = new DungeonGenerator(templates, plugin.getLogger());
        List<PlacedRoom> layout = generator.generate(numRooms, player.getLocation());
        
        if (layout.isEmpty()) {
            player.sendMessage(Component.text("Generation failed.", NamedTextColor.RED));
            return true;
        }

        buildDungeon(layout, player.getWorld());
        player.sendMessage(Component.text("Generated " + layout.size() + " rooms!", NamedTextColor.GREEN));
        return true;
    }

    private void buildDungeon(List<PlacedRoom> rooms, org.bukkit.World world) {
        try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            Set<BlockVector3> doorLocations = new HashSet<>();

            for (PlacedRoom room : rooms) {
                ClipboardHolder holder = new ClipboardHolder(room.getTemplate().getClipboard());
                BlockVector3 dims = room.getTemplate().getClipboard().getDimensions();
                
                // 1. Rotation
                AffineTransform transform = new AffineTransform().rotateY(room.getRotation());
                
                // 2. Correction Offset (Push back to positive coordinates)
                BlockVector3 offset = BlockVector3.ZERO;
                if (room.getRotation() == 90) offset = BlockVector3.at(dims.getZ() - 1, 0, 0);
                if (room.getRotation() == 180) offset = BlockVector3.at(dims.getX() - 1, 0, dims.getZ() - 1);
                if (room.getRotation() == 270) offset = BlockVector3.at(0, 0, dims.getX() - 1);
                
                transform = transform.translate(offset.toVector3());
                holder.setTransform(holder.getTransform().combine(transform));

                // 3. Paste
                Operation operation = holder.createPaste(session)
                        .to(room.getOrigin())
                        .ignoreAirBlocks(false)
                        .copyEntities(true)
                        .build();
                Operations.complete(operation);

                // Collect doors to delete later
                for (DungeonTemplate.DoorInfo d : room.getRotatedDoors()) {
                    doorLocations.add(d.position);
                }
            }

            // 4. Delete Gold Blocks
            for (BlockVector3 pos : doorLocations) {
                // Only delete if it is actually a gold block (prevents deleting accidental walls)
                if (session.getBlock(pos).getBlockType().equals(BlockTypes.GOLD_BLOCK)) {
                    session.setBlock(pos, BlockTypes.AIR.getDefaultState());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}