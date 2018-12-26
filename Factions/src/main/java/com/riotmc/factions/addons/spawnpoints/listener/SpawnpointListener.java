package com.riotmc.factions.addons.spawnpoints.listener;

import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.addons.spawnpoints.SpawnpointAddon;
import com.riotmc.factions.addons.spawnpoints.data.Spawnpoint;
import com.riotmc.factions.claims.data.DefinedClaim;
import com.riotmc.factions.factions.data.ServerFaction;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class SpawnpointListener implements Listener {
    @Getter
    public final SpawnpointAddon addon;

    public SpawnpointListener(SpawnpointAddon addon) {
        this.addon = addon;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (!player.hasPlayedBefore()) {
            final Spawnpoint spawn = addon.getManager().getSpawnpoint(Spawnpoint.SpawnpointType.OVERWORLD);

            if (spawn != null) {
                new Scheduler(addon.getPlugin()).sync(() -> player.teleport(spawn.getBukkit())).delay(1L).run();
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        final Spawnpoint spawn = addon.getManager().getSpawnpoint(Spawnpoint.SpawnpointType.OVERWORLD);

        if (spawn != null) {
            event.setRespawnLocation(spawn.getBukkit());
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerPortal(PlayerPortalEvent event) {
        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (from.getWorld().getEnvironment().equals(World.Environment.NETHER) &&
        to.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
            final DefinedClaim fromInside = addon.getPlugin().getClaimManager().getClaimAt(new BLocatable(from.getWorld().getName(), from.getX(), from.getY(), from.getZ()));
            final DefinedClaim toInside = addon.getPlugin().getClaimManager().getClaimAt(new BLocatable(to.getWorld().getName(), to.getX(), to.getY(), to.getZ()));

            if (fromInside != null) {
                final ServerFaction owner = addon.getPlugin().getFactionManager().getServerFactionById(fromInside.getOwnerId());

                if (owner != null && owner.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    final Spawnpoint spawn = addon.getManager().getSpawnpoint(Spawnpoint.SpawnpointType.OVERWORLD);

                    if (spawn != null) {
                        event.getPortalTravelAgent().setCanCreatePortal(false);
                        new Scheduler(addon.getPlugin()).sync(() -> event.getPlayer().teleport(spawn.getBukkit())).delay(1L).run();
                        return;
                    }
                }
            }

            if (toInside != null) {
                final ServerFaction owner = addon.getPlugin().getFactionManager().getServerFactionById(toInside.getOwnerId());

                if (owner != null) {
                    event.getPortalTravelAgent().setCanCreatePortal(false);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        final Location from = event.getFrom();

        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) {
            if (from.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                final Spawnpoint spawn = addon.getManager().getSpawnpoint(Spawnpoint.SpawnpointType.END_ENTRANCE);

                if (spawn != null) {
                    // setTo doesn't work...
                    new Scheduler(addon.getPlugin()).sync(() -> event.getPlayer().teleport(spawn.getBukkit())).delay(1L).run();
                }
            }

            else {
                final Spawnpoint spawn = addon.getManager().getSpawnpoint(Spawnpoint.SpawnpointType.OVERWORLD);

                if (spawn != null) {
                    // setTo doesn't work...
                    new Scheduler(addon.getPlugin()).sync(() -> event.getPlayer().teleport(spawn.getBukkit())).delay(1L).run();
                }
            }
        }
    }
}