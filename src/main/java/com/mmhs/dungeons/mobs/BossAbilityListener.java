package com.mmhs.dungeons.mobs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Monster;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Random;

public class BossAbilityListener implements Listener {

    private final DungeonBossManager bossManager;
    private final Plugin plugin;
    private final Random random = new Random();

    public BossAbilityListener(Plugin plugin, DungeonBossManager bossManager) {
        this.plugin = plugin;
        this.bossManager = bossManager;
        
        // The Brain Loop: Checks every 4 seconds (80 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                tickBosses();
            }
        }.runTaskTimer(plugin, 60L, 80L);
    }

    private void tickBosses() {
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                BossType type = bossManager.getBossType(entity);
                
                // If it's a boss, valid, and has a target nearby
                if (type != null && !entity.isDead() && !entity.getLocation().getNearbyPlayers(20).isEmpty()) {
                    runAbility(entity, type);
                }
            }
        }
    }

    private void runAbility(LivingEntity boss, BossType type) {
        // 30% chance to just perform a basic attack cycle instead of ability
        if (random.nextInt(100) < 30) return;

        switch (type) {
            // --- TIER 1 BOSSES ---
            case TEMPEST_CONSTRUCT:
                if (random.nextBoolean()) castGust(boss);
                else castDash(boss);
                break;

            case STONE_COLOSSUS: // Renamed from STONE_COLOSSUS to match Enum
                if (random.nextBoolean()) castQuake(boss);
                else castTNTThrow(boss);
                break;
                
            case KNIGHT_OF_THE_VOID:
                if (random.nextBoolean()) castVoidPull(boss);
                else castWitherStorm(boss);
                break;

            // --- TIER 2 & 3 BOSSES ---
            case THE_FORGOTTEN_KING:
                int roll = random.nextInt(3);
                if (roll == 0) castRoyalDecree(boss);
                else if (roll == 1) castSummonBlades(boss);
                else castJudgementPhase(boss);
                break;

            case THE_ABYSSAL_OVERLORD:
                if (random.nextBoolean()) castVoidBarrage(boss);
                else castSummonAbyss(boss);
                castDarkPulse(boss); // Passive ability
                break;

            case THE_STORMBOUND_KNIGHT:
                if (random.nextBoolean()) castChainLightning(boss);
                else castThunderDash(boss);
                break;
        }
    }

    // =========================================
    //              TIER 1 ABILITIES
    // =========================================

    private void castGust(LivingEntity boss) {
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1f, 0.5f);
        boss.getWorld().spawnParticle(Particle.EXPLOSION, boss.getLocation(), 20, 1, 1, 1, 0.1);
        for (Entity e : boss.getNearbyEntities(8, 5, 8)) {
            if (e instanceof Player p) {
                Vector dir = p.getLocation().toVector().subtract(boss.getLocation().toVector()).normalize();
                p.setVelocity(dir.multiply(2.0).setY(0.6));
                p.sendMessage(Component.text("The Tempest blows you back!").color(NamedTextColor.AQUA));
            }
        }
    }
    
    private void castDash(LivingEntity boss) {
        Player target = getNearestPlayer(boss);
        if (target == null) return;
        Vector dir = target.getLocation().toVector().subtract(boss.getLocation().toVector()).normalize();
        boss.setVelocity(dir.multiply(2.5).setY(0.4));
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1.5f);
    }

    private void castQuake(LivingEntity boss) {
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1f, 0.5f);
        boss.getWorld().spawnParticle(Particle.BLOCK, boss.getLocation(), 50, 2, 0, 2, 0.1, Material.DIRT.createBlockData());
        for (Entity e : boss.getNearbyEntities(10, 2, 10)) {
            if (e instanceof Player p && p.isOnGround()) {
                p.setVelocity(new Vector(0, 1.2, 0));
                p.sendMessage(Component.text("EARTHQUAKE!").color(NamedTextColor.GOLD));
            }
        }
    }

    private void castTNTThrow(LivingEntity boss) {
        Player target = getNearestPlayer(boss);
        if (target == null) return;
        TNTPrimed tnt = boss.getWorld().spawn(boss.getEyeLocation(), TNTPrimed.class);
        tnt.setFuseTicks(40);
        Vector dir = target.getLocation().toVector().subtract(boss.getEyeLocation().toVector()).normalize();
        tnt.setVelocity(dir.multiply(1.2).setY(0.5));
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
    }

    private void castVoidPull(LivingEntity boss) {
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.1f);
        boss.getWorld().spawnParticle(Particle.PORTAL, boss.getLocation(), 100, 1, 1, 1, 0.5);
        for (Player p : boss.getLocation().getNearbyPlayers(15)) {
            Vector dir = boss.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
            p.setVelocity(dir.multiply(1.2));
            p.sendMessage(Component.text("The Void drags you in...").color(NamedTextColor.DARK_PURPLE));
        }
    }
    
    private void castWitherStorm(LivingEntity boss) {
        for (Player p : boss.getLocation().getNearbyPlayers(10)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            boss.getWorld().spawnParticle(Particle.SMOKE, p.getLocation(), 10, 0.5, 1, 0.5, 0.05);
        }
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1f, 0.8f);
    }

    // =========================================
    //            TIER 2 & 3 ABILITIES
    // =========================================

    private void castRoyalDecree(LivingEntity boss) {
        boss.getWorld().playSound(boss.getLocation(), Sound.EVENT_RAID_HORN, 1f, 1f);
        boss.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, boss.getLocation(), 50, 1, 1, 1, 0.5);
        for (Entity e : boss.getNearbyEntities(20, 10, 20)) {
            if (e instanceof Monster m && !m.equals(boss)) {
                m.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                m.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0));
                m.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, m.getLocation().add(0, 2, 0), 5);
            }
        }
        boss.sendMessage(Component.text("KING: Minions! Serve your master!").color(NamedTextColor.GOLD));
    }

    private void castSummonBlades(LivingEntity boss) {
        Player target = getNearestPlayer(boss);
        if (target == null) return;
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_EVOKER_FANGS_ATTACK, 1f, 1f);
        Vector dir = target.getLocation().toVector().subtract(boss.getLocation().toVector()).normalize();
        Location start = boss.getLocation();
        new BukkitRunnable() {
            int i = 1;
            public void run() {
                if (i > 10 || boss.isDead()) { this.cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(i));
                point.setY(boss.getWorld().getHighestBlockYAt(point));
                boss.getWorld().spawn(point, EvokerFangs.class);
                i++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void castJudgementPhase(LivingEntity boss) {
        boss.sendMessage(Component.text("KING: JUDGEMENT IS UPON YOU! (BLOCK!)").color(NamedTextColor.DARK_RED));
        boss.getWorld().playSound(boss.getLocation(), Sound.BLOCK_BELL_USE, 2f, 0.5f);
        new BukkitRunnable() {
            public void run() {
                if (boss.isDead()) return;
                boss.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, boss.getLocation(), 1);
                boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.5f);
                for (Player p : boss.getLocation().getNearbyPlayers(30)) {
                    if (p.isBlocking()) {
                        p.sendMessage(Component.text("You blocked the judgement!").color(NamedTextColor.GREEN));
                        p.playSound(p.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
                    } else {
                        p.damage(15.0, boss);
                        p.sendMessage(Component.text("You failed to block!").color(NamedTextColor.RED));
                    }
                }
            }
        }.runTaskLater(plugin, 60L);
    }

    private void castVoidBarrage(LivingEntity boss) {
        new BukkitRunnable() {
            int count = 0;
            public void run() {
                if (count >= 3 || boss.isDead()) { this.cancel(); return; }
                Player target = getNearestPlayer(boss);
                if (target != null) {
                    WitherSkull skull = boss.launchProjectile(WitherSkull.class);
                    skull.setCharged(true);
                    Vector dir = target.getLocation().add(0, 1, 0).toVector().subtract(boss.getEyeLocation().toVector()).normalize();
                    skull.setVelocity(dir.multiply(1.5));
                }
                count++;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void castDarkPulse(LivingEntity boss) {
        boss.getWorld().spawnParticle(Particle.SCULK_SOUL, boss.getLocation(), 20, 5, 1, 5, 0.1);
        for (Player p : boss.getLocation().getNearbyPlayers(20)) {
            p.damage(1.0, boss);
        }
    }

    private void castSummonAbyss(LivingEntity boss) {
        boss.sendMessage(Component.text("OVERLORD: Rise, creatures of the void...").color(NamedTextColor.GRAY));
        Location loc = boss.getLocation();
        for (int i = 0; i < 3; i++) {
            WitherSkeleton minion = boss.getWorld().spawn(loc, WitherSkeleton.class);
            minion.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.STONE_SWORD));
        }
    }

    private void castThunderDash(LivingEntity boss) {
        Player target = getNearestPlayer(boss);
        if (target == null) return;
        Vector dir = target.getLocation().toVector().subtract(boss.getLocation().toVector()).normalize();
        boss.setVelocity(dir.multiply(2.0));
        new BukkitRunnable() {
            public void run() {
                if (boss.isOnGround() || boss.isDead()) { this.cancel(); return; }
                boss.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, boss.getLocation(), 5, 0.5, 0.5, 0.5, 0.1);
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void castChainLightning(LivingEntity boss) {
        boss.sendMessage(Component.text("KNIGHT: LINK!").color(NamedTextColor.AQUA));
        List<Player> players = (List<Player>) boss.getLocation().getNearbyPlayers(15);
        if (players.isEmpty()) return;
        Location prevLoc = boss.getEyeLocation();
        for (Player p : players) {
            drawParticleLine(prevLoc, p.getEyeLocation());
            p.damage(6.0, boss);
            p.getWorld().playSound(p.getLocation(), Sound.BLOCK_CHAIN_BREAK, 1f, 2f);
            prevLoc = p.getEyeLocation();
        }
    }

    private void drawParticleLine(Location start, Location end) {
        double distance = start.distance(end);
        Vector p1 = start.toVector();
        Vector p2 = end.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(0.5);
        for (double i = 0; i < distance; i += 0.5) {
            Vector current = p1.clone().add(vector.clone().multiply(i));
            start.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, current.getX(), current.getY(), current.getZ(), 1, 0, 0, 0, 0);
        }
    }

    private Player getNearestPlayer(LivingEntity boss) {
        Player nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Player p : boss.getLocation().getNearbyPlayers(30)) {
            if (p.getGameMode() == GameMode.SURVIVAL) {
                double d = p.getLocation().distanceSquared(boss.getLocation());
                if (d < minDist) {
                    minDist = d;
                    nearest = p;
                }
            }
        }
        return nearest;
    }
}