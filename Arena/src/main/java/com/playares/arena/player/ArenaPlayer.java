package com.playares.arena.player;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.arena.report.PlayerReport;
import com.playares.arena.timer.PlayerTimer;
import com.playares.commons.base.util.Time;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ArenaPlayer {
    @Getter public final UUID uniqueId;
    @Getter public final String username;
    @Getter @Setter public PlayerStatus status;
    @Getter @Setter public PlayerReport activeReport;
    @Getter public final Set<PlayerTimer> timers;

    public ArenaPlayer(Player player) {
        this.uniqueId = player.getUniqueId();
        this.username = player.getName();
        this.status = PlayerStatus.LOBBY;
        this.activeReport = null;
        this.timers = Sets.newConcurrentHashSet();
    }

    public ArenaPlayer(UUID uniqueId, String username) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.status = PlayerStatus.LOBBY;
        this.activeReport = null;
        this.timers = Sets.newConcurrentHashSet();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    public boolean hasTimer(PlayerTimer.PlayerTimerType type) {
        return timers.stream().anyMatch(timer -> timer.getType().equals(type));
    }

    public PlayerTimer getTimer(PlayerTimer.PlayerTimerType type) {
        return timers.stream().filter(timer -> timer.getType().equals(type)).findFirst().orElse(null);
    }

    public void addTimer(PlayerTimer timer) {
        if (hasTimer(timer.getType())) {
            final PlayerTimer existing = getTimer(timer.getType());
            existing.setExpire(timer.getExpire());
            return;
        }

        timers.add(timer);
    }

    public void removeTimer(PlayerTimer.PlayerTimerType type) {
        if (!hasTimer(type)) {
            return;
        }

        final PlayerTimer timer = getTimer(type);

        timers.remove(timer);
    }

    public void update() {
        final List<String> hudElements = Lists.newArrayList();

        if (!timers.isEmpty()) {
            timers.stream().filter(timer -> !timer.isExpired() && timer.getType().isRender()).forEach(timer ->
                    hudElements.add(timer.getType().getDisplayName() + " " + ChatColor.RED + (timer.getType().isDecimal() ?
                            Time.convertToDecimal(timer.getRemaining()) : Time.convertToHHMMSS(timer.getRemaining()))));
        }

        if (!hudElements.isEmpty()) {
            getPlayer().sendActionBar(Joiner.on(ChatColor.RESET + " " + ChatColor.RESET + " ").join(hudElements));
        }

        timers.stream().filter(timer -> timer.isExpired() && !timer.isFrozen()).forEach(timer -> {
            timer.onFinish();
            timers.remove(timer);
        });
    }

    public enum PlayerStatus {
        LOBBY, INGAME, INGAME_DEAD, SPECTATING
    }
}