package com.playares.services.humbug.features.cont;

import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

public final class InfiniteRepair implements HumbugModule, Listener {
    @Getter
    public final HumbugService humbug;

    @Getter @Setter
    public boolean enabled;

    public InfiniteRepair(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public String getName() {
        return "Infinite Item Repairs";
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("modules.infinite-repair.enabled");
    }

    @Override
    public void start() {
        this.humbug.registerListener(this);
    }

    @Override
    public void stop() {
        InventoryMoveItemEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (event.getItem() == null || event.getItem().getType().equals(Material.AIR)) {
            return;
        }

        final ItemStack item = event.getItem();
        final ItemMeta meta = item.getItemMeta();

        if (!(event.getDestination() instanceof AnvilInventory)) {
            return;
        }

        if (!(meta instanceof Repairable)) {
            return;
        }

        final Repairable repairable = (Repairable)meta;

        if (!repairable.hasRepairCost()) {
            return;
        }

        if (repairable.getRepairCost() >= 39) {
            repairable.setRepairCost(39);
        }
    }
}