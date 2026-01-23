package com.mmhs.dungeons.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SpawnDMCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Players only.").color(NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("dungeons.admin")) {
            player.sendMessage(Component.text("You cannot summon the Master.").color(NamedTextColor.RED));
            return true;
        }

        // Spawn Wither Skeleton
        WitherSkeleton dm = (WitherSkeleton) player.getWorld().spawnEntity(player.getLocation(), EntityType.WITHER_SKELETON);

        // --- VISUALS ---
        dm.customName(Component.text("Dungeon Master").color(NamedTextColor.DARK_PURPLE).decoration(TextDecoration.BOLD, true));
        dm.setCustomNameVisible(true);
        dm.setRemoveWhenFarAway(false); // Important: Keeps him loaded

        // --- BEHAVIOR ---
        dm.setAI(false);            // Won't move or attack
        dm.setSilent(true);         // No rattling bones
        dm.setInvulnerable(true);   // Can't be killed by players
        dm.setCanPickupItems(false);

        // --- EQUIPMENT ---
        if (dm.getEquipment() != null) {
            // Hand: The "Control Book"
            dm.getEquipment().setItemInOffHand(new ItemStack(Material.ENCHANTED_BOOK));
            // Head: Dragon Head
            dm.getEquipment().setHelmet(new ItemStack(Material.DRAGON_HEAD));
            // Chest: Netherite Chestplate
            dm.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
            dm.getEquipment().setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
            dm.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));
            dm.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        }
        
        player.sendMessage(Component.text("The Dungeon Master (Wither Skeleton) has arrived.").color(NamedTextColor.GREEN));
        return true;
    }
}