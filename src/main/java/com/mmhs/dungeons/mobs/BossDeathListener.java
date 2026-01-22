package com.mmhs.dungeons.mobs;

import com.mmhs.dungeons.core.ProgressionManager;
import com.mmhs.dungeons.items.DungeonItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class BossDeathListener implements Listener {

    private final DungeonBossManager bossManager;
    private final DungeonItems dungeonItems;
    private final ProgressionManager progressionManager;

    public BossDeathListener(DungeonBossManager bossManager, DungeonItems dungeonItems, ProgressionManager progressionManager) {
        this.bossManager = bossManager;
        this.dungeonItems = dungeonItems;
        this.progressionManager = progressionManager;
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 1. Check if this is a custom boss
        BossType type = bossManager.getBossType(entity);
        if (type == null) return;

        // 2. Handle Drops
        event.getDrops().clear();
        event.setDroppedExp(type == BossType.THE_ABYSSAL_OVERLORD ? 5000 : 1000);

        // Unique Drop
        ItemStack uniqueDrop = dungeonItems.getItemById(type.dropId);
        if (uniqueDrop != null) {
            event.getDrops().add(uniqueDrop);
        }
        
        // Fragments (Scaling amount)
        ItemStack fragments = dungeonItems.dungeonFragment();
        int fragCount = 3 + (int)(Math.random() * 3); // 3-6 default
        if (type == BossType.THE_FORGOTTEN_KING) fragCount = 6 + (int)(Math.random() * 4);
        if (type == BossType.THE_ABYSSAL_OVERLORD) fragCount = 10 + (int)(Math.random() * 10);
        
        fragments.setAmount(fragCount);
        event.getDrops().add(fragments);

        // 3. Global Announcement
        Bukkit.broadcast(
            Component.text("☠ The " + type.displayName + " has been slain!")
            .color(NamedTextColor.GOLD)
        );
        entity.getWorld().playSound(entity.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

        // 4. UNLOCK PROGRESSION
        // Map Boss Type to Tier Cleared
        int tierCleared = 0;
        
        // Tier 1 Bosses -> Unlocks Tier 2
        if (type == BossType.TEMPEST_CONSTRUCT || type == BossType.STONE_COLOSSUS || type == BossType.THE_STORMBOUND_KNIGHT) {
            tierCleared = 1; 
        }
        // Tier 2 Boss -> Unlocks Tier 3
        else if (type == BossType.THE_FORGOTTEN_KING) {
            tierCleared = 2;
        }
        // Tier 3 Boss -> Completion (Maybe unlock prestige later?)
        else if (type == BossType.THE_ABYSSAL_OVERLORD) {
            tierCleared = 3;
        }

        if (tierCleared > 0) {
            // Reward all players in the arena (Radius check is safer than Party check in case of disconnects)
            for (Player p : entity.getLocation().getNearbyPlayers(100)) {
                
                int oldProgress = progressionManager.getUnlockedTier(p);
                progressionManager.completeTier(p, tierCleared);
                int newProgress = progressionManager.getUnlockedTier(p);

                // Only send the "Unlocked" message if they actually gained a new tier
                if (newProgress > oldProgress && newProgress < 3) {
                    p.sendMessage(Component.text("--------------------------------").color(NamedTextColor.GOLD));
                    p.sendMessage(Component.text("   DUNGEON CLEARED!").color(NamedTextColor.GREEN).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD));
                    p.sendMessage(Component.text("   Tier " + (tierCleared + 1) + " Unlocked!").color(NamedTextColor.YELLOW));
                    p.sendMessage(Component.text("--------------------------------").color(NamedTextColor.GOLD));
                    p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                }
            }
        }
    }
}