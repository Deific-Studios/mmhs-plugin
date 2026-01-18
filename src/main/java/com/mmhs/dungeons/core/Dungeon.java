package com.mmhs.dungeons.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;

public class Dungeon {
    private final String name;
    private final List<Location> spawns = new ArrayList<>();

    public Dungeon(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Location> getSpawns() {
        return spawns;
    }

    public void addSpawn(Location loc) {
        if (loc != null) spawns.add(loc);
    }

    // Serialization helper: convert to a map suitable for YamlConfiguration
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        List<Map<String, Object>> spawnMaps = new ArrayList<>();
        for (Location loc : spawns) {
            if (loc.getWorld() == null) continue;
            Map<String, Object> lm = new HashMap<>();
            lm.put("world", loc.getWorld().getName());
            lm.put("x", loc.getX());
            lm.put("y", loc.getY());
            lm.put("z", loc.getZ());
            lm.put("yaw", loc.getYaw());
            lm.put("pitch", loc.getPitch());
            spawnMaps.add(lm);
        }
        map.put("spawns", spawnMaps);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Dungeon deserialize(Map<String, Object> data) {
        String name = (String) data.get("name");
        Dungeon d = new Dungeon(name);
        Object maybeSpawns = data.get("spawns");
        if (maybeSpawns instanceof List) {
            for (Object o : (List<Object>) maybeSpawns) {
                if (!(o instanceof Map)) continue;
                Map<String, Object> lm = (Map<String, Object>) o;
                String world = (String) lm.get("world");
                Object xo = lm.get("x");
                Object yo = lm.get("y");
                Object zo = lm.get("z");
                Object yawO = lm.get("yaw");
                Object pitchO = lm.get("pitch");
                if (world == null || xo == null || yo == null || zo == null) continue;
                try {
                    double x = ((Number) xo).doubleValue();
                    double y = ((Number) yo).doubleValue();
                    double z = ((Number) zo).doubleValue();
                    float yaw = yawO == null ? 0f : ((Number) yawO).floatValue();
                    float pitch = pitchO == null ? 0f : ((Number) pitchO).floatValue();
                    org.bukkit.World w = org.bukkit.Bukkit.getWorld(world);
                    if (w == null) continue; // world not loaded
                    Location loc = new Location(w, x, y, z, yaw, pitch);
                    d.addSpawn(loc);
                } catch (ClassCastException ignored) {
                }
            }
        }
        return d;
    }
}

