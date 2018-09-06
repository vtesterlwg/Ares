package com.playares.factions.players;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.timer.Timer;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.players.handlers.PlayerTimerHandler;
import com.playares.factions.timers.PlayerTimer;
import com.playares.factions.timers.cont.player.ProtectionTimer;
import com.playares.services.automatedrestarts.AutomatedRestartService;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PlayerManager {
    @Getter
    public final Factions plugin;

    @Getter
    public final PlayerTimerHandler timerHandler;

    @Getter
    public final Set<FactionPlayer> playerRepository;

    @Getter
    public final BukkitTask displayUpdater;

    @Getter
    public final BukkitTask timerUpdater;

    public PlayerManager(Factions plugin) {
        final AutomatedRestartService restartService = (AutomatedRestartService)plugin.getService(AutomatedRestartService.class);
        this.plugin = plugin;
        this.timerHandler = new PlayerTimerHandler(this);
        this.playerRepository = Sets.newConcurrentHashSet();

        this.displayUpdater = new Scheduler(plugin).async(() -> playerRepository.forEach(profile -> {
            List<String> hudElements = null;

            if (restartService != null && restartService.isInProgress()) {
                hudElements = Lists.newArrayList();
                hudElements.add(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Reboot" + " " + ChatColor.RED + Time.convertToHHMMSS(restartService.getTimeUntilReboot()));
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

        this.timerUpdater = new Scheduler(plugin).async(() -> playerRepository.stream().filter(profile -> !profile.getTimers().isEmpty()).forEach(profile -> profile.getTimers().stream().filter(Timer::isExpired).forEach(expired -> new Scheduler(plugin).sync(() -> {
            expired.onFinish();
            profile.getTimers().remove(expired);
        }).run()))).repeat(0L, 5L).run();
    }

    public void cancelTasks() {
        if (this.displayUpdater != null) {
            this.displayUpdater.cancel();
        }

        if (this.timerUpdater != null) {
            this.timerUpdater.cancel();
        }
    }

    public FactionPlayer loadPlayer(UUID uniqueId, String username) {
        FactionPlayer profile = PlayerDAO.getPlayer(plugin.getMongo(), Filters.eq("id", uniqueId));

        if (profile == null) {
            profile = new FactionPlayer(uniqueId, username);
            profile.getTimers().add(new ProtectionTimer(uniqueId, plugin.getFactionConfig().getTimerProtection()));
        }

        return profile;
    }

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

    public FactionPlayer getPlayer(UUID uniqueId) {
        return playerRepository.stream().filter(player -> player.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public FactionPlayer getPlayer(String username) {
        return playerRepository.stream().filter(player -> player.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }
}
