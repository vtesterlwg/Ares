package com.riotmc.factions.players.manager;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.client.model.Filters;
import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.commons.bukkit.timer.Timer;
import com.riotmc.commons.bukkit.util.Players;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.Factions;
import com.riotmc.factions.addons.events.EventsAddon;
import com.riotmc.factions.addons.events.data.type.RiotEvent;
import com.riotmc.factions.addons.events.data.type.koth.KOTHEvent;
import com.riotmc.factions.players.dao.PlayerDAO;
import com.riotmc.factions.players.data.FactionPlayer;
import com.riotmc.factions.players.handlers.PlayerDisplayHandler;
import com.riotmc.factions.players.handlers.PlayerTimerHandler;
import com.riotmc.factions.timers.PlayerTimer;
import com.riotmc.factions.timers.cont.player.ProtectionTimer;
import com.riotmc.services.automatedrestarts.AutomatedRestartService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PlayerManager {
    @Getter public final Factions plugin;
    /** Handles Player Timers **/
    @Getter public final PlayerTimerHandler timerHandler;
    /** Handles Player Displays **/
    @Getter public final PlayerDisplayHandler displayHandler;
    /** Handles Faction Timers **/
    @Getter public final Set<FactionPlayer> playerRepository;
    /** Performs HUD rendering **/
    @Getter public final BukkitTask actionBarUpdated;
    /** Performs timer updating **/
    @Getter public final BukkitTask timerUpdater;
    /** Performs tab rendering **/
    @Getter public final BukkitTask tabUpdater;

    public PlayerManager(Factions plugin) {
        final AutomatedRestartService restartService = (AutomatedRestartService)plugin.getService(AutomatedRestartService.class);
        final EventsAddon eventAddon = (EventsAddon)plugin.getAddonManager().getAddon(EventsAddon.class);
        this.plugin = plugin;
        this.timerHandler = new PlayerTimerHandler(this);
        this.displayHandler = new PlayerDisplayHandler(this);
        this.playerRepository = Sets.newConcurrentHashSet();

        this.actionBarUpdated = new Scheduler(plugin).async(() -> playerRepository.forEach(profile -> {
            List<String> hudElements = null;

            if (eventAddon != null && !eventAddon.getManager().getActiveEvents().isEmpty()) {
                hudElements = Lists.newArrayList();

                for (RiotEvent event : eventAddon.getManager().getActiveEvents()) {
                    final StringBuilder eventElement = new StringBuilder();

                    eventElement.append(event.getDisplayName()).append(" ").append(ChatColor.RED);

                    if (event instanceof KOTHEvent) {
                        final KOTHEvent koth = (KOTHEvent)event;
                        eventElement.append(Time.convertToHHMMSS(koth.getSession().getTimer().getRemaining()));
                    }

                    hudElements.add(eventElement.toString());
                }
            }

            // Restarts are not part of the typical HUD elements
            if (restartService != null && restartService.isInProgress()) {
                if (hudElements == null) {
                    hudElements = Lists.newArrayList();
                }

                hudElements.add(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Server Restart" + " " + ChatColor.RED + Time.convertToHHMMSS(restartService.getTimeUntilReboot()));
            }

            if (!profile.getTimers().isEmpty()) {
                for (PlayerTimer timer : profile.getTimers().stream().filter(timer -> !timer.isExpired() && timer.getType().isRender()).collect(Collectors.toList())) {
                    if (hudElements == null) {
                        hudElements = Lists.newArrayList();
                    }

                    hudElements.add(timer.getType().getDisplayName() + " " + ChatColor.RED + (timer.getType().isDecimal() ? Time.convertToDecimal(timer.getRemaining()) : Time.convertToHHMMSS(timer.getRemaining())));
                }
            }

            if (hudElements != null && !hudElements.isEmpty()) {
                profile.sendActionBar(Joiner.on(ChatColor.RESET + " " + ChatColor.RESET + " ").join(hudElements));
            }
        })).repeat(0L, 1L).run();

        this.timerUpdater = new Scheduler(plugin).async(() -> playerRepository
                .stream()
                .filter(profile -> !profile.getTimers().isEmpty())
                .forEach(profile -> profile.getTimers()
                        .stream()
                        .filter(timer -> !timer.isFrozen())
                        .filter(Timer::isExpired)
                        .forEach(expired -> new Scheduler(plugin).sync(() -> {

            expired.onFinish();
            profile.getTimers().remove(expired);

        }).run()))).repeat(0L, 5L).run();

        this.tabUpdater = new Scheduler(plugin).async(() -> {
            // TODO: Add koth info and other display information
            Bukkit.getOnlinePlayers().forEach(player -> Players.sendTablist(plugin.getProtocol(), player, ChatColor.DARK_RED + "" + ChatColor.BOLD + "RiotMC", ChatColor.RED + "riotmc.com"));
        }).repeat(0L, 60 * 20L).run();
    }

    /**
     * Cancels all tasks running under this manager
     */
    public void cancelTasks() {
        if (this.actionBarUpdated != null) {
            this.actionBarUpdated.cancel();
        }

        if (this.timerUpdater != null) {
            this.timerUpdater.cancel();
        }
    }

    /**
     * Loads or creates a new FactionPlayer profile based on the provided Unique ID and Username
     *
     * Warning: This method accesses a db and should not be called on the main thread
     * @param uniqueId Unique ID
     * @param username Username
     * @return FactionPlayer
     */
    public FactionPlayer loadPlayer(UUID uniqueId, String username) {
        FactionPlayer profile = PlayerDAO.getPlayer(plugin, plugin.getMongo(), Filters.eq("id", uniqueId));

        if (profile == null) {
            profile = new FactionPlayer(plugin, uniqueId, username);
            profile.getTimers().add(new ProtectionTimer(plugin, uniqueId, plugin.getFactionConfig().getTimerProtection()));
        }

        return profile;
    }

    /**
     * Saves all players to the db
     * @param blocking If true the main thread will be blocked
     */
    public void savePlayers(boolean blocking) {
        Logger.print("Saving " + playerRepository.size() + " Players");

        if (blocking) {
            for (FactionPlayer profile : playerRepository) {
                PlayerDAO.savePlayer(plugin.getMongo(), profile);
            }

            Logger.print("Finished saving players");
            return;
        }

        for (FactionPlayer profile : playerRepository) {
            PlayerDAO.savePlayer(plugin.getMongo(), profile);
        }

        Logger.print("Finished saving players");
    }

    /**
     * Returns a FactionPlayer matching the provided Unique ID
     * @param uniqueId Unique ID
     * @return FactionPlayer
     */
    public FactionPlayer getPlayer(UUID uniqueId) {
        return playerRepository.stream().filter(player -> player.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    /**
     * Returns a FactionPlayer matching the provided username
     * @param username Username
     * @return FactionPlayer
     */
    public FactionPlayer getPlayer(String username) {
        return playerRepository.stream().filter(player -> player.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }
}
