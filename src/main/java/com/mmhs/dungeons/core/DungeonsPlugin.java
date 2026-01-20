package com.mmhs.dungeons.core;

import com.mmhs.dungeons.commands.GiveDungeonItemsCommand;
import com.mmhs.dungeons.commands.PartyChatCommand;
import com.mmhs.dungeons.commands.PartyCommand;
import com.mmhs.dungeons.commands.PrivateMessageCommand;
import com.mmhs.dungeons.commands.ReplyCommand;
import com.mmhs.dungeons.items.DungeonCraftingListener;
import com.mmhs.dungeons.items.DungeonDurabilityListener;
import com.mmhs.dungeons.items.DungeonItemListener;
import com.mmhs.dungeons.items.DungeonItems;
import com.mmhs.dungeons.items.DungeonRecipes;
import org.bukkit.plugin.java.JavaPlugin;

public class DungeonsPlugin extends JavaPlugin {

    private DungeonItems dungeonItems;
    private PartyManager partyManager;
    private MessageManager messageManager;
    private DungeonInstanceManager instanceManager;

    @Override
    public void onEnable() {
        // 1. Initialize Items & Logic
        this.dungeonItems = new DungeonItems(this);
        
        // Register Crafting Recipes
        new DungeonRecipes(dungeonItems, this).register();

        // 2. Initialize Core Managers
        this.partyManager = new PartyManager();
        this.messageManager = new MessageManager(partyManager);
        this.instanceManager = new DungeonInstanceManager(this, partyManager);

        // 3. Register Commands
        registerCommands();

        // 4. Register Event Listeners
        registerListeners();
        
        getLogger().info("MMHSDungeons has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("DungeonsPlugin disabled!");
    }

    private void registerCommands() {
        // Item Management
        if (getCommand("ditems") != null) {
            getCommand("ditems").setExecutor(new GiveDungeonItemsCommand(dungeonItems));
        }

        // Party System
        if (getCommand("party") != null) {
            getCommand("party").setExecutor(new PartyCommand(partyManager));
        }

        // Chat & Messaging
        if (getCommand("w") != null) getCommand("w").setExecutor(new PrivateMessageCommand(messageManager));
        if (getCommand("r") != null) getCommand("r").setExecutor(new ReplyCommand(messageManager));
        if (getCommand("p") != null) getCommand("p").setExecutor(new PartyChatCommand(messageManager));
    }

    private void registerListeners() {
        // Item Abilities & Mechanics
        getServer().getPluginManager().registerEvents(new DungeonItemListener(dungeonItems, this), this);
        getServer().getPluginManager().registerEvents(new DungeonDurabilityListener(dungeonItems, this), this);
        getServer().getPluginManager().registerEvents(new DungeonCraftingListener(dungeonItems), this);
        getServer().getPluginManager().registerEvents(new DungeonRecipeUnlocker(this), this);
        
        // Dungeon Generation & Spawning
        getServer().getPluginManager().registerEvents(new DungeonMobPopulator(this), this);
        getServer().getPluginManager().registerEvents(new DungeonMobListener(), this); // Handles Tier buffs
        getServer().getPluginManager().registerEvents(new DungeonNPCListener(instanceManager, partyManager), this);
        
        // Player Interaction (Chat, Death, Spectating)
        getServer().getPluginManager().registerEvents(new ChatListener(messageManager, this), this);
        getServer().getPluginManager().registerEvents(new DungeonDeathListener(partyManager, this), this);
    }

    // --- Getters (Optional, useful for API access) ---
    public DungeonItems getDungeonItems() { return dungeonItems; }
    public PartyManager getPartyManager() { return partyManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public DungeonInstanceManager getInstanceManager() { return instanceManager; }
}