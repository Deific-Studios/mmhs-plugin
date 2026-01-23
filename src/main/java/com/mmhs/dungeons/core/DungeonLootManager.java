package com.mmhs.dungeons.core;

import com.mmhs.dungeons.items.DungeonItems;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import java.util.Random;

public class DungeonLootManager implements Listener {

    private final DungeonInstanceManager instanceManager;
    private final DungeonItems dungeonItems;
    private final Plugin plugin;
    private final Random random = new Random();

    public DungeonLootManager(Plugin plugin, DungeonInstanceManager instanceManager, DungeonItems dungeonItems) {
        this.plugin = plugin;
        this.instanceManager = instanceManager;
        this.dungeonItems = dungeonItems;
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock().getType() != Material.CHEST && event.getClickedBlock().getType() != Material.BARREL) return;

        DungeonType type = instanceManager.getTypeAtLocation(event.getClickedBlock().getLocation());
        if (type == null) return;

        if (event.getClickedBlock().hasMetadata("dungeon_looted")) return;

        if (event.getClickedBlock().getState() instanceof Chest chest) {
            chest.getInventory().clear();
            int count = 2 + random.nextInt(4);
            for (int i = 0; i < count; i++) {
                int slot = random.nextInt(27);
                while(chest.getInventory().getItem(slot) != null) { slot = random.nextInt(27); }
                chest.getInventory().setItem(slot, getRandomLoot(type));
            }
            event.getClickedBlock().setMetadata("dungeon_looted", new FixedMetadataValue(plugin, true));
        }
    }

    private ItemStack getRandomLoot(DungeonType type) {
        int roll = random.nextInt(100);

        if (type == DungeonType.MOSS) {
            if (roll < 50) return new ItemStack(Material.BREAD, random.nextInt(3) + 1);
            else if (roll < 80) return new ItemStack(Material.IRON_INGOT);
            else {
                // FIXED: Now uses getItemById
                return random.nextBoolean() ? dungeonItems.getItemById(DungeonItems.Id.MAT_GOLEM) : dungeonItems.getItemById(DungeonItems.Id.MAT_STORM);
            }
        }
        else if (type == DungeonType.LAB) {
            if (roll < 50) return new ItemStack(Material.COOKED_BEEF, random.nextInt(4) + 1);
            else if (roll < 80) return new ItemStack(Material.GOLD_INGOT);
            else return dungeonItems.getItemById(DungeonItems.Id.CORE_WEAPON);
        }
        else {
            if (roll < 50) return new ItemStack(Material.GOLDEN_CARROT, random.nextInt(5) + 1);
            else if (roll < 80) return new ItemStack(Material.DIAMOND);
            else return random.nextBoolean() ? dungeonItems.getItemById(DungeonItems.Id.CORE_ABYSSAL) : dungeonItems.getItemById(DungeonItems.Id.SHARD_RELIC);
        }
    }
}