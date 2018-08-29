package com.playares.arena.team;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.player.PlayerStatus;
import com.playares.arena.scoreboard.ArenaScoreboard;
import com.playares.arena.stats.StatisticHolder;
import com.playares.arena.stats.TeamStatisticHolder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class Team implements StatisticHolder, TeamStatisticHolder {
    @Nonnull @Getter
    public final UUID uniqueId;

    @Nonnull @Getter @Setter
    public ArenaPlayer leader;

    @Nonnull @Getter @Setter
    public TeamStatus status;

    @Nonnull @Getter
    public final ArenaScoreboard scoreboard;

    @Nonnull @Getter
    public final Set<ArenaPlayer> members;

    @Nonnull @Getter
    public final Set<ArenaPlayer> invites;

    @Getter @Setter
    public boolean open;

    @Getter @Setter
    public int usedHealthPotions;

    @Getter @Setter
    public int hits;

    @Getter @Setter
    public double damage;

    public Team(@Nonnull ArenaPlayer player) {
        Preconditions.checkArgument(player.getPlayer() != null, "Team creator is not online");

        this.uniqueId = UUID.randomUUID();
        this.leader = player;
        this.status = TeamStatus.LOBBY;
        this.scoreboard = new ArenaScoreboard();
        this.members = Sets.newConcurrentHashSet();
        this.invites = Sets.newConcurrentHashSet();
        this.open = false;
        this.members.add(player);
        this.scoreboard.getFriendlyTeam().addEntry(player.getUsername());
    }

    @Override
    public void addUsedHealthPotion() {
        setUsedHealthPotions(getUsedHealthPotions() + 1);
    }

    @Override
    public void addHit() {
        setHits(getHits() + 1);
    }

    @Override
    public void addDamage(double amount) {
        setDamage(getDamage() + amount);
    }

    public String getName() {
        return "Team " + leader.getUsername();
    }

    public boolean hasInvite(ArenaPlayer player) {
        return invites.contains(player);
    }

    public ImmutableSet<ArenaPlayer> getAlive() {
        return ImmutableSet.copyOf(members.stream().filter(member -> member.getPlayer() != null && member.getStatus().equals(PlayerStatus.INGAME)).collect(Collectors.toSet()));
    }

    public void resetStats() {
        this.usedHealthPotions = 0;
        this.hits = 0;
        this.damage = 0.0;
    }

    public void sendMessage(@Nonnull String message) {
        members.forEach(member -> {
            final Player player = member.getPlayer();

            if (player != null) {
                player.sendMessage(message);
            }
        });
    }

    public void teleport(@Nonnull Location location) {
        members.forEach(member -> {
            final Player player = member.getPlayer();

            if (player != null) {
                player.teleport(location);
            }
        });
    }

    public void setStatuses(@Nonnull PlayerStatus status) {
        members.forEach(member -> member.setStatus(status));
    }
}