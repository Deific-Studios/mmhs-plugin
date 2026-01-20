package com.mmhs.dungeons.items;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge; // Requires 1.21 API
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.Location;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class DungeonItemListener implements Listener {
    private final DungeonItems dungeonItems;
    private final Plugin plugin;
    private final CrownManager crownManager;
    
    // Cooldowns & States
    private final Set<UUID> sweepInProgress = new HashSet<>();
    private final Map<UUID, Long> lastSpearBoost = new HashMap<>();
    private final Map<UUID, Long> lastDaggerInvis = new HashMap<>();
    private final Map<UUID, ItemStack> openCrowns = new HashMap<>();
    
    // Stores player armor while they are invisible so we can restore it later
    private final Map<UUID, ItemStack[]> invisibleArmorStore = new HashMap<>();

    public DungeonItemListener(DungeonItems dungeonItems, Plugin plugin) {
        this.dungeonItems = dungeonItems;
        this.plugin = plugin;
        this.crownManager = new CrownManager(plugin);
    }

    // --- INTERACTION EVENTS (Right/Left Click) ---
    @EventHandler(priority = EventPriority.NORMAL)
    public void onRightClick(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        ItemStack item = event.getItem();
        DungeonItems.Id id = dungeonItems.getId(item);
        if (id == null) return;
        
        int tier = dungeonItems.getTier(item);

        if (id.name().startsWith("SOULPIERCER")) {
            event.setCancelled(true); // Prevent throwing trident
            handleSoulpiercerBoost(p, tier);
        } else if (id.name().startsWith("NIGHTVEIL")) {
            // No cancel needed for swords usually, but good practice if it has block interaction
            handleShadowCloak(p, tier);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLeftClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        
        ItemStack item = event.getItem();
        DungeonItems.Id id = dungeonItems.getId(item);
        if (id == null) return;

        if (id.name().startsWith("CROWN")) {
            event.setCancelled(true);
            openCrownGUI(event.getPlayer(), item);
        }
    }

    // --- COMBAT EVENTS ---
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        // Handle Attacker Logic
        if (event.getDamager() instanceof Player p && event.getEntity() instanceof LivingEntity victim) {
            handleAttacker(event, p, victim);
        }
        
        // Handle Defender Logic (Shields)
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof LivingEntity attacker) {
            handleVictim(event, victim, attacker);
        }
    }

    private void handleAttacker(EntityDamageByEntityEvent event, Player p, LivingEntity victim) {
        if (sweepInProgress.contains(p.getUniqueId())) return;
        
        ItemStack weapon = p.getInventory().getItemInMainHand();
        DungeonItems.Id id = dungeonItems.getId(weapon);
        if (id == null) return;
        
        int tier = dungeonItems.getTier(weapon);

        if (id.name().startsWith("SOULPIERCER")) handleSoulpiercerStrike(event, p, victim, tier);
        else if (id.name().startsWith("NIGHTVEIL")) handleBackstab(event, p, victim, tier);
        else if (id.name().startsWith("HEROES")) handleSweepingStrike(event, p, victim, tier);
    }

    private void handleVictim(EntityDamageByEntityEvent event, Player victim, LivingEntity attacker) {
        if (!victim.isBlocking()) return;
        
        // Check both hands for the shield
        ItemStack offhand = victim.getInventory().getItemInOffHand();
        ItemStack mainhand = victim.getInventory().getItemInMainHand();
        
        DungeonItems.Id offhandId = dungeonItems.getId(offhand);
        DungeonItems.Id mainhandId = dungeonItems.getId(mainhand);
        
        boolean isOffhandBulwark = offhandId != null && offhandId.name().startsWith("BULWARK");
        boolean isMainhandBulwark = mainhandId != null && mainhandId.name().startsWith("BULWARK");

        if (isOffhandBulwark || isMainhandBulwark) {
            int tier = isOffhandBulwark ? dungeonItems.getTier(offhand) : dungeonItems.getTier(mainhand);
            handleShieldBash(event, victim, attacker, tier);
        }
    }

    // --- ABILITY IMPLEMENTATIONS ---

    private void handleSoulpiercerBoost(Player p, int tier) {
        long cooldown = tier == 1 ? 5000 : tier == 2 ? 3500 : 2000;
        long now = System.currentTimeMillis();
        
        if (now - lastSpearBoost.getOrDefault(p.getUniqueId(), 0L) < cooldown) {
            long remaining = (cooldown - (now - lastSpearBoost.getOrDefault(p.getUniqueId(), 0L))) / 1000;
            // Optional: send cooldown message
            return; 
        }
        lastSpearBoost.put(p.getUniqueId(), now);

        double boostStrength = tier == 1 ? 2.0 : tier == 2 ? 2.8 : 3.5;
        double verticalBoost = tier == 3 ? 0.9 : 0.5;

        Vector boost = p.getLocation().getDirection().normalize().multiply(boostStrength).setY(verticalBoost);
        p.setVelocity(boost);
        
        p.playSound(p.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 1f, 1.2f);
        p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation(), 15, 0.2, 0.2, 0.2, 0.1);
        
        // Tier 3 flight mechanic: handled by velocity, no Flight mode needed for simple dash
    }

    private void handleSoulpiercerStrike(EntityDamageByEntityEvent event, Player p, LivingEntity victim, int tier) {
        if (tier >= 2) event.setDamage(event.getDamage() * 1.1);
        
        // Spawn Wind Charge above victim and shoot down
        Location spawnLoc = victim.getLocation().add(0, 1, 0);
        
        // 1.21 API Check
        try {
            WindCharge charge = p.getWorld().spawn(spawnLoc, WindCharge.class);
            charge.setShooter(p);
            charge.setVelocity(new Vector(0, -1, 0)); // Shoot down
        } catch (NoClassDefFoundError | IllegalArgumentException e) {
            // Fallback for older versions or missing entity types
            p.getWorld().spawnParticle(Particle.EXPLOSION, victim.getLocation().add(0,1,0), 1);
        }
    }
    
    private void handleBackstab(EntityDamageByEntityEvent event, Player p, LivingEntity victim, int tier) {
        double bonus = tier == 1 ? 5.0 : tier == 2 ? 7.0 : 10.0;
        
        Vector victimDir = victim.getLocation().getDirection().setY(0).normalize();
        Vector attackDir = p.getLocation().getDirection().setY(0).normalize();
        
        // Dot product > 0.5 means facing same direction (attacking from behind)
        if (victimDir.dot(attackDir) > 0.5) { 
            event.setDamage(event.getDamage() + bonus);
            p.sendMessage(Component.text("Backstab! +" + bonus).color(NamedTextColor.DARK_PURPLE));
            p.getWorld().spawnParticle(Particle.CRIT, victim.getEyeLocation(), 10);
            p.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 2f);
                
            if (tier == 3) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1));
            }
        }
    }
    
    private void handleShadowCloak(Player p, int tier) {
        // Cooldown Check
        long cooldown = 15000;
        long now = System.currentTimeMillis();
        if (now - lastDaggerInvis.getOrDefault(p.getUniqueId(), 0L) < cooldown) {
            p.sendMessage(Component.text("Cloak on cooldown!").color(NamedTextColor.RED));
            return;
        }
        lastDaggerInvis.put(p.getUniqueId(), now);

        int durationTicks = tier == 1 ? 100 : tier == 2 ? 140 : 200; // 5s, 7s, 10s

        // 1. Store Armor
        invisibleArmorStore.put(p.getUniqueId(), p.getInventory().getArmorContents());
        p.getInventory().setArmorContents(null); // Remove armor to be fully invisible

        // 2. Apply Effects
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, durationTicks, 0, false, false, true));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, 1, false, false, true));
        
        p.sendMessage(Component.text("You vanish into the shadows...").color(NamedTextColor.DARK_PURPLE));
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation(), 20, 0.5, 1, 0.5, 0.1);
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 2f);

        // 3. Restore Task
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            restoreArmor(p);
            p.sendMessage(Component.text("You reappear.").color(NamedTextColor.GRAY));
            p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.5f, 2f);
        }, durationTicks);
    }
    
    private void restoreArmor(Player p) {
        if (invisibleArmorStore.containsKey(p.getUniqueId())) {
            ItemStack[] armor = invisibleArmorStore.remove(p.getUniqueId());
            if (armor != null) {
                p.getInventory().setArmorContents(armor);
            }
        }
    }

    private void handleSweepingStrike(EntityDamageByEntityEvent event, Player p, LivingEntity victim, int tier) {
        sweepInProgress.add(p.getUniqueId());
        
        double aoeDmg = tier == 1 ? 3.0 : tier == 2 ? 4.5 : 6.0;
        double radius = tier == 1 ? 3.5 : 4.5;
        
        for (Entity e : victim.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity le && e != p) {
                le.damage(aoeDmg, p);
                if (tier == 3) {
                    le.setVelocity(le.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(0.5).setY(0.2));
                }
            }
        }
        
        p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, victim.getLocation().add(0, 1, 0), 1);
        p.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);
        
        // Allow attacking again next tick
        Bukkit.getScheduler().runTaskLater(plugin, () -> sweepInProgress.remove(p.getUniqueId()), 1L);
    }

    private void handleShieldBash(EntityDamageByEntityEvent event, Player victim, LivingEntity attacker, int tier) {
        double knockback = tier == 1 ? 0.8 : tier == 2 ? 1.2 : 1.5;
        
        Vector dir = attacker.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize().multiply(knockback).setY(0.4);
        attacker.setVelocity(dir);
        
        victim.getWorld().playSound(victim.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 0.8f);
        victim.getWorld().spawnParticle(Particle.CRIT, attacker.getLocation().add(0, 1, 0), 10);
        
        if (tier >= 2) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, tier == 3 ? 1 : 0));
        }
        
        if (tier >= 3) {
            attacker.damage(event.getDamage() * 0.2, victim); // Reflect 20%
            victim.sendMessage(Component.text("Reflected Damage!").color(NamedTextColor.GOLD));
        }
    }
    
    // --- CROWN GUI LOGIC ---
    
    private void openCrownGUI(Player p, ItemStack crown) {
        int tier = dungeonItems.getTier(crown);
        int slots = tier == 1 ? 3 : tier == 2 ? 6 : 9;
        
        Inventory inv = Bukkit.createInventory(new CrownInventoryHolder(null), 27, 
            Component.text("Crown of Monsters").color(NamedTextColor.DARK_RED));
        
        List<EntityType> storedHeads = crownManager.getStoredHeads(crown);
        
        // Fill slots with heads
        for (int i = 0; i < slots; i++) {
            if (i < storedHeads.size()) {
                ItemStack head = new ItemStack(Material.PLAYER_HEAD); // You would ideally set the skin here
                ItemMeta meta = head.getItemMeta();
                meta.displayName(Component.text(storedHeads.get(i).name()).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                head.setItemMeta(meta);
                inv.setItem(10 + i, head); // Center row
            } else {
                ItemStack empty = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta meta = empty.getItemMeta();
                meta.displayName(Component.text("Empty Slot").color(NamedTextColor.GRAY));
                empty.setItemMeta(meta);
                inv.setItem(10 + i, empty);
            }
        }
        
        openCrowns.put(p.getUniqueId(), crown);
        p.openInventory(inv);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CrownInventoryHolder)) return;
        event.setCancelled(true); // Read-only view for now, or handle adding/removing logic if desired
        
        // If you want to implement adding/removing logic here, you can call crownManager methods
        // For now, we keep it safe by cancelling clicks
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof CrownInventoryHolder) {
            openCrowns.remove(event.getPlayer().getUniqueId());
        }
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Safety: Restore armor if player quits while invisible
        restoreArmor(event.getPlayer());
    }
}