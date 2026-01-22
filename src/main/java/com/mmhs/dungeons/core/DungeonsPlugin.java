package com.mmhs.dungeons.core;

import com.mmhs.dungeons.commands.GiveDungeonItemsCommand;
import com.mmhs.dungeons.commands.PartyChatCommand;
import com.mmhs.dungeons.commands.PartyCommand;
import com.mmhs.dungeons.commands.PrivateMessageCommand;
import com.mmhs.dungeons.commands.ReplyCommand;
import com.mmhs.dungeons.commands.SummonBossCommand;
import com.mmhs.dungeons.gui.DungeonGUIListener;
import com.mmhs.dungeons.items.DungeonCraftingListener;
import com.mmhs.dungeons.items.DungeonDurabilityListener;
import com.mmhs.dungeons.items.DungeonItemListener;
import com.mmhs.dungeons.items.DungeonItems;
import com.mmhs.dungeons.items.DungeonRecipes;
import com.mmhs.dungeons.mobs.BossAbilityListener;
import com.mmhs.dungeons.mobs.BossDeathListener;
import com.mmhs.dungeons.mobs.DungeonBossManager;

import org.bukkit.plugin.java.JavaPlugin;

public class DungeonsPlugin extends JavaPlugin {

    // --- CLASS VARIABLES (Fields) ---
    // These must be declared here so they are visible to all methods below.
    private DungeonItems dungeonItems;
    private PartyManager partyManager;
    private MessageManager messageManager;
    private DungeonInstanceManager instanceManager;
    private DungeonBossManager bossManager;
    private ProgressionManager progressionManager; // <--- The missing symbol

    @Override
    public void onEnable() {
        // 1. Initialize Items (Must be first)
        this.dungeonItems = new DungeonItems(this);
        new DungeonRecipes(dungeonItems, this).register();

        // 2. Initialize Managers
        // We initialize the class variables here.
        this.progressionManager = new ProgressionManager(this);
        this.partyManager = new PartyManager();
        this.messageManager = new MessageManager(partyManager);
        this.instanceManager = new DungeonInstanceManager(this, partyManager);
        
        // Boss Manager needs dungeonItems
        this.bossManager = new DungeonBossManager(this, dungeonItems);

        // 3. Register Commands
        registerCommands();

        // 4. Register Listeners
        registerListeners();
        
        getLogger().info("MMHSDungeons has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("DungeonsPlugin disabled!");
    }

    private void registerCommands() {
        if (getCommand("ditems") != null) getCommand("ditems").setExecutor(new GiveDungeonItemsCommand(dungeonItems));
        if (getCommand("party") != null) getCommand("party").setExecutor(new PartyCommand(partyManager));
        if (getCommand("summonboss") != null) getCommand("summonboss").setExecutor(new SummonBossCommand(bossManager));
        if (getCommand("w") != null) getCommand("w").setExecutor(new PrivateMessageCommand(messageManager));
        if (getCommand("r") != null) getCommand("r").setExecutor(new ReplyCommand(messageManager));
        if (getCommand("p") != null) getCommand("p").setExecutor(new PartyChatCommand(messageManager));
    }

    private void registerListeners() {
        // Item Mechanics
        getServer().getPluginManager().registerEvents(new DungeonItemListener(dungeonItems, this), this);
        getServer().getPluginManager().registerEvents(new DungeonDurabilityListener(dungeonItems, this), this);
        getServer().getPluginManager().registerEvents(new DungeonCraftingListener(dungeonItems), this);
        getServer().getPluginManager().registerEvents(new DungeonRecipeUnlocker(this), this);

        // Dungeon Generation & Spawning
        getServer().getPluginManager().registerEvents(new DungeonMobPopulator(this), this);
        getServer().getPluginManager().registerEvents(new DungeonMobListener(), this); 
        
        // NPC & GUI
        // Pass the progressionManager variable we defined at the top
        getServer().getPluginManager().registerEvents(new DungeonNPCListener(instanceManager, partyManager, progressionManager), this);
        getServer().getPluginManager().registerEvents(new DungeonGUIListener(this, instanceManager, progressionManager), this);

        // Player Interactions
        getServer().getPluginManager().registerEvents(new ChatListener(messageManager, this), this);
        getServer().getPluginManager().registerEvents(new DungeonDeathListener(partyManager, this), this);
        
        // Boss Mechanics
        getServer().getPluginManager().registerEvents(new BossAbilityListener(this, bossManager), this);
        // This line caused your previous error; now it sees progressionManager correctly
        getServer().getPluginManager().registerEvents(new BossDeathListener(bossManager, dungeonItems, progressionManager), this);
    }

    // --- Getters ---
    public DungeonItems getDungeonItems() { return dungeonItems; }
    public PartyManager getPartyManager() { return partyManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public DungeonInstanceManager getInstanceManager() { return instanceManager; }
    public DungeonBossManager getBossManager() { return bossManager; }
    public ProgressionManager getProgressionManager() { return progressionManager; }
}