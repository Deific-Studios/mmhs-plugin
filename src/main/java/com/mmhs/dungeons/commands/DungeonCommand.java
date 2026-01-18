package com.mmhs.dungeons.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mmhs.dungeons.core.Dungeon;
import com.mmhs.dungeons.core.DungeonManager;
import com.mmhs.dungeons.core.DungeonsPlugin;

public class DungeonCommand implements CommandExecutor {
    private final DungeonsPlugin plugin;
    private final DungeonManager manager;

    public DungeonCommand(DungeonsPlugin plugin, DungeonManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    private void send(CommandSender s, String msg) {
        s.sendMessage(ChatColor.GREEN + "[Dungeons] " + ChatColor.RESET + msg);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            send(sender, "Usage: /dungeon <create|addspawn|start|stop|list>");
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create" -> {
                if (args.length < 2) { send(sender, "Usage: /dungeon create <name>"); return true; }
                String name = args[1];
                Dungeon d = manager.createDungeon(name);
                if (d == null) { send(sender, "A dungeon with that name already exists."); }
                else { send(sender, "Created dungeon '" + name + "'."); manager.saveAll(); }
                return true;
            }
            case "addspawn" -> {
                if (!(sender instanceof Player)) { send(sender, "Only players can add spawns."); return true; }
                if (args.length < 2) { send(sender, "Usage: /dungeon addspawn <name>"); return true; }
                String name = args[1];
                Dungeon d = manager.getDungeon(name);
                if (d == null) { send(sender, "No such dungeon."); return true; }
                Player p = (Player) sender;
                Location loc = p.getLocation();
                manager.addSpawn(name, loc);
                manager.saveAll();
                send(sender, "Added spawn for dungeon '" + name + "' at your location.");
                return true;
            }
            case "start" -> {
                if (args.length < 2) { send(sender, "Usage: /dungeon start <name>"); return true; }
                String name = args[1];
                boolean ok = manager.startDungeon(name);
                if (!ok) send(sender, "Could not start dungeon (missing/empty/already running)."); else send(sender, "Dungeon started.");
                return true;
            }
            case "stop" -> {
                if (args.length < 2) { send(sender, "Usage: /dungeon stop <name>"); return true; }
                String name = args[1];
                boolean ok = manager.stopDungeon(name);
                if (!ok) send(sender, "Dungeon not running."); else send(sender, "Dungeon stopped.");
                return true;
            }
            case "list" -> {
                StringBuilder sb = new StringBuilder();
                sb.append("Dungeons: ");
                boolean first = true;
                for (Dungeon d : manager.listDungeons()) {
                    if (!first) sb.append(", "); first = false;
                    sb.append(d.getName());
                }
                send(sender, sb.toString());
                return true;
            }
            default -> {
                send(sender, "Unknown subcommand.");
                return true;
            }
        }
    }
}

