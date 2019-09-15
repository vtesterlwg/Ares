package com.playares.arena.team;

import com.destroystokyo.paper.Title;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class Team {
    @Getter public final Arenas plugin;
    @Getter public final UUID uniqueId;
    @Getter @Setter public ArenaPlayer leader;
    @Getter public final Set<ArenaPlayer> members;
    @Getter public final Set<ArenaPlayer> invitations;
    @Getter @Setter public boolean open;
    @Getter @Setter public TeamStatus status;
    @Getter @Setter public boolean disbanding;

    public Team(Arenas plugin, ArenaPlayer leader) {
        Preconditions.checkArgument(leader.getPlayer() != null, "Team creator is not online");

        this.plugin = plugin;
        this.uniqueId = UUID.randomUUID();
        this.leader = leader;
        this.members = Sets.newConcurrentHashSet();
        this.invitations = Sets.newConcurrentHashSet();
        this.open = false;
        this.status = TeamStatus.LOBBY;
        this.disbanding = false;

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

    public void sendTitle(Title title) {
        getAvailableMembers().forEach(member -> member.getPlayer().sendTitle(title));
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

    public void transferLeadership() {
        final List<ArenaPlayer> members = Lists.newArrayList(getAvailableMembers());

        if (members.size() <= 1) {
            if (status.equals(TeamStatus.IN_GAME)) {
                setDisbanding(true);
                return;
            }

            plugin.getTeamManager().getTeams().remove(this);
            return;
        }

        final ArenaPlayer leader = members.get(0);

        members.remove(this.leader);

        setLeader(leader);
        sendMessage(ChatColor.AQUA + leader.getUsername() + ChatColor.YELLOW + " has been randomly selected as the new leader of the team");
    }

    public enum TeamStatus {
        LOBBY, IN_GAME
    }
}