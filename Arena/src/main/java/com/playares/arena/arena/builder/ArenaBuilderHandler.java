package com.playares.arena.arena.builder;

import com.playares.arena.arena.data.Arena;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.logger.Logger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class ArenaBuilderHandler {
    @Getter public ArenaBuilderManager builderManager;

    public void create(Player player, String name, String displayName, SimplePromise promise) {
        final Arena existing = builderManager.getManager().getArena(name);
        final ArenaBuilder existingBuilder = builderManager.getBuilder(player);

        if (existingBuilder != null) {
            promise.failure("You are already building an arena");
            return;
        }

        if (existing != null) {
            promise.failure("Arena with this name already exists");
            return;
        }

        final ArenaBuilder builder = new ArenaBuilder(builderManager.getManager().getPlugin(), player.getUniqueId(), name, displayName);
        builderManager.getBuilders().add(builder);

        player.sendMessage(ChatColor.YELLOW + "Stand where specators should spawn and type /arena set");

        Logger.print(player.getName() + " is now building Arena: " + name);

        promise.success();
    }

    public void set(Player player, SimplePromise promise) {
        final ArenaBuilder builder = getBuilderManager().getBuilder(player);

        if (builder == null) {
            promise.failure("You are not actively building an event");
            return;
        }

        if (builder.getCurrentState().equals(ArenaBuilder.ArenaBuilderState.SET_SPEC)) {
            builder.setSpectatorSpawn(new PLocatable(player));
            promise.success();
            return;
        }

        if (builder.getCurrentState().equals(ArenaBuilder.ArenaBuilderState.SET_A)) {
            builder.setSpawnA(new PLocatable(player));
            promise.success();
            return;
        }

        if (builder.getCurrentState().equals(ArenaBuilder.ArenaBuilderState.SET_B)) {
            builder.setSpawnB(new PLocatable(player));

            final Arena arena = builder.build();
            arena.save();
            getBuilderManager().getManager().getArenas().add(arena);
            getBuilderManager().getBuilders().remove(builder);

            Logger.print(player.getName() + " has finished creating the arena: " + arena.getName());

            promise.success();
        }
    }
}