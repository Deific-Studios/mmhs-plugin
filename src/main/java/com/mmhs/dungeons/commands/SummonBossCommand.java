package com.mmhs.dungeons.commands;

import com.mmhs.dungeons.mobs.BossType;
import com.mmhs.dungeons.mobs.DungeonBossManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SummonBossCommand implements CommandExecutor, TabCompleter {

    private final DungeonBossManager bossManager;

    public SummonBossCommand(DungeonBossManager bossManager) {
        this.bossManager = bossManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can summon bosses.").color(NamedTextColor.RED));
            return true;
        }

        if (!player.isOp()) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /summonboss <type>").color(NamedTextColor.RED));
            return true;
        }

        String inputName = args[0].toUpperCase();
        BossType type;

        try {
            type = BossType.valueOf(inputName);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Component.text("Invalid Boss Type!").color(NamedTextColor.RED));
            player.sendMessage(Component.text("Valid options: " + getBossNames()).color(NamedTextColor.GRAY));
            return true;
        }

        player.sendMessage(Component.text("Summoning " + type.displayName + "...").color(NamedTextColor.GREEN));
        bossManager.spawnBoss(player.getLocation(), type);

        return true;
    }

    // --- Tab Completion (Auto-fills boss names) ---
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.stream(BossType.values())
                    .map(Enum::name)
                    .filter(name -> name.startsWith(args[0].toUpperCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private String getBossNames() {
        return Arrays.stream(BossType.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}