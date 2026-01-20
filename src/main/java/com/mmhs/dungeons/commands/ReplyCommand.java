package com.mmhs.dungeons.commands;

import com.mmhs.dungeons.core.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReplyCommand implements CommandExecutor {
    private final MessageManager messageManager;

    public ReplyCommand(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /r <message>").color(NamedTextColor.RED));
            return true;
        }

        String message = String.join(" ", args);
        messageManager.reply(player, message);
        return true;
    }
}