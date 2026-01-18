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
            sender.sendMessage("Players only.");
            return true;
        }

        // Updated to use the new method names
        p.getInventory().addItem(
                items.soulpiercerI(),
                items.bulwarkOfResolveI(),
                items.heroesBroadswordI(),
                items.nightveilDaggersI(),
                items.stonewardenAxeI(),
                items.soulpiercerII(),
                items.bulwarkOfResolveII(),
                items.heroesBroadswordII(),
                items.nightveilDaggersII(),
                items.stonewardenAxeII(),
                items.soulpiercerIII(),
                items.bulwarkOfResolveIII(),
                items.heroesBroadswordIII(),
                items.nightveilDaggersIII(),
                items.stonewardenAxeIII()
        );
        p.sendMessage("Given all dungeon items.");
        return true;
    }
}
