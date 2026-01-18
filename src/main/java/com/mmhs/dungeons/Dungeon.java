package com.mmhs.dungeons;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class Dungeon {
    private final String name;
    private final List<Location> spawns = new ArrayList<>();

    public Dungeon(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addSpawn(Location loc) {
        spawns.add(loc);
    }

    public List<Location> getSpawns() {
        return Collections.unmodifiableList(spawns);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        List<Map<String, Object>> spawnList = new ArrayList<>();
        for (Location loc : spawns) {
            Map<String, Object> locMap = new HashMap<>();
            if (loc.getWorld() != null) {
                locMap.put("world", loc.getWorld().getName());
            }
            locMap.put("x", loc.getX());
            locMap.put("y", loc.getY());
            locMap.put("z", loc.getZ());
            locMap.put("yaw", loc.getYaw());
            locMap.put("pitch", loc.getPitch());
            spawnList.add(locMap);
        }
        map.put("spawns", spawnList);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Dungeon deserialize(Map<String, Object> map) {
        String name = (String) map.get("name");
        if (name == null) return null;

        Dungeon d = new Dungeon(name);
        Object spawnObj = map.get("spawns");
        if (spawnObj instanceof List) {
            List<Map<String, Object>> spawnList = (List<Map<String, Object>>) spawnObj;
            for (Map<String, Object> locMap : spawnList) {
                String worldName = (String) locMap.get("world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;

                double x = ((Number) locMap.get("x")).doubleValue();
                double y = ((Number) locMap.get("y")).doubleValue();
                double z = ((Number) locMap.get("z")).doubleValue();
                float yaw = ((Number) locMap.getOrDefault("yaw", 0)).floatValue();
                float pitch = ((Number) locMap.getOrDefault("pitch", 0)).floatValue();

                Location loc = new Location(world, x, y, z, yaw, pitch);
                d.addSpawn(loc);
            }
        }
        return d;
    }
}

