package com.mmhs.dungeons.commands;

import com.mmhs.dungeons.items.DungeonItems;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveDungeonItemsCommand implements CommandExecutor {
    private final DungeonItems items;

    public GiveDungeonItemsCommand(DungeonItems items) {
        this.items = items;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            return true;
        }

        // Updated to use the new method names
        p.getInventory().addItem(
            
            items.soulpiercerI(),
            items.soulpiercerII(),
            items.soulpiercerIII(),
            
            items.stonewardenAxeI(),
            items.stonewardenAxeII(),
            items.stonewardenAxeIII(),
            
            items.nightveilDaggersI(),
            items.nightveilDaggersII(),
            items.nightveilDaggersIII(),
            
            items.bulwarkOfResolveI(),
            items.bulwarkOfResolveII(),
            items.bulwarkOfResolveIII(),
            
            items.heroesBroadswordI(),
            items.heroesBroadswordII(),
            items.heroesBroadswordIII(),
            
            items.crownOfMonstersI(),
            items.crownOfMonstersII(),
            items.crownOfMonstersIII()

        );
        p.sendMessage("Given all dungeon items.");
        return true;
    }
}
