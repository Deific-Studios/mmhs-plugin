package com.mmhs.dungeons.core;

import org.bukkit.plugin.java.JavaPlugin;

import com.mmhs.dungeons.commands.DungeonCommand;
import com.mmhs.dungeons.commands.GiveDungeonItemsCommand;
import com.mmhs.dungeons.items.DungeonItemListener;
import com.mmhs.dungeons.items.DungeonItems;

public class DungeonsPlugin extends JavaPlugin {
    private DungeonManager dungeonManager;

    @Override
public void onEnable() {
    DungeonItems dungeonItems = new DungeonItems(this);
    this.dungeonManager = new DungeonManager(this);
    
    // Register commands
    if (this.getCommand("ditems") != null) {
        this.getCommand("ditems").setExecutor(new GiveDungeonItemsCommand(dungeonItems));
    }
    if (this.getCommand("dungeon") != null) {
        this.getCommand("dungeon").setExecutor(new DungeonCommand(this, dungeonManager));
    }
    
        //Register listener
    getServer().getPluginManager().registerEvents(
        new DungeonItemListener(dungeonItems, this),
        this
    );
    
    getLogger().info("MMHSDungeons has been enabled!");
}




    @Override
    public void onDisable() {
        if (dungeonManager != null) {
            dungeonManager.saveAll();
        }
        getLogger().info("DungeonsPlugin disabled!");
    }

    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }
}