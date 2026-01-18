package com.mmhs.dungeons.items;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class CrownInventoryHolder implements InventoryHolder {
    private final Inventory inventory;

    public CrownInventoryHolder(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
