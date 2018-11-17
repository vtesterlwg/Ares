package com.playares.commons.bukkit.menu;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.RiotPlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

public class Menu implements Listener {
    @Nonnull @Getter
    public RiotPlugin plugin;

    @Nonnull @Getter
    public final Player player;

    @Nonnull @Getter
    public final Inventory inventory;

    @Nonnull @Getter
    public final Set<ClickableItem> items;

    public Menu(@Nonnull RiotPlugin plugin, @Nonnull Player player, @Nonnull String title, int rows) {
        Preconditions.checkArgument((rows > 0 && rows < 7), "Rows must be 1-7");
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
        this.items = Sets.newConcurrentHashSet();

        plugin.registerListener(this);
    }

    /**
     * @return True if this inventory has viewers
     */
    public boolean isOpen() {
        return !getInventory().getViewers().isEmpty();
    }

    /**
     * Returns the ClickableItem at the given slot position
     * @param position Slot
     * @return ClickableItem, null if no item is found
     */
    public ClickableItem getItemAtPosition(int position) {
        return items.stream().filter(item -> item.getPosition() == position).findFirst().orElse(null);
    }

    /**
     * Returns the first empty slot in this inventory
     * @return First empty slot in the inventory, -1 if no slot is empty
     */
    public int getFirstEmpty() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (getItemAtPosition(i) == null) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Add an item to the menu
     * @param item ClickableItem
     */
    public void addItem(ClickableItem item) {
        if (getItemAtPosition(item.getPosition()) != null) {
            removeItem(item.getPosition());
        }

        items.add(item);
        inventory.setItem(item.getPosition(), item.getItem());
    }

    /**
     * Add a collection of items to the menu
     * @param items ClickableItems
     */
    public void addItem(Collection<ClickableItem> items) {
        items.forEach(this::addItem);
    }

    /**
     * Remove an item from the menu
     * @param item ClickableItem
     */
    public void removeItem(@Nonnull ClickableItem item) {
        items.remove(item);
        inventory.setItem(item.getPosition(), null);
    }

    /**
     * Remove an item at the given slot position
     * @param position Slot Position
     */
    public void removeItem(int position) {
        final ClickableItem item = getItemAtPosition(position);

        if (item == null) {
            return;
        }

        items.remove(item);
        inventory.setItem(position, null);
    }

    /**
     * Fill every open slot with an ItemStack
     * @param item ItemStack
     */
    public void fill(@Nonnull ItemStack item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            final ClickableItem itemAt = getItemAtPosition(i);

            if (itemAt != null) {
                continue;
            }

            addItem(new ClickableItem(item, i, click -> {}));
        }
    }

    /**
     * Remove every item in this menu
     */
    public void clearInventory() {
        inventory.clear();
        items.clear();
    }

    /**
     * Open the menu
     */
    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        InventoryCloseEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        final ClickType type = event.getClick();
        final ClickableItem item = getItemAtPosition(event.getRawSlot());

        event.setCancelled(true);

        if (item == null) {
            return;
        }

        item.getResult().click(type);
    }
}