package com.mmhs.dungeons.core;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class ChatListener implements Listener {
    private final MessageManager messageManager;

    public ChatListener(MessageManager messageManager, Plugin plugin) {
        this.messageManager = messageManager;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (messageManager.isPartyChatToggled(event.getPlayer())) {
            event.setCancelled(true);
            
            // Convert Component back to string for simple party processing
            String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
            messageManager.sendPartyMessage(event.getPlayer(), rawMessage);
        }
    }
}