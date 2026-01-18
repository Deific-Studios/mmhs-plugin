package com.mmhs.dungeons.commands;

import com.mmhs.dungeons.Dungeon;
import com.mmhs.dungeons.DungeonManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DungeonCommand implements CommandExecutor, TabCompleter {
    private final DungeonManager manager;

    public DungeonCommand(DungeonManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCmd = args[0].toLowerCase();

        switch (subCmd) {
            case "create":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /dungeon create <name>", NamedTextColor.RED));
                    return true;
                }
                Dungeon created = manager.createDungeon(args[1]);
                if (created == null) {
                    sender.sendMessage(Component.text("Dungeon already exists!", NamedTextColor.RED));
                } else {
                    sender.sendMessage(Component.text("Dungeon '" + args[1] + "' created!", NamedTextColor.GREEN));
                    manager.saveAll();
                }
                break;

            case "addspawn":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /dungeon addspawn <name>", NamedTextColor.RED));
                    return true;
                }
                Player p = (Player) sender;
                manager.addSpawn(args[1], p.getLocation());
                sender.sendMessage(Component.text("Spawn point added to dungeon '" + args[1] + "'!", NamedTextColor.GREEN));
                manager.saveAll();
                break;

            case "start":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /dungeon start <name>", NamedTextColor.RED));
                    return true;
                }
                if (manager.startDungeon(args[1])) {
                    sender.sendMessage(Component.text("Dungeon '" + args[1] + "' started!", NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("Failed to start dungeon. Check it exists and has spawn points.", NamedTextColor.RED));
                }
                break;

            case "stop":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /dungeon stop <name>", NamedTextColor.RED));
                    return true;
                }
                if (manager.stopDungeon(args[1])) {
                    sender.sendMessage(Component.text("Dungeon '" + args[1] + "' stopped!", NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("Dungeon is not running!", NamedTextColor.RED));
                }
                break;

            case "list":
                if (manager.listDungeons().isEmpty()) {
                    sender.sendMessage(Component.text("No dungeons found.", NamedTextColor.YELLOW));
                } else {
                    sender.sendMessage(Component.text("Dungeons:", NamedTextColor.GOLD));
                    for (Dungeon d : manager.listDungeons()) {
                        sender.sendMessage(Component.text("  - " + d.getName() + " (" + d.getSpawns().size() + " spawns)", NamedTextColor.YELLOW));
                    }
                }
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== Dungeon Commands ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/dungeon create <name> - Create a new dungeon", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/dungeon addspawn <name> - Add spawn point at your location", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/dungeon start <name> - Start a dungeon", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/dungeon stop <name> - Stop a dungeon", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/dungeon list - List all dungeons", NamedTextColor.YELLOW));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "addspawn", "start", "stop", "list").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("addspawn") ||
                                  args[0].equalsIgnoreCase("start") ||
                                  args[0].equalsIgnoreCase("stop"))) {
            return manager.listDungeons().stream()
                    .map(Dungeon::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}

