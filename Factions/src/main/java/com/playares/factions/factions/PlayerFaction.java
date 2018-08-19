package com.playares.factions.factions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.timer.BossTimer;
import com.playares.factions.Factions;
import com.playares.factions.addons.stats.Statistics;
import com.playares.factions.timers.FactionTimer;
import com.playares.factions.timers.cont.faction.DTRFreezeTimer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PlayerFaction implements Faction, MongoDocument<PlayerFaction> {
    @Getter
    public final Factions plugin;

    @Getter
    public UUID uniqueId;

    @Getter @Setter
    public String name;

    @Setter
    public String announcement;

    @Getter
    public PLocatable home;

    @Getter
    public PLocatable rally;

    @Getter @Setter
    public double balance;

    @Getter @Setter
    public double deathsTilRaidable;

    @Getter @Setter
    public int reinvites;

    @Getter
    public Set<FactionProfile> members;

    @Getter
    public Set<UUID> pendingInvites;

    @Getter
    public Set<UUID> memberHistory;

    @Getter
    public Set<FactionTimer> timers;

    @Getter
    public Scoreboard scoreboard;

    @Getter
    public Statistics stats;

    @Getter @Setter
    public long nextTick;

    public PlayerFaction(Factions plugin) {
        this.plugin = plugin;
        this.uniqueId = null;
        this.name = null;
        this.announcement = null;
        this.home = null;
        this.rally = null;
        this.balance = 0.0;
        this.deathsTilRaidable = plugin.getFactionConfig().getFactionPerPlayerValue();
        this.reinvites = plugin.getFactionConfig().getFactionReinvites();
        this.members = Sets.newConcurrentHashSet();
        this.pendingInvites = Sets.newConcurrentHashSet();
        this.memberHistory = Sets.newConcurrentHashSet();
        this.timers = Sets.newConcurrentHashSet();
        this.scoreboard = null;
        this.stats = new Statistics();
        this.nextTick = (Time.now() + (plugin.getFactionConfig().getFactionTickInterval() * 1000L));

        configureScoreboard();
    }

    public PlayerFaction(Factions plugin, String name) {
        this.plugin = plugin;
        this.uniqueId = UUID.randomUUID();
        this.name = name;
        this.announcement = null;
        this.home = null;
        this.rally = null;
        this.balance = 0.0;
        this.deathsTilRaidable = plugin.getFactionConfig().getFactionPerPlayerValue();
        this.reinvites = plugin.getFactionConfig().getFactionReinvites();
        this.members = Sets.newConcurrentHashSet();
        this.pendingInvites = Sets.newConcurrentHashSet();
        this.memberHistory = Sets.newConcurrentHashSet();
        this.timers = Sets.newConcurrentHashSet();
        this.scoreboard = null;
        this.stats = new Statistics();
        this.nextTick = (Time.now() + (plugin.getFactionConfig().getFactionTickInterval() * 1000L));

        configureScoreboard();
    }

    public void tick() {
        final long next = Time.now() + ((plugin.getFactionConfig().getFactionTickInterval() * 1000L) -
                getOnlineMembers().size() * plugin.getFactionConfig().getFactionTickSubtractPerPlayer());

        setNextTick(next);

        if (getOnlineMembers().isEmpty() || isFrozen() || getDeathsTilRaidable() >= getMaxDTR()) {
            return;
        }

        setDeathsTilRaidable(getDeathsTilRaidable() + 0.1);

        if (getDeathsTilRaidable() >= getMaxDTR()) {
            sendMessage(ChatColor.GREEN + "Your faction is now at max DTR");
        }
    }

    public void sendMessage(String message) {
        getOnlineMembers().forEach(member -> Bukkit.getPlayer(member.getUniqueId()).sendMessage(message));
    }

    public void sendMessage(FactionRank minRank, String message) {
        getOnlineMembers()
                .stream()
                .filter(member -> member.getRank().getWeight() >= minRank.getWeight())
                .forEach(member -> Bukkit.getPlayer(member.getUniqueId()).sendMessage(message));
    }

    public void addMember(UUID playerId) {
        if (isMember(playerId)) {
            return;
        }

        final FactionProfile profile = new FactionProfile(playerId, FactionRank.MEMBER);

        members.add(profile);
    }

    public void addMember(UUID playerId, FactionRank rank) {
        if (isMember(playerId)) {
            return;
        }

        final FactionProfile profile = new FactionProfile(playerId, rank);

        members.add(profile);
    }

    public void removeMember(UUID playerId) {
        if (!isMember(playerId)) {
            return;
        }

        final FactionProfile profile = getMember(playerId);

        if (profile == null) {
            return;
        }

        members.remove(profile);
    }

    public double getMaxDTR() {
        final double max = plugin.getFactionConfig().getFactionPerPlayerValue() * members.size();

        if (max > plugin.getFactionConfig().getFactionMaxDTR()) {
            return plugin.getFactionConfig().getFactionMaxDTR();
        }

        return max;
    }

    public ImmutableList<FactionProfile> getOnlineMembers() {
        return ImmutableList.copyOf(members.stream().filter(member -> Bukkit.getPlayer(member.getUniqueId()) != null).collect(Collectors.toList()));
    }

    public ImmutableList<FactionProfile> getMembersByRank(FactionRank rank) {
        return ImmutableList.copyOf(members.stream().filter(member -> member.getRank().equals(rank)).collect(Collectors.toList()));
    }

    public FactionProfile getMember(UUID playerId) {
        return members.stream().filter(member -> member.getUniqueId().equals(playerId)).findFirst().orElse(null);
    }

    public FactionTimer getTimer(FactionTimer.FactionTimerType type) {
        return timers.stream().filter(timer -> timer.getType().equals(type)).findFirst().orElse(null);
    }

    public boolean isFrozen() {
        return getTimer(FactionTimer.FactionTimerType.FREEZE) != null;
    }

    public boolean isRaidable() {
        return this.deathsTilRaidable <= 0.0;
    }

    public boolean isMember(UUID uuid) {
        return getMember(uuid) != null;
    }

    public boolean isReinvited(UUID uuid) {
        return memberHistory.contains(uuid);
    }

    public boolean isInvited(UUID uuid) {
        return pendingInvites.contains(uuid);
    }

    public void updateAnnouncement(Player player, String message) {
        this.announcement = message;
        sendMessage(ChatColor.GOLD + "(Faction Announcement) " + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.YELLOW + ": " + message);
    }

    public void updateHome(Player player) {
        this.home = new PLocatable(player);

        sendMessage(ChatColor.GOLD + "Your home location has been updated to " + ChatColor.YELLOW +
                player.getLocation().getBlockX() + " " + player.getLocation().getBlockY() + " " + player.getLocation().getBlockZ() +
                ChatColor.GOLD + " by " + ChatColor.DARK_GREEN + player.getName());

        Logger.print(player.getName() + " updated " + name + "'s home location");
    }

    public void updateRally(Player player) {
        this.rally = new PLocatable(player);

        final String text = ChatColor.GOLD + "Rally" + ChatColor.YELLOW + ": " +
                player.getLocation().getBlockX() + " " + player.getLocation().getBlockY() + " " + player.getLocation().getBlockZ() + " " +
                StringUtils.capitaliseAllWords(player.getLocation().getWorld().getEnvironment().name().toLowerCase().replace("_", " "));

        final BossTimer timer = new BossTimer(plugin, text, BarColor.BLUE, BarStyle.SEGMENTED_10, BossTimer.BossTimerDuration.TEN_SECONDS);

        sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " updated your faction rally");
        getOnlineMembers().forEach(online -> timer.addPlayer(Bukkit.getPlayer(online.getUniqueId())));
    }

    public void registerFriendly(Player player) {
        if (this.scoreboard == null) {
            return;
        }

        if (this.scoreboard.getTeam("friendly").hasEntry(player.getName())) {
            return;
        }

        this.scoreboard.getTeam("friendly").addEntry(player.getName());
    }

    public void registerAlly(Player player) {
        if (this.scoreboard == null) {
            return;
        }

        if (this.scoreboard.getTeam("ally").hasEntry(player.getName())) {
            return;
        }

        this.scoreboard.getTeam("ally").addEntry(player.getName());
    }

    public void unregister() {
        if (this.scoreboard == null) {
            return;
        }

        this.scoreboard.getTeam("friendly").unregister();
        this.scoreboard.getTeam("ally").unregister();
    }

    public void unregister(Player player) {
        if (this.scoreboard == null) {
            return;
        }

        this.scoreboard.getTeam("friendly").removeEntry(player.getName());
        this.scoreboard.getTeam("ally").removeEntry(player.getName());
    }

    public void configureScoreboard() {
        if (this.scoreboard != null) {
            return;
        }

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        final Team friendly = this.scoreboard.registerNewTeam("friendly");
        final Team ally = this.scoreboard.registerNewTeam("ally");

        friendly.setPrefix(ChatColor.DARK_GREEN + "");
        friendly.setColor(ChatColor.GREEN);
        friendly.setNameTagVisibility(NameTagVisibility.ALWAYS);
        friendly.setCanSeeFriendlyInvisibles(true);

        ally.setPrefix(ChatColor.BLUE + "");
        ally.setColor(ChatColor.AQUA);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PlayerFaction fromDocument(Document document) {
        final Map<UUID, String> convertedMembers = (Map<UUID, String>)document.get("members");
        final Map<String, Long> convertedTimers = (Map<String, Long>)document.get("timers");

        this.uniqueId = (UUID)document.get("id");
        this.name = document.getString("name");
        this.announcement = document.getString("announcement");
        this.home = (document.get("home") != null) ? new PLocatable().fromDocument(document.get("home", Document.class)) : null;
        this.balance = document.getDouble("balance");
        this.deathsTilRaidable = document.getDouble("dtr");
        this.reinvites = document.getInteger("reinvites");

        convertedMembers.keySet().forEach(memberId -> {
            final FactionRank rank = FactionRank.valueOf(convertedMembers.get(memberId));
            this.members.add(new FactionProfile(memberId, rank));
        });

        convertedTimers.keySet().forEach(timerName -> {
            final FactionTimer.FactionTimerType type = FactionTimer.FactionTimerType.valueOf(timerName);
            final long expire = convertedTimers.get(timerName);
            final long remaining = expire - Time.now();
            final int remainingSeconds = (int)(remaining / 1000L);

            if (remaining > 0) {
                if (type.equals(FactionTimer.FactionTimerType.FREEZE)) {
                    final DTRFreezeTimer timer = new DTRFreezeTimer(this, remainingSeconds);
                    this.timers.add(timer);
                }
            }
        });

        this.pendingInvites = (Set<UUID>)document.get("pendingInvites");
        this.memberHistory = (Set<UUID>)document.get("memberHistory");
        this.stats.fromDocument(document.get("stats", Document.class));

        return this;
    }

    @Override
    public Document toDocument() {
        final Map<UUID, String> convertedMembers = Maps.newHashMap();
        final Map<String, Long> convertedTimers = Maps.newHashMap();

        this.members.forEach(member -> convertedMembers.put(member.getUniqueId(), member.getRank().toString()));
        this.timers.forEach(timer -> convertedTimers.put(timer.getType().toString(), timer.getExpire()));

        return new Document()
                .append("id", uniqueId)
                .append("name", name)
                .append("announcement", announcement)
                .append("home", (home != null ? home.toDocument() : null))
                .append("balance", balance)
                .append("dtr", deathsTilRaidable)
                .append("reinvites", reinvites)
                .append("members", convertedMembers)
                .append("pendingInvites", pendingInvites)
                .append("memberHistory", memberHistory)
                .append("timers", convertedTimers)
                .append("stats", stats.toDocument());
    }

    @AllArgsConstructor
    public enum FactionRank {
        MEMBER(0), OFFICER(1), CO_LEADER(2), LEADER(3);

        @Getter
        public final int weight;

        public String getDisplayName() {
            return StringUtils.capitaliseAllWords(this.name().toLowerCase().replace("_", "-"));
        }
    }

    @AllArgsConstructor
    public final class FactionProfile {
        @Getter
        public final UUID uniqueId;

        @Getter @Setter
        public FactionRank rank;
    }
}