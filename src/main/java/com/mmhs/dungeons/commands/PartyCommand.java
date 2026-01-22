package com.mmhs.dungeons.commands;

import com.mmhs.dungeons.core.PartyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyCommand implements CommandExecutor {
    private final PartyManager partyManager;

    public PartyCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /party <create|invite|join|leave|list>").color(NamedTextColor.RED));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                partyManager.createParty(player);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /party invite <player>").color(NamedTextColor.RED));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(Component.text("Player not found.").color(NamedTextColor.RED));
                    return true;
                }
                partyManager.invitePlayer(player, target);
                break;
            case "join":
                partyManager.joinParty(player);
                break;
            case "leave":
            case "disband":
                partyManager.disband(player);
                break;
            case "list":
                partyManager.getOnlineMembers(player);
                break;
            default:
                player.sendMessage(Component.text("Unknown subcommand.").color(NamedTextColor.RED));
        }
        return true;
    }
}