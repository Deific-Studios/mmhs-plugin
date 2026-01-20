package com.mmhs.dungeons.commands;


import com.mmhs.dungeons.items.DungeonItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GiveDungeonItemsCommand implements CommandExecutor, TabCompleter {

    private final DungeonItems dungeonItems;

    public GiveDungeonItemsCommand(DungeonItems dungeonItems) {
        this.dungeonItems = dungeonItems;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /ditems <item_id>").color(NamedTextColor.RED));
            return true;
        }

        String idStr = args[0].toUpperCase();
        DungeonItems.Id id;
        try {
            id = DungeonItems.Id.valueOf(idStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Component.text("Invalid Item ID!").color(NamedTextColor.RED));
            return true;
        }

        ItemStack item = getItemById(id);
        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage(Component.text("Gave you: ").color(NamedTextColor.GREEN).append(item.displayName()));
        } else {
            player.sendMessage(Component.text("Error generating item.").color(NamedTextColor.RED));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.stream(DungeonItems.Id.values())
                    .map(Enum::name)
                    .filter(name -> name.startsWith(args[0].toUpperCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private ItemStack getItemById(DungeonItems.Id id) {
        switch (id) {
            // Soulpiercer
            case SOULPIERCER_I: return dungeonItems.soulpiercerI();
            case SOULPIERCER_II: return dungeonItems.soulpiercerII();
            case SOULPIERCER_III: return dungeonItems.soulpiercerIII();
            
            // Bulwark
            case BULWARK_OF_RESOLVE_I: return dungeonItems.bulwarkOfResolveI();
            case BULWARK_OF_RESOLVE_II: return dungeonItems.bulwarkOfResolveII();
            case BULWARK_OF_RESOLVE_III: return dungeonItems.bulwarkOfResolveIII();
            
            // Broadsword
            case HEROES_BROADSWORD_I: return dungeonItems.heroesBroadswordI();
            case HEROES_BROADSWORD_II: return dungeonItems.heroesBroadswordII();
            case HEROES_BROADSWORD_III: return dungeonItems.heroesBroadswordIII();
            
            // Daggers
            case NIGHTVEIL_DAGGERS_I: return dungeonItems.nightveilDaggersI();
            case NIGHTVEIL_DAGGERS_II: return dungeonItems.nightveilDaggersII();
            case NIGHTVEIL_DAGGERS_III: return dungeonItems.nightveilDaggersIII();
            
            // Axe
            case STONEWARDEN_AXE_I: return dungeonItems.stonewardenAxeI();
            case STONEWARDEN_AXE_II: return dungeonItems.stonewardenAxeII();
            case STONEWARDEN_AXE_III: return dungeonItems.stonewardenAxeIII();
            
            // Crown
            case CROWN_OF_MONSTERS_I: return dungeonItems.crownOfMonstersI();
            case CROWN_OF_MONSTERS_II: return dungeonItems.crownOfMonstersII();
            case CROWN_OF_MONSTERS_III: return dungeonItems.crownOfMonstersIII();

            // --- MATERIALS ---
            case MAT_FRAGMENT: return dungeonItems.dungeonFragment();
            case MAT_ASCENSION: return dungeonItems.ascensionShard();
            case MAT_STORM: return dungeonItems.stormEssence();
            case MAT_PLATING: return dungeonItems.ancientPlating();
            case MAT_SHADOW: return dungeonItems.shadowDust();
            case MAT_GOLEM: return dungeonItems.golemCore();
            case MAT_HILT: return dungeonItems.heroHilt();
            case MAT_GOLD: return dungeonItems.cursedGold();
            
            default: return null;
        }
    }
}