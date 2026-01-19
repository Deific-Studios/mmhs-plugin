# MMHS Dungeons Plugin

A Minecraft Paper plugin (1.21.x) that provides dungeon management with procedural generation from pre-built room templates.

## Features

### Dungeon Management
- Create named dungeons with spawn points
- Wave-based mob spawning system
- Start/stop dungeon instances
- Persistent storage in YAML

### Procedural Generation
- **Template-based rooms** - Build rooms with red concrete floors and gold block door markers
- **Smart rotation** - Automatically rotates rooms (0°, 90°, 180°, 270°) to align doors
- **Collision detection** - Prevents overlap while allowing red concrete/gold blocks to merge at doorways
- **Starter room detection** - Closest room to origin becomes entry with single exit
- **Template reuse** - Generates dungeons of any size from available templates

## Commands

### Dungeon Management
- `/dungeon create <name>` - Create a new dungeon
- `/dungeon addspawn <name>` - Add spawn point at your location (player only)
- `/dungeon start <name>` - Begin wave-based spawning
- `/dungeon stop <name>` - Stop active dungeon run
- `/dungeon list` - List all dungeons

### Procedural Generation
- `/gendungeon <numRooms> [scanRadius]` - Generate dungeon from templates
  - `numRooms`: Number of rooms to place
  - `scanRadius`: Blocks to scan from origin (default: 200)
  - Requires: `dungeons.generate` permission



## Building Room Templates

### Step 1: Room Floors
1. Build in **positive X/Z** coordinates, otherwise your dungeon will not be detected (e.g., x:10, z:10)
2. Use **red concrete** for floor outlines at **y=-61**
3. Separate rooms by **at least 1 block** to prevent merging

### Step 2: Door Markers
1. Place **2 adjacent gold blocks** above red concrete
2. Gold pairs define 2-wide doors (horizontal only)
3. Each dungeon needs at least one room with only one door (Starter room & Boss room)

### Step 3: Interior
1. Build walls/ceiling **between and above** red concrete
2. Max height: **10 blocks** above floor
3. All blocks in bounding box are copied
