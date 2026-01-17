# MMHS Dungeons Plugin

Small Paper plugin (Minecraft 1.21.x) that provides lightweight dungeon support: create dungeons, add spawn points, and start simple wave-based spawns.

## Features
- Create named dungeons: `/dungeon create <name>`
- Add a spawn to a dungeon at your location: `/dungeon addspawn <name>` (player only)
- Start/stop a dungeon wave run: `/dungeon start <name>`, `/dungeon stop <name>`
- List all dungeons: `/dungeon list`

## Build

This project uses Maven. To build the plugin jar:

```bash
mvn -DskipTests package
```

Drop the resulting JAR from `target/` into your Paper server's `plugins/` folder and start the server.

## Notes
- This is a minimal example intended as a starting point. It stores dungeon data in `plugins/MMHSDungeons/dungeons.yml`.
- Adjust wave counts, mob types, and timings in `DungeonManager` as needed.
