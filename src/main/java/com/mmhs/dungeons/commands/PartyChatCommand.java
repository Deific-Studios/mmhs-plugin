package com.mmhs.dungeons.commands;

import com.mmhs.dungeons.core.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyChatCommand implements CommandExecutor {
    private final MessageManager messageManager;

    public PartyChatCommand(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        // Logic:
        // /p -> Toggles chat mode (same as /pc)
        // /p <msg> -> Sends quick message

        if (args.length == 0) {
            messageManager.togglePartyChat(player);
        } else {
            String message = String.join(" ", args);
            messageManager.sendPartyMessage(player, message);
        }
        return true;
    }
}