package com.mmhs.dungeons.items;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DungeonItemListener implements Listener {
    private final DungeonItems dungeonItems;
    private final Plugin plugin;
    private final CrownManager crownManager;
    
    private final Set<UUID> sweepInProgress = new HashSet<>();
    private final Map<UUID, Long> lastSpearBoost = new HashMap<>();
    private final Map<UUID, Long> lastDaggerInvis = new HashMap<>();
    private final Map<UUID, ItemStack> openCrowns = new HashMap<>();

    public DungeonItemListener(DungeonItems dungeonItems, Plugin plugin) {
        this.dungeonItems = dungeonItems;
        this.plugin = plugin;
        this.crownManager = new CrownManager(plugin);
    }

    /* ========================================
       RIGHT CLICK ABILITIES
       ======================================== */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRightClick(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        
        if (event.getAction() != Action.RIGHT_CLICK_AIR && 
            event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null) return;
        
        DungeonItems.Id id = dungeonItems.getId(item);
        if (id == null) return;
        
        int tier = dungeonItems.getTier(item);

        // SOULPIERCER: Wind charge boost
        if (isSoulpiercer(id)) {
            event.setCancelled(true);
            handleSoulpiercerBoost(p, tier);
        }

        // NIGHTVEIL DAGGERS: Shadow cloak
        if (isNightveilDaggers(id)) {
            event.setCancelled(true);
            handleShadowCloak(p, tier);
        }
    }

    /* ========================================
       LEFT CLICK ABILITIES
       ======================================== */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLeftClick(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        
        if (event.getAction() != Action.LEFT_CLICK_AIR && 
            event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null) return;
        
        DungeonItems.Id id = dungeonItems.getId(item);
        if (id == null) return;

        // CROWN OF MONSTERS: Open management GUI
        if (isCrown(id)) {
            event.setCancelled(true);
            openCrownGUI(p, item);
        }
    }

    /* ========================================
       ATTACK ABILITIES
       ======================================== */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        handleAttacker(event);
        handleVictim(event);
    }

    private void handleAttacker(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player p)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        if (sweepInProgress.contains(p.getUniqueId())) return;

        ItemStack weapon = p.getInventory().getItemInMainHand();
        DungeonItems.Id id = dungeonItems.getId(weapon);
        if (id == null) return;
        
        int tier = dungeonItems.getTier(weapon);

        // SOULPIERCER: Impaling strike
        if (isSoulpiercer(id)) {
            handleSoulpiercerStrike(event, p, victim, tier);
        }

        // NIGHTVEIL DAGGERS: Backstab
        if (isNightveilDaggers(id)) {
            handleBackstab(event, p, victim, tier);
        }

        // HERO'S BROADSWORD: Sweeping strike
        if (isHeroesBroadsword(id)) {
            handleSweepingStrike(event, p, victim, tier);
        }
    }

    private void handleVictim(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;

        ItemStack offhand = victim.getInventory().getItemInOffHand();
        ItemStack mainhand = victim.getInventory().getItemInMainHand();
        
        DungeonItems.Id offhandId = dungeonItems.getId(offhand);
        DungeonItems.Id mainhandId = dungeonItems.getId(mainhand);
        
        boolean hasBulwark = isBulwark(offhandId) || isBulwark(mainhandId);
        if (!hasBulwark) return;
        if (!victim.isBlocking()) return;

        int tier = isBulwark(offhandId) ? dungeonItems.getTier(offhand) : dungeonItems.getTier(mainhand);
        handleShieldBash(event, victim, attacker, tier);
    }

    /* ========================================
       SOULPIERCER ABILITIES
       ======================================== */

    private void handleSoulpiercerBoost(Player p, int tier) {
        // Cooldown: 2s (T1), 1.5s (T2), 1s (T3)
        long cooldown = tier == 1 ? 2000 : tier == 2 ? 1500 : 1000;
        
        long now = System.currentTimeMillis();
        Long last = lastSpearBoost.get(p.getUniqueId());
        if (last != null && now - last < cooldown) {
            long remaining = (cooldown - (now - last)) / 1000;
            p.sendMessage(Component.text("Cooldown: " + remaining + "s").color(NamedTextColor.RED));
            return;
        }
        
        lastSpearBoost.put(p.getUniqueId(), now);

        Location playerLoc = p.getLocation();
        Vector direction = playerLoc.getDirection();
        Vector behind = direction.clone().multiply(-1);
        
        double horizontalMag = Math.sqrt(behind.getX() * behind.getX() + behind.getZ() * behind.getZ());
        double angle = Math.toRadians(25);
        behind.setY(-horizontalMag * Math.tan(angle));
        behind.normalize().multiply(tier == 3 ? 2.0 : 1.5); // Tier 3 gets stronger boost
        
        Location spawnLoc = playerLoc.clone().add(behind).add(0, 1.0, 0);
        Entity windCharge = p.getWorld().spawnEntity(spawnLoc, EntityType.WIND_CHARGE);
        
        if (windCharge instanceof WindCharge wc) {
            wc.setVelocity(behind.clone().normalize().multiply(0.1));
        }
        
        p.getWorld().spawnParticle(Particle.CLOUD, spawnLoc, 8, 0.2, 0.2, 0.2, 0.02);
        
        // Tier 3: Brief flight effect
        if (tier == 3) {
            p.setAllowFlight(true);
            p.setFlying(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!p.getGameMode().toString().contains("CREATIVE") && 
                    !p.getGameMode().toString().contains("SPECTATOR")) {
                    p.setAllowFlight(false);
                    p.setFlying(false);
                }
            }, 20L); // 1 second of flight
        }
    }

    private void handleSoulpiercerStrike(EntityDamageByEntityEvent event, Player p, LivingEntity victim, int tier) {
        victim.getWorld().spawnParticle(Particle.CLOUD,
                victim.getLocation().add(0, 1.0, 0),
                15, 0.4, 0.5, 0.4, 0.05);
        
        victim.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                victim.getLocation().add(0, 1.0, 0),
                3, 0.3, 0.3, 0.3, 0.0);

        // Tier 2+: +10% damage, Tier 3: +20% vs airborne
        if (tier >= 2) {
            double multiplier = 1.1;
            if (tier >= 3 && !victim.isOnGround()) {
                multiplier = 1.2;
            }
            event.setDamage(event.getDamage() * multiplier);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Entity windCharge = victim.getWorld().spawnEntity(
                victim.getLocation().add(0, 0.5, 0), 
                EntityType.WIND_CHARGE
            );
            
            if (windCharge instanceof WindCharge wc) {
                wc.setVelocity(new Vector(0, -1.5, 0));
            }
        }, 1L);
    }

    /* ========================================
       NIGHTVEIL DAGGERS ABILITIES
       ======================================== */

    private void handleShadowCloak(Player p, int tier) {
        long now = System.currentTimeMillis();
        Long last = lastDaggerInvis.get(p.getUniqueId());
        if (last != null && now - last < 15000) {
            long remaining = (15000 - (now - last)) / 1000;
            p.sendMessage(Component.text("Cooldown: " + remaining + "s").color(NamedTextColor.RED));
            return;
        }
        
        lastDaggerInvis.put(p.getUniqueId(), now);

        // Duration: 5s (T1), 7s (T2), 10s (T3)
        int duration = tier == 1 ? 100 : tier == 2 ? 140 : 200; // in ticks

        // Store and hide armor
        ItemStack[] armor = p.getInventory().getArmorContents();
        p.getInventory().setArmorContents(new ItemStack[4]);
        
        // Apply invisibility + speed 2
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1, false, false));
        
        // Particles
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.05);
        p.sendMessage(Component.text("You vanish into the shadows...").color(NamedTextColor.DARK_PURPLE));
        
        // Restore armor after duration
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            p.getInventory().setArmorContents(armor);
            p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.03);
            p.sendMessage(Component.text("You reappear.").color(NamedTextColor.GRAY));
        }, duration);
    }

    private void handleBackstab(EntityDamageByEntityEvent event, Player p, LivingEntity victim, int tier) {
        // Backstab damage: +4 (T1), +6 (T2), +10 (T3)
        double backstabBonus = tier == 1 ? 4.0 : tier == 2 ? 6.0 : 10.0;
        
        if (isBackstab(p.getLocation(), victim.getLocation())) {
            event.setDamage(event.getDamage() + backstabBonus);
            
            victim.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                    victim.getLocation().add(0, 1.0, 0),
                    15, 0.3, 0.5, 0.3, 0.1);
            
            p.sendMessage(Component.text("BACKSTAB! +" + backstabBonus + " damage")
                    .color(NamedTextColor.DARK_PURPLE));
            
            // Tier 3: Apply poison
            if (tier == 3) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0, false, true));
            }
        }
        
        victim.getWorld().spawnParticle(Particle.CRIT,
                victim.getLocation().add(0, 1.0, 0),
                20, 0.4, 0.5, 0.4, 0.1);
        
        victim.getWorld().spawnParticle(Particle.ENCHANT,
                victim.getLocation().add(0, 1.0, 0),
                10, 0.3, 0.5, 0.3, 0.5);
    }

    private boolean isBackstab(Location attackerLoc, Location victimLoc) {
        Vector victimDirection = victimLoc.getDirection().setY(0).normalize();
        Vector toAttacker = attackerLoc.toVector().subtract(victimLoc.toVector()).setY(0).normalize();
        
        double dot = victimDirection.dot(toAttacker);
        double angle = Math.toDegrees(Math.acos(dot));
        
        return angle > 120; // 120+ degrees = behind
    }

    /* ========================================
       HERO'S BROADSWORD ABILITIES
       ======================================== */

    private void handleSweepingStrike(EntityDamageByEntityEvent event, Player p, LivingEntity victim, int tier) {
        sweepInProgress.add(p.getUniqueId());
        
        // AOE damage: 6 (T1), 8 (T2), 12 (T3)
        double aoeDamage = tier == 1 ? 6.0 : tier == 2 ? 8.0 : 12.0;
        // Radius: 3.5 (T1), 4.0 (T2), 4.5 (T3)
        double radius = tier == 1 ? 3.5 : tier == 2 ? 4.0 : 4.5;

        victim.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                victim.getLocation().add(0, 1.0, 0),
                10, 1.2, 0.3, 1.2, 0.0);

        for (Entity e : victim.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity le && e != victim && e != p) {
                le.damage(aoeDamage, p);
                
                // Tier 3: Apply knockback
                if (tier == 3) {
                    Vector direction = le.getLocation().toVector()
                            .subtract(p.getLocation().toVector())
                            .normalize()
                            .multiply(0.5)
                            .setY(0.3);
                    le.setVelocity(direction);
                }
            }
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            sweepInProgress.remove(p.getUniqueId());
        }, 1L);
    }

    /* ========================================
       BULWARK OF RESOLVE ABILITIES
       ======================================== */

    private void handleShieldBash(EntityDamageByEntityEvent event, Player victim, LivingEntity attacker, int tier) {
        // Knockback: 1.8x (T1), 2.2x (T2), 2.8x (T3)
        double knockbackMultiplier = tier == 1 ? 1.8 : tier == 2 ? 2.2 : 2.8;
        
        Vector direction = attacker.getLocation().toVector()
                .subtract(victim.getLocation().toVector())
                .normalize();
        
        direction.multiply(knockbackMultiplier).setY(0.6);
        attacker.setVelocity(direction);

        victim.getWorld().spawnParticle(Particle.EXPLOSION,
                attacker.getLocation().add(0, 1.0, 0),
                3, 0.3, 0.3, 0.3, 0.0);
        
        victim.getWorld().spawnParticle(Particle.CRIT,
                attacker.getLocation().add(0, 1.0, 0),
                15, 0.5, 0.5, 0.5, 0.2);
        
        // Tier 2+: Resistance I for 3s, Tier 3: Resistance II for 5s
        if (tier >= 2) {
            int duration = tier == 3 ? 100 : 60; // 5s or 3s
            int amplifier = tier == 3 ? 1 : 0; // Resistance II or I
            victim.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, amplifier, false, true));
        }
        
        // Tier 3: Damage reflection
        if (tier >= 3) {
            double reflectDamage = event.getDamage() * 0.2;
            attacker.damage(reflectDamage, victim);
            victim.sendMessage(Component.text("Reflected " + String.format("%.1f", reflectDamage) + " damage!")
                    .color(NamedTextColor.GOLD));
        }
    }

    /* ========================================
       CROWN OF MONSTERS GUI
       ======================================== */

    private void openCrownGUI(Player p, ItemStack crown) {
        int tier = dungeonItems.getTier(crown);
        int slots = tier == 1 ? 3 : tier == 2 ? 6 : 9; // 3, 4, or 5 slots
        
        Inventory inv = Bukkit.createInventory(
                new CrownInventoryHolder(null),
                27,
                Component.text("Crown of Monsters").color(NamedTextColor.DARK_RED)
        );
        
        // Populate with current heads
        List<EntityType> stored = crownManager.getStoredHeads(crown);
        for (int i = 0; i < stored.size() && i < slots; i++) {
            ItemStack headDisplay = new ItemStack(Material.PLAYER_HEAD);
            var meta = headDisplay.getItemMeta();
            meta.displayName(Component.text(formatMobName(stored.get(i)))
                    .color(NamedTextColor.YELLOW)
                    .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
            headDisplay.setItemMeta(meta);
            inv.setItem(10 + i * 2, headDisplay);
        }
        
        // Add info item
        ItemStack info = new ItemStack(Material.BOOK);
        var infoMeta = info.getItemMeta();
        infoMeta.displayName(Component.text("Crown Slots: " + stored.size() + "/" + slots)
                .color(NamedTextColor.GOLD)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        infoMeta.lore(java.util.Arrays.asList(
            Component.text("Place mob heads in slots to absorb")
                    .color(NamedTextColor.GRAY)
                    .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false),
            Component.text("Right-click existing heads to remove")
                    .color(NamedTextColor.GRAY)
                    .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false)
        ));
        info.setItemMeta(infoMeta);
        inv.setItem(22, info);
        
        openCrowns.put(p.getUniqueId(), crown);
        p.openInventory(inv);
    }

    @EventHandler
    public void onCrownInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        if (!(event.getInventory().getHolder() instanceof CrownInventoryHolder)) return;
        
        ItemStack crown = openCrowns.get(p.getUniqueId());
        if (crown == null) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        
        int slot = event.getSlot();
        int tier = dungeonItems.getTier(crown);
        int maxSlots = tier == 1 ? 3 : tier == 2 ? 4 : 5;
        
        // Valid slots: 10, 12, 14, 16, 18 (depending on tier)
        int[] validSlots = {10, 12, 14, 16, 18};
        int crownSlot = -1;
        for (int i = 0; i < maxSlots && i < validSlots.length; i++) {
            if (slot == validSlots[i]) {
                crownSlot = i;
                break;
            }
        }
        
        if (crownSlot == -1) return;
        
        // Right-click to remove
        if (event.isRightClick() && clicked != null && clicked.getType() == Material.PLAYER_HEAD) {
            crownManager.removeMobHead(crown, crownSlot);
            event.getInventory().setItem(slot, null);
            p.sendMessage(Component.text("Head removed!").color(NamedTextColor.GREEN));
            return;
        }
        
        // Left-click with head to add
        if (event.isLeftClick() && cursor != null && cursor.getType() == Material.PLAYER_HEAD) {
            EntityType mobType = crownManager.getMobTypeFromHead(cursor);
            if (mobType == null) {
                p.sendMessage(Component.text("Could not identify mob head!").color(NamedTextColor.RED));
                return;
            }
            
            if (crownManager.storeMobHead(crown, crownSlot, mobType)) {
                ItemStack headDisplay = cursor.clone();
                headDisplay.setAmount(1);
                event.getInventory().setItem(slot, headDisplay);
                
                cursor.setAmount(cursor.getAmount() - 1);
                p.setItemOnCursor(cursor.getAmount() > 0 ? cursor : null);
                
                p.sendMessage(Component.text("Absorbed " + formatMobName(mobType) + " essence!")
                        .color(NamedTextColor.GREEN));
            }
        }
    }

    @EventHandler
    public void onCrownInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player p)) return;
        if (!(event.getInventory().getHolder() instanceof CrownInventoryHolder)) return;
        
        openCrowns.remove(p.getUniqueId());
    }

    /* ========================================
       HELPER METHODS
       ======================================== */

    private boolean isSoulpiercer(DungeonItems.Id id) {
        return id == DungeonItems.Id.SOULPIERCER_I || 
               id == DungeonItems.Id.SOULPIERCER_II || 
               id == DungeonItems.Id.SOULPIERCER_III;
    }

    private boolean isNightveilDaggers(DungeonItems.Id id) {
        return id == DungeonItems.Id.NIGHTVEIL_DAGGERS_I || 
               id == DungeonItems.Id.NIGHTVEIL_DAGGERS_II || 
               id == DungeonItems.Id.NIGHTVEIL_DAGGERS_III;
    }

    private boolean isHeroesBroadsword(DungeonItems.Id id) {
        return id == DungeonItems.Id.HEROES_BROADSWORD_I || 
               id == DungeonItems.Id.HEROES_BROADSWORD_II || 
               id == DungeonItems.Id.HEROES_BROADSWORD_III;
    }

    private boolean isBulwark(DungeonItems.Id id) {
        if (id == null) return false;
        return id == DungeonItems.Id.BULWARK_OF_RESOLVE_I || 
               id == DungeonItems.Id.BULWARK_OF_RESOLVE_II || 
               id == DungeonItems.Id.BULWARK_OF_RESOLVE_III;
    }

    private boolean isCrown(DungeonItems.Id id) {
        return id == DungeonItems.Id.CROWN_OF_MONSTERS_I || 
               id == DungeonItems.Id.CROWN_OF_MONSTERS_II || 
               id == DungeonItems.Id.CROWN_OF_MONSTERS_III;
    }

    private String formatMobName(EntityType type) {
        String[] words = type.name().toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            formatted.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }
        return formatted.toString().trim();
    }
}
