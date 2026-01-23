package com.mmhs.dungeons.mobs;

import com.mmhs.dungeons.core.DungeonInstanceManager;
import com.mmhs.dungeons.core.DungeonsPlugin;
import com.mmhs.dungeons.core.PartyManager;
import com.mmhs.dungeons.core.ProgressionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class BossDeathListener implements Listener {

    private final DungeonsPlugin plugin;
    private final PartyManager partyManager;
    private final ProgressionManager progressionManager;
    private final DungeonInstanceManager instanceManager;
    private final DungeonBossManager bossManager;

    // Updated Constructor to accept ALL managers
    public BossDeathListener(DungeonsPlugin plugin, PartyManager partyManager, ProgressionManager progressionManager, DungeonInstanceManager instanceManager, DungeonBossManager bossManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
        this.progressionManager = progressionManager;
        this.instanceManager = instanceManager;
        this.bossManager = bossManager;
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        // 1. Check if the dead entity is a Custom Boss
        if (!bossManager.isBoss(event.getEntity())) return;

        // 2. Get the killer (Player)
        Player killer = event.getEntity().getKiller();
        if (killer == null) return; // If died to lava/cactus, ignore

        // 3. Get the Party
        List<Player> party = partyManager.getOnlineMembers(killer);

        // 4. Handle Victory for the whole party
        for (Player member : party) {
            // A. Unlock Next Tier
            // Assuming the boss tier is stored or mapped. For now, we increment blindly or check current dungeon type.
            // (You can refine this later to check specific boss names)
            int currentTier = progressionManager.getUnlockedTier(member);
            progressionManager.completeTier(member, currentTier);

            // B. Victory Effects
            member.playSound(member.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            member.sendTitlePart(net.kyori.adventure.title.TitlePart.TITLE, Component.text("VICTORY!").color(NamedTextColor.GOLD));
            member.sendTitlePart(net.kyori.adventure.title.TitlePart.SUBTITLE, Component.text("Teleporting to Dungeon Hub in 10s...").color(NamedTextColor.GREEN));
            
            // C. Heal
            member.setHealth(20);
            member.setFoodLevel(20);
        }

        // 5. Teleport Out Sequence (10 Seconds Delay)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player member : party) {
                    // Check if they are still online
                    if (member.isOnline()) {
                        member.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()); // Send to main world spawn
                        member.sendMessage(Component.text("Dungeon instance closed.").color(NamedTextColor.GRAY));
                    }
                }
            }
        }.runTaskLater(plugin, 200L); // 200 Ticks = 10 Seconds
    }
}