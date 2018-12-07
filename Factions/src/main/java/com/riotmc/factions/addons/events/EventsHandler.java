package com.riotmc.factions.addons.events;

import com.riotmc.commons.base.promise.FailablePromise;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.factions.addons.events.menu.EventsMenu;
import lombok.Getter;
import org.bukkit.entity.Player;

public final class EventsHandler {
    @Getter public final EventsManager manager;

    public EventsHandler(EventsManager manager) {
        this.manager = manager;
    }

    public void list(Player player, FailablePromise<EventsMenu> promise) {

    }

    public void create(Player player, String name, SimplePromise promise) {

    }

    public void delete(Player player, String name, SimplePromise promise) {

    }

    public void rename(Player player, String currentName, String newName, SimplePromise promise) {

    }

    public void start(Player player, String name, SimplePromise promise) {

    }

    public void stop(Player player, String name, SimplePromise promise) {

    }

    public void set(Player player, String name, String type, int value, SimplePromise promise) {

    }
}