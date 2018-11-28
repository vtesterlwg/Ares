package com.riotmc.factions.addons.events.menu;

import com.riotmc.commons.bukkit.item.ItemBuilder;
import com.riotmc.commons.bukkit.menu.Menu;
import com.riotmc.factions.addons.events.EventsAddon;
import com.riotmc.factions.addons.events.type.RiotEvent;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;

public final class EventMenu extends Menu {
    @Getter public final EventsAddon addon;

    public EventMenu(EventsAddon addon, Player player) {
        super(addon.getPlugin(), player, "Events", 1);
        this.addon = addon;
    }

    public void update() {
        final List<RiotEvent> events = addon.getManager().getEventsAlphabetical();

        clearInventory();

        events.forEach(event -> {
            final ItemBuilder builder = new ItemBuilder();
        });
    }

    @Override
    public void open() {
        super.open();
    }
}
