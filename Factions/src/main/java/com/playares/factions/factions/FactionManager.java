package com.playares.factions.factions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.commons.base.promise.Promise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.timer.Timer;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.factions.handlers.*;
import com.playares.services.profiles.ProfileService;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class FactionManager {
    @Getter
    public final Factions plugin;

    @Getter
    public final FactionCreationHandler createHandler;

    @Getter
    public final FactionDisbandHandler disbandHandler;

    @Getter
    public final FactionDisplayHandler displayHandler;

    @Getter
    public final FactionManageHandler manageHandler;

    @Getter
    public final FactionStaffHandler staffHandler;

    @Getter
    public final FactionChatHandler chatHandler;

    @Getter
    public final BukkitTask factionTicker;

    @Getter
    public final Set<Faction> factionRepository;

    public FactionManager(Factions plugin) {
        this.plugin = plugin;

        this.createHandler = new FactionCreationHandler(this);
        this.disbandHandler = new FactionDisbandHandler(this);
        this.displayHandler = new FactionDisplayHandler(this);
        this.manageHandler = new FactionManageHandler(this);
        this.staffHandler = new FactionStaffHandler(this);
        this.chatHandler = new FactionChatHandler(this);

        this.factionRepository = Sets.newConcurrentHashSet();

        this.factionTicker = new Scheduler(plugin)
                .async(() -> getPlayerFactions().forEach(faction -> {
                    if (faction.getNextTick() <= Time.now()) {
                        faction.tick();
                    }

                    if (!faction.getTimers().isEmpty()) {
                        faction.getTimers().stream().filter(Timer::isExpired).forEach(expired -> {
                            expired.onFinish();
                            faction.getTimers().remove(expired);
                        });
                    }
                })).repeat(0L, 20L).run();
    }

    public void cancelTasks() {
        if (this.factionTicker != null) {
            this.factionTicker.cancel();
        }
    }

    public void loadFactions() {
        factionRepository.addAll(FactionDAO.getFactions(plugin, plugin.getMongo()));
        Logger.print("Loaded " + factionRepository.size() + " Factions");
    }

    public void saveFactions(boolean blocking) {
        Logger.print("Saving " + factionRepository.size() + " Factions, Blocking = " + blocking);

        if (blocking) {
            FactionDAO.saveFactions(plugin.getMongo(), factionRepository);
            Logger.print("Finished saving factions");
            return;
        }

        new Scheduler(plugin).async(() -> {
            FactionDAO.saveFactions(plugin.getMongo(), factionRepository);
            Logger.print("Finished saving factions");
        }).run();
    }

    public Faction getFactionById(UUID uniqueId) {
        return factionRepository.stream().filter(f -> f.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public Faction getFactionByName(String name) {
        return factionRepository.stream().filter(f -> f.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public PlayerFaction getPlayerFactionById(UUID uniqueId) {
        return (PlayerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof PlayerFaction)
                .filter(pf -> pf.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    public PlayerFaction getPlayerFactionByName(String name) {
        return (PlayerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof PlayerFaction)
                .filter(pf -> pf.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public PlayerFaction getFactionByPlayer(UUID playerId) {
        return (PlayerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof PlayerFaction)
                .filter(pf -> ((PlayerFaction) pf).isMember(playerId))
                .findFirst()
                .orElse(null);
    }

    public void getFactionByPlayer(String username, Promise<PlayerFaction> promise) {
        final ProfileService profileService = (ProfileService)plugin.getService(ProfileService.class);

        if (profileService == null) {
            promise.ready(null);
            return;
        }

        profileService.getProfile(username, profile -> {
            if (profile == null) {
                promise.ready(null);
                return;
            }

            promise.ready(getFactionByPlayer(profile.getUniqueId()));
        });
    }

    public ServerFaction getServerFactionById(UUID uniqueId) {
        return (ServerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof ServerFaction)
                .filter(sf -> sf.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    public ServerFaction getServerFactionByName(String name) {
        return (ServerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof ServerFaction)
                .filter(sf -> sf.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public ImmutableList<PlayerFaction> getPlayerFactions() {
        final List<PlayerFaction> result = Lists.newArrayList();

        factionRepository.stream().filter(f -> f instanceof PlayerFaction).forEach(pf -> {
            final PlayerFaction faction = (PlayerFaction)pf;
            result.add(faction);
        });

        return ImmutableList.copyOf(result);
    }

    public ImmutableList<ServerFaction> getServerFactions() {
        final List<ServerFaction> result = Lists.newArrayList();

        factionRepository.stream().filter(f -> f instanceof ServerFaction).forEach(sf -> {
            final ServerFaction faction = (ServerFaction)sf;
            result.add(faction);
        });

        return ImmutableList.copyOf(result);
    }
}