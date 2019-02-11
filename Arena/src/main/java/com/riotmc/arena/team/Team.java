package com.riotmc.arena.team;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.riotmc.arena.player.ArenaPlayer;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class Team {
    @Getter public final UUID uniqueId;
    @Getter public final ArenaPlayer leader;
    @Getter public final Set<ArenaPlayer> members;
    @Getter public final Set<ArenaPlayer> invitations;
    @Getter @Setter public boolean open;
    @Getter @Setter public TeamStatus status;

    public Team(ArenaPlayer leader) {
        Preconditions.checkArgument(leader.getPlayer() != null, "Team creator is not online");

        this.uniqueId = UUID.randomUUID();
        this.leader = leader;
        this.members = Sets.newConcurrentHashSet();
        this.invitations = Sets.newConcurrentHashSet();
        this.open = false;
        this.status = TeamStatus.LOBBY;

        this.members.add(leader);
    }

    public ImmutableList<ArenaPlayer> getAvailableMembers() {
        return ImmutableList.copyOf(members.stream().filter(member -> member.getPlayer() != null).collect(Collectors.toList()));
    }

    public void sendMessage(String message) {
        getAvailableMembers().forEach(member -> member.getPlayer().sendMessage(message));
    }

    public void sendMessage(TextComponent message) {
        getAvailableMembers().forEach(member -> member.getPlayer().sendMessage(message));
    }

    public void teleport(Location location) {
        getAvailableMembers().forEach(member -> member.getPlayer().teleport(location));
    }

    public void setStatuses(ArenaPlayer.PlayerStatus status) {
        getMembers().forEach(member -> member.setStatus(status));
    }

    public boolean isLeader(ArenaPlayer player) {
        return getLeader().equals(player);
    }

    public boolean isLeader(UUID uniqueId) {
        return getLeader().getUniqueId().equals(uniqueId);
    }

    public enum TeamStatus {
        LOBBY, IN_GAME
    }
}