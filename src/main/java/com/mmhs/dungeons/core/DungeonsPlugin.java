package com.mmhs.dungeons.core;

import com.mmhs.dungeons.commands.DungeonCommand;
import com.mmhs.dungeons.commands.GiveDungeonItemsCommand;
import com.mmhs.dungeons.items.CrownManager;
import com.mmhs.dungeons.items.DungeonItemListener;
import com.mmhs.dungeons.items.DungeonItems;
import org.bukkit.plugin.java.JavaPlugin;

public class DungeonsPlugin extends JavaPlugin {
    private DungeonManager dungeonManager;

    @Override
public void onEnable() {
    DungeonItems dungeonItems = new DungeonItems(this);
    DungeonManager dungeonManager = new DungeonManager(this);
    
    // Register commands
    this.getCommand("ditems").setExecutor(new GiveDungeonItemsCommand(dungeonItems));
    this.getCommand("dungeon").setExecutor(new DungeonCommand(this, dungeonManager));
    
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