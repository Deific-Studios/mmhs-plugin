package com.mmhs.dungeons.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class RamBarCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    // Keep track of active bars so we can toggle them off
    private final HashMap<UUID, BukkitRunnable> activeTasks = new HashMap<>();
    private final HashMap<UUID, BossBar> activeBars = new HashMap<>();

    public RamBarCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is for players only.");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        // TOGGLE OFF
        if (activeBars.containsKey(uuid)) {
            activeBars.get(uuid).removeAll(); // Remove bar from screen
            activeBars.remove(uuid);
            
            if (activeTasks.containsKey(uuid)) {
                activeTasks.get(uuid).cancel(); // Stop the loop
                activeTasks.remove(uuid);
            }
            
            player.sendMessage(ChatColor.RED + "Server Monitor Disabled.");
            return true;
        }

        // TOGGLE ON
        BossBar bossBar = Bukkit.createBossBar("Calculating...", BarColor.GREEN, BarStyle.SEGMENTED_20);
        bossBar.addPlayer(player);
        activeBars.put(uuid, bossBar);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                // If player leaves, kill the task
                if (!player.isOnline()) {
                    this.cancel();
                    activeBars.remove(uuid);
                    activeTasks.remove(uuid);
                    return;
                }

                // --- MEMORY CALCULATION ---
                Runtime runtime = Runtime.getRuntime();
                long maxMemory = runtime.maxMemory();   // Max RAM allocated to Java
                long totalMemory = runtime.totalMemory(); // Current RAM allocated
                long freeMemory = runtime.freeMemory();   // Unused RAM within current allocation
                
                long usedMemory = totalMemory - freeMemory;

                // Convert bytes to MB
                long usedMB = usedMemory / 1024 / 1024;
                long maxMB = maxMemory / 1024 / 1024;

                // Calculate percentage (0.0 to 1.0)
                double percentage = (double) usedMemory / (double) maxMemory;
                // Clamp it just in case
                if (percentage > 1.0) percentage = 1.0;
                if (percentage < 0.0) percentage = 0.0;

                // Update Visuals
                bossBar.setProgress(percentage);
                bossBar.setTitle(ChatColor.AQUA + "" + ChatColor.BOLD + "RAM USAGE: " + 
                                 ChatColor.WHITE + usedMB + "MB / " + maxMB + "MB " + 
                                 ChatColor.GRAY + String.format("(%.1f%%)", percentage * 100));

                // Dynamic Coloring
                if (percentage > 0.90) {
                    bossBar.setColor(BarColor.RED); // Critical
                } else if (percentage > 0.75) {
                    bossBar.setColor(BarColor.YELLOW); // Warning
                } else {
                    bossBar.setColor(BarColor.GREEN); // Good
                }
            }
        };

        // Run every 20 ticks (1 second)
        task.runTaskTimer(plugin, 0L, 20L);
        activeTasks.put(uuid, task);

        player.sendMessage(ChatColor.GREEN + "Server Monitor Enabled.");
        return true;
    }
}