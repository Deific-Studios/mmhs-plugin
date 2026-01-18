package com.mmhs.dungeons;

import com.mmhs.dungeons.commands.DungeonCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class DungeonsPlugin extends JavaPlugin {
    private DungeonManager dungeonManager;

    @Override
    public void onEnable() {
        dungeonManager = new DungeonManager(this);

        // Register commands
        DungeonCommand cmd = new DungeonCommand(dungeonManager);
        getCommand("dungeon").setExecutor(cmd);
        getCommand("dungeon").setTabCompleter(cmd);

        getLogger().info("DungeonsPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (dungeonManager != null) {
            dungeonManager.saveAll();
        }
        getLogger().info("DungeonsPlugin has been disabled!");
    }

    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }
}

