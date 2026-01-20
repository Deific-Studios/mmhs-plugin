package com.mmhs.dungeons.generation;

import com.sk89q.worldedit.math.BlockVector3;

public class RotationMath {

    /**
     * Rotates a point around (0,0,0) and shifts it back to positive coordinates.
     */
    public static BlockVector3 rotate(BlockVector3 point, int rotation, int sizeX, int sizeZ) {
        int x = point.getX();
        int y = point.getY(); 
        int z = point.getZ();

        switch (rotation) {
            case 90:
                // Rotates 90 deg and shifts X back into positive
                return BlockVector3.at(-z + sizeZ - 1, y, x);
                
            case 180:
                // Rotates 180 deg and shifts X and Z back into positive
                return BlockVector3.at(sizeX - 1 - x, y, sizeZ - 1 - z);
                
            case 270:
                // Rotates 270 deg and shifts Z back into positive
                return BlockVector3.at(z, y, sizeX - 1 - x);
                
            default: // 0
                return point;
        }
    }

    /**
     * Returns the offset needed to correct WorldEdit's negative rotation coordinates.
     */
    public static BlockVector3 getTranslationOffset(int rotation, int sizeX, int sizeZ) {
         switch (rotation) {
            case 90:
                return BlockVector3.at(sizeZ - 1, 0, 0);
            case 180:
                return BlockVector3.at(sizeX - 1, 0, sizeZ - 1);
            case 270:
                return BlockVector3.at(0, 0, sizeX - 1);
            default:
                return BlockVector3.ZERO;
        }
    }
}