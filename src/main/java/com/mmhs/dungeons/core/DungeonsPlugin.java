package com.mmhs.dungeons.core;

import org.bukkit.plugin.java.JavaPlugin;

// Commands
import com.mmhs.dungeons.commands.GiveDungeonItemsCommand;
import com.mmhs.dungeons.commands.PartyChatCommand;
import com.mmhs.dungeons.commands.PartyCommand;
import com.mmhs.dungeons.commands.PrivateMessageCommand;
import com.mmhs.dungeons.commands.RamBarCommand;
import com.mmhs.dungeons.commands.ReplyCommand;
import com.mmhs.dungeons.commands.SummonBossCommand;
import com.mmhs.dungeons.commands.SpawnDMCommand;

// Listeners & Managers
import com.mmhs.dungeons.gui.DungeonGUIListener;
import com.mmhs.dungeons.gui.DungeonNPCListener;
import com.mmhs.dungeons.items.DungeonCraftingListener;
import com.mmhs.dungeons.items.DungeonDurabilityListener;
import com.mmhs.dungeons.items.DungeonItemListener;
import com.mmhs.dungeons.items.DungeonItems;
import com.mmhs.dungeons.items.DungeonRecipes;
import com.mmhs.dungeons.mobs.BossAbilityListener;
import com.mmhs.dungeons.mobs.BossDeathListener;
import com.mmhs.dungeons.mobs.DungeonBossManager;

public class DungeonsPlugin extends JavaPlugin {

    // --- CLASS VARIABLES ---
    private DungeonItems dungeonItems;
    private PartyManager partyManager;
    private MessageManager messageManager;
    private DungeonInstanceManager instanceManager;
    private DungeonBossManager bossManager;
    private ProgressionManager progressionManager;

    @Override
    public void onEnable() {
        // 1. Initialize Items (Must be first)
        // We pass 'this' because your DungeonItems expects the plugin instance
        this.dungeonItems = new DungeonItems(this);
        new DungeonRecipes(dungeonItems, this).register();

        // 2. Initialize Managers
        this.progressionManager = new ProgressionManager(this);
        this.partyManager = new PartyManager();
        this.messageManager = new MessageManager(partyManager);
        this.instanceManager = new DungeonInstanceManager(this, partyManager);
        this.bossManager = new DungeonBossManager(this, dungeonItems);

        // 3. Register Game Mechanics (Mobs & Loot)
        // FIXED: Now passes 'instanceManager' to the populator
        new DungeonMobPopulator(this, instanceManager);
        
        // NEW: Register the Loot Manager (Passes dungeonItems so it can generate loot)
        getServer().getPluginManager().registerEvents(new DungeonLootManager(this, instanceManager, dungeonItems), this);

        // 4. Register Commands
        registerCommands();

        // 5. Register Listeners
        registerListeners();
        
        getLogger().info("-------------------------------------------");
        getLogger().info("          Evan_Hu is super cool ");
        getLogger().info("-------------------------------------------");
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
        if (getCommand("rambar") != null) getCommand("rambar").setExecutor(new RamBarCommand(this));
        if (getCommand("spawndm") != null) getCommand("spawndm").setExecutor(new SpawnDMCommand());
    }

    private void registerListeners() {
        // Item Mechanics
        getServer().getPluginManager().registerEvents(new DungeonItemListener(dungeonItems, this), this);
        getServer().getPluginManager().registerEvents(new DungeonDurabilityListener(dungeonItems, this), this);
        getServer().getPluginManager().registerEvents(new DungeonCraftingListener(dungeonItems), this);
        // getServer().getPluginManager().registerEvents(new DungeonRecipeUnlocker(this), this); // Uncomment if you have this file

        // Dungeon Generation & Spawning
        // getServer().getPluginManager().registerEvents(new DungeonMobListener(), this); // Uncomment if you have this file
        
        // NPC & GUI
        getServer().getPluginManager().registerEvents(new DungeonNPCListener(progressionManager), this);
        getServer().getPluginManager().registerEvents(new DungeonGUIListener(this, instanceManager, progressionManager), this);

        // Player Interactions
        getServer().getPluginManager().registerEvents(new ChatListener(messageManager, this), this);
        getServer().getPluginManager().registerEvents(new DungeonDeathListener(partyManager, this), this);
        
        // Boss Mechanics
        getServer().getPluginManager().registerEvents(new BossAbilityListener(this, bossManager), this);
        getServer().getPluginManager().registerEvents(new BossDeathListener(this, partyManager, progressionManager, instanceManager, bossManager), this);
    }

    // --- Getters ---
    public DungeonItems getDungeonItems() { return dungeonItems; }
    public PartyManager getPartyManager() { return partyManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public DungeonInstanceManager getInstanceManager() { return instanceManager; }
    public DungeonBossManager getBossManager() { return bossManager; }
    public ProgressionManager getProgressionManager() { return progressionManager; }
}