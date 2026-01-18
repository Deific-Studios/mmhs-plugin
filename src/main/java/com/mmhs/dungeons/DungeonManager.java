package com.mmhs.dungeons;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DungeonManager {
    private final DungeonsPlugin plugin;
    private final Map<String, Dungeon> dungeons = new ConcurrentHashMap<>();
    private final File dataFile;
    private final YamlConfiguration cfg;
    private final Map<String, BukkitRunnable> activeRuns = new HashMap<>();

    public DungeonManager(DungeonsPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "dungeons.yml");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        this.cfg = YamlConfiguration.loadConfiguration(dataFile);
        loadAll();
    }

    public Dungeon createDungeon(String name) {
        if (dungeons.containsKey(name.toLowerCase())) return null;
        Dungeon d = new Dungeon(name);
        dungeons.put(name.toLowerCase(), d);
        return d;
    }

    public Dungeon getDungeon(String name) {
        if (name == null) return null;
        return dungeons.get(name.toLowerCase());
    }

    public Collection<Dungeon> listDungeons() {
        return Collections.unmodifiableCollection(dungeons.values());
    }

    public void addSpawn(String dungeonName, Location loc) {
        Dungeon d = getDungeon(dungeonName);
        if (d == null) return;
        d.addSpawn(loc);
    }

    public void saveAll() {
        try {
            for (Dungeon d : dungeons.values()) {
                cfg.set("dungeons." + d.getName().toLowerCase(), d.serialize());
            }
            cfg.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed saving dungeons.yml: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadAll() {
        if (!dataFile.exists()) return;
        Object root = cfg.get("dungeons");
        if (!(root instanceof Map)) return;
        Map<String, Object> map = (Map<String, Object>) root;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            Object val = e.getValue();
            if (!(val instanceof Map)) continue;
            Dungeon d = Dungeon.deserialize((Map<String, Object>) val);
            if (d != null) dungeons.put(d.getName().toLowerCase(), d);
        }
    }

    public boolean startDungeon(String name) {
        Dungeon d = getDungeon(name);
        if (d == null) return false;
        if (activeRuns.containsKey(d.getName().toLowerCase())) return false; // already running
        if (d.getSpawns().isEmpty()) return false;

        final int maxWaves = 5;
        BukkitRunnable run = new BukkitRunnable() {
            private int wave = 0;

            @Override
            public void run() {
                if (wave >= maxWaves) {
                    cancel();
                    activeRuns.remove(d.getName().toLowerCase());
                    plugin.getLogger().info("Dungeon " + d.getName() + " finished.");
                    return;
                }
                wave++;
                plugin.getLogger().info("Starting wave " + wave + " for dungeon " + d.getName());
                for (Location s : d.getSpawns()) {
                    World w = s.getWorld();
                    if (w == null) continue;
                    for (int i = 0; i < Math.min(3 + wave, 8); i++) {
                        LivingEntity ent = (LivingEntity) w.spawnEntity(s, EntityType.ZOMBIE);
                        ent.customName(Component.text("Dungeon Mob"));
                        ent.setCustomNameVisible(false);
                    }
                }
            }
        };
        run.runTaskTimer(plugin, 0L, 20L * 10); // every 10 seconds
        activeRuns.put(d.getName().toLowerCase(), run);
        return true;
    }

    public boolean stopDungeon(String name) {
        BukkitRunnable r = activeRuns.remove(name.toLowerCase());
        if (r != null) {
            r.cancel();
            return true;
        }
        return false;
    }
}
