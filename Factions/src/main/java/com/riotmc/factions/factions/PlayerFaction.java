package com.riotmc.factions.factions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.riotmc.commons.base.connect.mongodb.MongoDocument;
import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.location.PLocatable;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.commons.bukkit.timer.BossTimer;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.Factions;
import com.riotmc.factions.addons.stats.holder.FactionStatisticHolder;
import com.riotmc.factions.timers.FactionTimer;
import com.riotmc.factions.timers.cont.faction.DTRFreezeTimer;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PlayerFaction implements Faction, MongoDocument<PlayerFaction> {
    /** The plugin owning this faction **/
    @Getter public final Factions plugin;
    /** Unique ID **/
    @Getter public UUID uniqueId;
    /** Faction Name **/
    @Getter @Setter public String name;
    /** Faction Announcement, shows up in '/f show' display **/
    @Getter public String announcement;
    /** Faction home location **/
    @Getter public PLocatable home;
    /** Faction rally location **/
    @Getter public PLocatable rally;
    /** Faction economy balance **/
    @Getter @Setter public double balance;
    /** Faction DTR **/
    @Getter @Setter public double deathsTilRaidable;
    /** Faction re-invites **/
    @Getter @Setter public int reinvites;
    /** Contains FactionProfile data for this factions roster **/
    @Getter public Set<FactionProfile> members;
    /** Pending invites to join this faction, contains Bukkit Player Unique IDs **/
    @Getter public Set<UUID> pendingInvites;
    /** Member history for this faction, contains Bukkit Player Unique IDs, holds all players who have joined this faction since re-invites we refreshed **/
    @Getter public Set<UUID> memberHistory;
    /** Contains all active timers **/
    @Getter public Set<FactionTimer> timers;
    /** Contains the scoreboard object for this faction, applied to all members **/
    @Getter public Scoreboard scoreboard;
    /** Statistics holder for this faction **/
    @Getter public FactionStatisticHolder stats;
    /** Next tick (in milliseconds) that this Faction should be ticked **/
    @Getter @Setter public long nextTick;

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
        this.stats = new FactionStatisticHolder();
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
        this.stats = new FactionStatisticHolder();
        this.nextTick = (Time.now() + (plugin.getFactionConfig().getFactionTickInterval() * 1000L));

        configureScoreboard();
    }

    /**
     * 'ticks' this faction, shifting up their DTR and setting the next tick time
     */
    void tick() {
        final long next = Time.now() + ((plugin.getFactionConfig().getFactionTickInterval() * 1000L) -
                getOnlineMembers().size() * plugin.getFactionConfig().getFactionTickSubtractPerPlayer());

        setNextTick(next);

        if (getOnlineMembers().isEmpty() || isFrozen() || getDeathsTilRaidable() == getMaxDTR()) {
            return;
        }

        double newDTR = getDeathsTilRaidable() + 0.01;

        if (newDTR > getMaxDTR()) {
            newDTR = getMaxDTR();
        }

        setDeathsTilRaidable(newDTR);

        if (getDeathsTilRaidable() >= getMaxDTR()) {
            new Scheduler(plugin).sync(() -> sendMessage(ChatColor.GREEN + "Your faction is now at max DTR")).run();
        }
    }

    /**
     * Sends a chat message to players in this faction
     * @param message Message
     */
    public void sendMessage(String message) {
        getOnlineMembers().forEach(member -> Bukkit.getPlayer(member.getUniqueId()).sendMessage(message));
    }

    /**
     * Sends a chat message to all players in this faction that meet the required minimum rank
     * @param minRank Minimum rank, for example setting this to OFFICER would make it so only Officer+ could see this message
     * @param message Message
     */
    public void sendMessage(FactionRank minRank, String message) {
        getOnlineMembers()
                .stream()
                .filter(member -> member.getRank().getWeight() >= minRank.getWeight())
                .forEach(member -> Bukkit.getPlayer(member.getUniqueId()).sendMessage(message));
    }

    /**
     * Adds a member to this roster
     * @param playerId Player Unique ID
     */
    public void addMember(UUID playerId) {
        if (isMember(playerId)) {
            return;
        }

        final FactionProfile profile = new FactionProfile(playerId, FactionRank.MEMBER, ChatChannel.PUBLIC);

        members.add(profile);
    }

    /**
     * Adds a member to this roster
     * @param playerId Player Unique ID
     * @param rank FactionRank
     */
    public void addMember(UUID playerId, FactionRank rank) {
        if (isMember(playerId)) {
            return;
        }

        final FactionProfile profile = new FactionProfile(playerId, rank, ChatChannel.PUBLIC);

        members.add(profile);
    }

    /**
     * Removes a member from this roster
     * @param playerId Player Unique ID
     */
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

    /**
     * Returns Max DTR
     * @return Returns the maximum DTR this faction can achieve
     */
    public double getMaxDTR() {
        final double max = plugin.getFactionConfig().getFactionPerPlayerValue() * members.size();

        if (max > plugin.getFactionConfig().getFactionMaxDTR()) {
            return plugin.getFactionConfig().getFactionMaxDTR();
        }

        return max;
    }

    /**
     * Returns online members
     * @return ImmutableList containing all online members
     */
    public ImmutableList<FactionProfile> getOnlineMembers() {
        return ImmutableList.copyOf(members.stream().filter(member -> Bukkit.getPlayer(member.getUniqueId()) != null).collect(Collectors.toList()));
    }

    /**
     * Returns members matching provided rank
     * @param rank FactionRank
     * @return ImmutableList containing all members matching provided rank
     */
    public ImmutableList<FactionProfile> getMembersByRank(FactionRank rank) {
        return ImmutableList.copyOf(members.stream().filter(member -> member.getRank().equals(rank)).collect(Collectors.toList()));
    }

    /**
     * Returns a FactionProfile matching the provided player Unique ID
     * @param playerId Player Unique ID
     * @return FactionProfile
     */
    public FactionProfile getMember(UUID playerId) {
        return members.stream().filter(member -> member.getUniqueId().equals(playerId)).findFirst().orElse(null);
    }

    /**
     * Returns an active FactionTimer matching the provided type
     * @param type Type
     * @return FactionTimer
     */
    public FactionTimer getTimer(FactionTimer.FactionTimerType type) {
        return timers.stream().filter(timer -> timer.getType().equals(type)).findFirst().orElse(null);
    }

    /**
     * Adds a new FactionTimer
     * @param timer FactionTimer
     */
    public void addTimer(FactionTimer timer) {
        final FactionTimer existing = getTimer(timer.getType());

        if (existing != null) {
            existing.setExpire(timer.getExpire());
            return;
        }

        getTimers().add(timer);
    }

    /**
     * Removes a timer matching the provided type
     * @param type Type
     */
    public void removeTimer(FactionTimer.FactionTimerType type) {
        getTimers().removeIf(timer -> timer.getType().equals(type));
    }

    /**
     * Returns true if this faction's DTR is frozen
     * @return True if frozen
     */
    public boolean isFrozen() {
        return getTimer(FactionTimer.FactionTimerType.FREEZE) != null;
    }

    /**
     * Returns true if this faction is raid-able
     * @return True if raid-able
     */
    public boolean isRaidable() {
        return this.deathsTilRaidable <= 0.0;
    }

    /**
     * Returns true if the provided unique ID is a member of this faction
     * @param uuid Player Unique ID
     * @return True if a member
     */
    public boolean isMember(UUID uuid) {
        return getMember(uuid) != null;
    }

    /**
     * Returns true if this player is being re-invited to the roster
     * @param uuid Player Unique ID
     * @return True if re-invited
     */
    public boolean isReinvited(UUID uuid) {
        return memberHistory.contains(uuid);
    }

    /**
     * Returns true if this player has an open invitation to this faction
     * @param uuid Player Unique ID
     * @return True if invited
     */
    public boolean isInvited(UUID uuid) {
        return pendingInvites.contains(uuid);
    }

    /**
     * Updates the Faction Announcement
     * @param player Announcing Player
     * @param message Message
     */
    public void updateAnnouncement(Player player, String message) {
        this.announcement = message;
        sendMessage(ChatColor.GOLD + "(Faction Announcement) " + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.YELLOW + ": " + message);
    }

    /**
     * Updates the Faction Home
     * @param player Player
     */
    public void updateHome(Player player) {
        this.home = new PLocatable(player);

        sendMessage(ChatColor.GOLD + "Your home location has been updated to " + ChatColor.YELLOW +
                player.getLocation().getBlockX() + " " + player.getLocation().getBlockY() + " " + player.getLocation().getBlockZ() +
                ChatColor.GOLD + " by " + ChatColor.DARK_GREEN + player.getName());

        Logger.print(player.getName() + " updated " + name + "'s home location");
    }

    /**
     * Updates the Faction Rally
     * @param player Player
     */
    public void updateRally(Player player) {
        this.rally = new PLocatable(player);

        final String text = ChatColor.GOLD + "Rally at " +
                ChatColor.LIGHT_PURPLE + "X: " + player.getLocation().getBlockX() + ChatColor.YELLOW + ", " +
                ChatColor.LIGHT_PURPLE + "Y: " + player.getLocation().getBlockY() + ChatColor.YELLOW + ", " +
                ChatColor.LIGHT_PURPLE + "Z: " + player.getLocation().getBlockZ() + ChatColor.YELLOW + ", " +
                ChatColor.LIGHT_PURPLE + "World: " + StringUtils.capitaliseAllWords(player.getLocation().getWorld().getEnvironment().name().toLowerCase().replace("_", " "));

        sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " updated your faction rally");

        final BossTimer timer = new BossTimer(plugin, text, BarColor.BLUE, BarStyle.SEGMENTED_10, BossTimer.BossTimerDuration.TEN_SECONDS);
        getOnlineMembers().forEach(online -> timer.addPlayer(Bukkit.getPlayer(online.getUniqueId())));
        timer.start();
    }

    /**
     * Unsets the Faction Home
     */
    public void unsetHome() {
        if (home == null) {
            return;
        }

        home = null;
        sendMessage(ChatColor.RED + "Faction home has been unset");
    }

    /**
     * Registers a player as a friendly
     *
     * Once registered:
     *  - Player can see members of this faction while they are invisible
     *  - Player will see members of this faction with green nameplates
     *
     * @param player Player
     */
    public void registerFriendly(Player player) {
        if (this.scoreboard == null) {
            return;
        }

        if (this.scoreboard.getTeam("friendly").hasEntry(player.getName())) {
            return;
        }

        this.scoreboard.getTeam("friendly").addEntry(player.getName());
    }

    /**
     * Unregisters the scoreboard for this faction
     */
    public void unregister() {
        if (this.scoreboard == null) {
            return;
        }

        this.scoreboard.getTeam("friendly").unregister();
    }

    /**
     * Unregisters a Player from this faction's scoreboard
     * @param player Player
     */
    public void unregister(Player player) {
        if (this.scoreboard == null) {
            return;
        }

        this.scoreboard.getTeam("friendly").removeEntry(player.getName());
    }

    /**
     * Configures the scoreboard for this faction
     */
    private void configureScoreboard() {
        if (this.scoreboard != null) {
            return;
        }

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        final Team friendly = this.scoreboard.registerNewTeam("friendly");

        friendly.setColor(ChatColor.DARK_GREEN);
        friendly.setNameTagVisibility(NameTagVisibility.ALWAYS);
        friendly.setCanSeeFriendlyInvisibles(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PlayerFaction fromDocument(Document document) {
        final Map<String, String> convertedMembers = (Map<String, String>)document.get("members");
        final Map<String, Long> convertedTimers = (Map<String, Long>)document.get("timers");

        this.uniqueId = (UUID)document.get("id");
        this.name = document.getString("name");
        this.announcement = document.getString("announcement");
        this.home = (document.get("home") != null) ? new PLocatable().fromDocument(document.get("home", Document.class)) : null;
        this.balance = document.getDouble("balance");
        this.deathsTilRaidable = document.getDouble("dtr");
        this.reinvites = document.getInteger("reinvites");

        convertedMembers.keySet().forEach(memberId -> {
            final UUID id = UUID.fromString(memberId);
            final FactionRank rank = FactionRank.valueOf(convertedMembers.get(memberId));

            this.members.add(new FactionProfile(id, rank, ChatChannel.PUBLIC));
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

        this.pendingInvites = Sets.newConcurrentHashSet((List<UUID>)document.get("pendingInvites"));
        this.memberHistory = Sets.newConcurrentHashSet((List<UUID>)document.get("memberHistory"));
        this.stats.fromDocument(document.get("stats", Document.class));

        return this;
    }

    @Override
    public Document toDocument() {
        final Map<String, String> convertedMembers = Maps.newHashMap();
        final Map<String, Long> convertedTimers = Maps.newHashMap();

        this.members.forEach(member -> convertedMembers.put(member.getUniqueId().toString(), member.getRank().toString()));
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

        /** Determines the value of this rank **/
        @Getter public final int weight;

        /**
         * Returns the display name for this rank
         * @return Display Name
         */
        public String getDisplayName() {
            return StringUtils.capitaliseAllWords(this.name().toLowerCase().replace("_", "-"));
        }

        /**
         * Returns true if this rank is higher than the provided rank
         * @param other Rank
         * @return True if higher
         */
        public boolean isHigher(FactionRank other) {
            return this.getWeight() > other.getWeight();
        }

        /**
         * Returns true if this rank is higher or equal to the provided rank
         * @param other Rank
         * @return True if higher or equal
         */
        public boolean isHigherOrEqual(FactionRank other) {
            return this.getWeight() >= other.getWeight();
        }

        /**
         * Returns the next rank in order of this rank
         * @return Next Rank
         */
        public FactionRank getNext() {
            switch (this) {
                case MEMBER: return OFFICER;
                case OFFICER: return CO_LEADER;
                case CO_LEADER: return LEADER;
                case LEADER: return null;
                default: return null;
            }
        }

        /**
         * Returns the previous rank in order of this rank
         * @return Previous Rank
         */
        public FactionRank getLower() {
            switch (this) {
                case MEMBER: return null;
                case OFFICER: return MEMBER;
                case CO_LEADER: return OFFICER;
                case LEADER: return CO_LEADER;
                default: return null;
            }
        }
    }

    /**
     * Determins Chat-Channels
     */
    public enum ChatChannel {
        PUBLIC, FACTION, OFFICER
    }

    /**
     * Stores all information for a member on this roster
     */
    @AllArgsConstructor
    public final class FactionProfile {
        /** Player Unique ID **/
        @Getter public final UUID uniqueId;
        /** Faction Rank **/
        @Getter @Setter public FactionRank rank;
        /** Current Chat Channel **/
        @Getter @Setter public ChatChannel channel;
    }
}