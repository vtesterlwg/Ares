package com.playares.factions.players.manager;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.timer.Timer;
import com.playares.commons.bukkit.util.Players;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.factions.players.dao.PlayerDAO;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.factions.players.handlers.PlayerTimerHandler;
import com.playares.factions.timers.PlayerTimer;
import com.playares.factions.timers.cont.player.ProtectionTimer;
import com.playares.services.automatedrestarts.AutomatedRestartService;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PlayerManager {
    @Getter public final Factions plugin;
    /** Handles Player Timers **/
    @Getter public final PlayerTimerHandler timerHandler;
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
        this.playerRepository = Sets.newConcurrentHashSet();

        this.actionBarUpdated = new Scheduler(plugin).async(() -> playerRepository.forEach(profile -> {
            List<String> hudElements = Lists.newArrayList();

            // Player Timers
            if (!profile.getTimers().isEmpty()) {
                for (PlayerTimer timer : profile.getTimers().stream().filter(timer -> !timer.isExpired() && timer.getType().isRender()).collect(Collectors.toList())) {
                    hudElements.add(timer.getType().getDisplayName() + " " + ChatColor.RED + (timer.getType().isDecimal() ? Time.convertToDecimal(timer.getRemaining()) : Time.convertToHHMMSS(timer.getRemaining())));
                }
            }

            // Restarts are not part of the typical HUD elements
            if (restartService != null && restartService.isInProgress()) {
                hudElements.add(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Server Restart" + " " + ChatColor.RED + Time.convertToHHMMSS(restartService.getTimeUntilReboot()));
            }

            // Event Timers
            if (eventAddon != null && !eventAddon.getManager().getActiveEvents().isEmpty()) {
                for (AresEvent event : eventAddon.getManager().getActiveEvents()) {
                    final StringBuilder eventElement = new StringBuilder();

                    eventElement.append(event.getDisplayName()).append(" ").append(ChatColor.RED);

                    if (event instanceof KOTHEvent) {
                        final KOTHEvent koth = (KOTHEvent)event;

                        if (koth.getSession().isContested()) {
                            eventElement.append("Contested");
                            hudElements.add(eventElement.toString());
                            continue;
                        }

                        final long remainingMs = koth.getSession().getTimer().getRemaining();

                        if (remainingMs <= 0.0) {
                            eventElement.append(ChatColor.RED).append("Capturing...");
                        } else if (remainingMs <= (10 * 1000L)) {
                            eventElement.append(Time.convertToDecimal(remainingMs));
                        } else {
                            eventElement.append(Time.convertToHHMMSS(remainingMs));
                        }

                        hudElements.add(eventElement.toString());
                    }
                }
            }

            if (!hudElements.isEmpty()) {
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

        this.tabUpdater = new Scheduler(plugin).sync(() -> Bukkit.getOnlinePlayers().forEach(this::sendTabDisplay)).repeat(0L, 20L).run();
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
     * Sends the tab header and footer to the player
     * @param player Player
     */
    public void sendTabDisplay(Player player) {
        final FactionPlayer factionPlayer = getPlayer(player.getUniqueId());
        final PlayerFaction faction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());
        final String location = (factionPlayer != null && factionPlayer.getCurrentClaim() != null) ? getPlugin().getFactionManager().getFactionById(factionPlayer.getCurrentClaim().getOwnerId()).getName() :
                getPlugin().getClaimManager().getWorldLocationManager().getWorldLocation(new PLocatable(player)).getDisplayName();

        final List<String> header = Lists.newArrayList();
        final List<String> footer = Lists.newArrayList();

        header.add(ChatColor.DARK_RED + "" + ChatColor.BOLD + "HCFRevival");

        if (faction != null) {
            footer.add(ChatColor.GOLD + faction.getName());
            footer.add(ChatColor.YELLOW + "Online: " + ChatColor.GRAY + faction.getOnlineMembers().size() + "/" + faction.getMembers().size());
            footer.add(ChatColor.YELLOW + "DTR: " + ChatColor.GRAY + String.format("%.2f", faction.getDeathsTilRaidable()));

            if (faction.getRally() != null) {
                footer.add(ChatColor.YELLOW + "Rally: " + ChatColor.GRAY +
                        "[" + faction.getRally().getBukkit().getBlockX() + ", " +
                        faction.getRally().getBukkit().getBlockY() + ", " +
                        faction.getRally().getBukkit().getBlockZ() + ", " +
                        StringUtils.capitaliseAllWords(faction.getRally().getBukkit().getWorld().getEnvironment().name())
                        + ChatColor.GRAY + "]");
            }

            footer.add(ChatColor.RESET + " ");
        }

        footer.add(ChatColor.YELLOW + "Your location: " + location);
        footer.add(ChatColor.GRAY + " [" + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ() + "]");

        footer.add(ChatColor.RESET + " ");
        footer.add(ChatColor.GRAY + "For map-specific info type " + ChatColor.LIGHT_PURPLE + "/map");
        footer.add(ChatColor.AQUA + "play.hcfrevival.net");

        new Scheduler(plugin).async(() -> Players.sendTablist(getPlugin().getProtocol(), player, Joiner.on("\n").join(header), Joiner.on("\n").join(footer))).run();
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
