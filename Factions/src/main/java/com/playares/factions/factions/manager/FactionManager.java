package com.playares.factions.factions.manager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.commons.base.promise.Promise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.timer.Timer;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.factions.dao.FactionDAO;
import com.playares.factions.factions.data.Faction;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.factions.factions.data.ServerFaction;
import com.playares.factions.factions.handlers.*;
import com.playares.services.profiles.ProfileService;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class FactionManager {
    /** Owner of this manager **/
    @Getter public final Factions plugin;
    /** Handles all faction creation tasks **/
    @Getter public final FactionCreationHandler createHandler;
    /** Handles all faction disbanding tasks **/
    @Getter public final FactionDisbandHandler disbandHandler;
    /** Handles all faction display tasks **/
    @Getter public final FactionDisplayHandler displayHandler;
    /** Handles all faction management tasks **/
    @Getter public final FactionManageHandler manageHandler;
    /** Handles all faction staff tasks **/
    @Getter public final FactionStaffHandler staffHandler;
    /** Handles all faction chat tasks **/
    @Getter public final FactionChatHandler chatHandler;
    /** Handles all faction economy tasks **/
    @Getter public final FactionEconomyHandler economyHandler;
    /** Performs faction ticking **/
    @Getter public final BukkitTask factionTicker;
    /** Contains a cache of every loaded faction **/
    @Getter public final Set<Faction> factionRepository;

    public FactionManager(Factions plugin) {
        this.plugin = plugin;

        // Handlers
        this.createHandler = new FactionCreationHandler(this);
        this.disbandHandler = new FactionDisbandHandler(this);
        this.displayHandler = new FactionDisplayHandler(this);
        this.manageHandler = new FactionManageHandler(this);
        this.staffHandler = new FactionStaffHandler(this);
        this.chatHandler = new FactionChatHandler(this);
        this.economyHandler = new FactionEconomyHandler(this);

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

    /**
     * Cancels all tasks for this manager
     */
    public void cancelTasks() {
        if (this.factionTicker != null) {
            this.factionTicker.cancel();
        }
    }

    /**
     * Loads all factions to memory
     */
    public void loadFactions() {
        factionRepository.addAll(FactionDAO.getFactions(plugin, plugin.getMongo()));
        Logger.print("Loaded " + factionRepository.size() + " Factions");
    }

    /**
     * Saves all factions to the db
     * @param blocking Block the main-thread
     */
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

    /**
     * Returns a Faction matching the provided Unique ID
     * @param uniqueId Unique ID
     * @return Faction
     */
    public Faction getFactionById(UUID uniqueId) {
        return factionRepository.stream().filter(f -> f.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    /**
     * Returns a Faction matching the provided name
     * @param name Name
     * @return Faction
     */
    public Faction getFactionByName(String name) {
        return factionRepository.stream().filter(f -> f.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Returns a PlayerFaction matching the provided Unique ID
     * @param uniqueId Unique ID
     * @return PlayerFaction
     */
    public PlayerFaction getPlayerFactionById(UUID uniqueId) {
        return (PlayerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof PlayerFaction)
                .filter(pf -> pf.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns a PlayerFaction matching the provided name
     * @param name Name
     * @return PlayerFaction
     */
    public PlayerFaction getPlayerFactionByName(String name) {
        return (PlayerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof PlayerFaction)
                .filter(pf -> pf.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns a PlayerFaction that has a member matching the provided Player Unique ID
     * @param playerId Player Unique ID
     * @return PlayerFaction
     */
    public PlayerFaction getFactionByPlayer(UUID playerId) {
        return (PlayerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof PlayerFaction)
                .filter(pf -> ((PlayerFaction) pf).isMember(playerId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns a PlayerFaction that has a member matching the provided Player username
     *
     * Returns in a promise/callback because username must be pulled from the Profile Service
     *
     * Will return null if the Profile Service is not running
     *
     * @param username Username
     * @param promise Promise
     */
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

    /**
     * Returns a ServerFaction matching the provided Unique ID
     * @param uniqueId Unique ID
     * @return ServerFaction
     */
    public ServerFaction getServerFactionById(UUID uniqueId) {
        return (ServerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof ServerFaction)
                .filter(sf -> sf.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns a ServerFaction matching the provided name
     * @param name Name
     * @return ServerFaction
     */
    public ServerFaction getServerFactionByName(String name) {
        return (ServerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof ServerFaction)
                .filter(sf -> sf.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns a collection of every PlayerFaction currently in memory
     * @return ImmutableList containing PlayerFactions
     */
    public ImmutableList<PlayerFaction> getPlayerFactions() {
        final List<PlayerFaction> result = Lists.newArrayList();

        factionRepository.stream().filter(f -> f instanceof PlayerFaction).forEach(pf -> {
            final PlayerFaction faction = (PlayerFaction)pf;
            result.add(faction);
        });

        return ImmutableList.copyOf(result);
    }

    /**
     * Returns a collection of every ServerFaction currently in memory
     * @return ImmutableList containing ServerFactions
     */
    public ImmutableList<ServerFaction> getServerFactions() {
        final List<ServerFaction> result = Lists.newArrayList();

        factionRepository.stream().filter(f -> f instanceof ServerFaction).forEach(sf -> {
            final ServerFaction faction = (ServerFaction)sf;
            result.add(faction);
        });

        return ImmutableList.copyOf(result);
    }
}