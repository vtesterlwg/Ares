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
import com.playares.factions.timers.cont.player.ProtectionTimer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.UUID;

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
        this.plugin = plugin;
        this.timerHandler = new PlayerTimerHandler(this);
        this.playerRepository = Sets.newConcurrentHashSet();

        this.displayUpdater = new Scheduler(plugin).async(() -> playerRepository.stream().filter(profile -> !profile.getTimers().isEmpty()).forEach(profile -> {
            final List<String> hudElements = Lists.newArrayList();
            profile.getTimers().stream().filter(timer -> !timer.isExpired() && timer.getType().isRender()).forEach(timer -> hudElements.add(timer.getType().getDisplayName() + " " + ChatColor.RED + (timer.getType().isDecimal() ? Time.convertToDecimal(timer.getRemaining()) : Time.convertToHHMMSS(timer.getRemaining()))));
            profile.sendActionBar(Joiner.on(ChatColor.RESET + " " + ChatColor.RESET + " ").join(hudElements));
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
