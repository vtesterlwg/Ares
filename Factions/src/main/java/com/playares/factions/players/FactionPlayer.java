package com.playares.factions.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.bukkit.util.Players;
import com.playares.factions.addons.stats.holder.PlayerStatisticHolder;
import com.playares.factions.claims.DefinedClaim;
import com.playares.factions.claims.pillars.ClaimPillar;
import com.playares.factions.claims.pillars.MapPillar;
import com.playares.factions.claims.pillars.Pillar;
import com.playares.factions.factions.PlayerFaction;
import com.playares.factions.timers.PlayerTimer;
import com.playares.factions.timers.cont.player.*;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class FactionPlayer implements MongoDocument<FactionPlayer> {
    /** Player Unique ID **/
    @Getter public UUID uniqueId;
    /** Player Username **/
    @Getter @Setter public String username;
    /** Economy Balance **/
    @Getter @Setter public double balance;
    /** If true the player should not spawn a Combat Logger **/
    @Getter @Setter public boolean safelogging;
    /** Faction **/
    @Getter @Setter public PlayerFaction faction;
    /** Current claim inside of **/
    @Getter @Setter public DefinedClaim currentClaim;
    /** Contains all active timers **/
    @Getter public Set<PlayerTimer> timers;
    /** Contains all active pillars **/
    @Getter public Set<Pillar> pillars;
    /** Contains all Player Statistics **/
    @Getter public PlayerStatisticHolder stats;

    public FactionPlayer() {
        this.uniqueId = null;
        this.username = null;
        this.balance = 0.0;
        this.safelogging = false;
        this.faction = null;
        this.currentClaim = null;
        this.timers = Sets.newConcurrentHashSet();
        this.pillars = Sets.newHashSet();
        this.stats = null;
    }

    public FactionPlayer(Player player) {
        this.uniqueId = player.getUniqueId();
        this.username = player.getName();
        this.balance = 0.0; // TODO: Get from economyconfig
        this.safelogging = false;
        this.faction = null;
        this.currentClaim = null;
        this.timers = Sets.newConcurrentHashSet();
        this.pillars = Sets.newHashSet();
        this.stats = new PlayerStatisticHolder();
    }

    public FactionPlayer(UUID uniqueId, String username) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.balance = 0.0;
        this.safelogging = false;
        this.faction = null;
        this.currentClaim = null;
        this.timers = Sets.newConcurrentHashSet();
        this.pillars = Sets.newHashSet();
        this.stats = new PlayerStatisticHolder();
    }

    /**
     * Returns a Player Timer matching the provided type
     * @param type Type
     * @return PlayerTimer
     */
    public PlayerTimer getTimer(PlayerTimer.PlayerTimerType type) {
        return getTimers()
                .stream()
                .filter(timer -> timer.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns true if the provided type is active on this profile
     * @param type Type
     * @return True if exists
     */
    public boolean hasTimer(PlayerTimer.PlayerTimerType type) {
        return getTimers()
                .stream()
                .anyMatch(timer -> timer.getType().equals(type));
    }

    /**
     * Adds a new PlayerTimer to this profile
     * @param timer PlayerTimer
     */
    public void addTimer(PlayerTimer timer) {
        final PlayerTimer existing = getTimer(timer.getType());

        if (existing != null) {
            existing.setExpire(timer.getExpire());
            return;
        }

        getTimers().add(timer);
    }

    /**
     * Returns the Bukkit object this profile represents
     * @return Player
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    /**
     * Sends a chat message to this player
     * @param message Message
     */
    public void sendMessage(String message) {
        if (getPlayer() != null) {
            getPlayer().sendMessage(message);
        }
    }

    /**
     * Sends a title message to this player
     * @param title Title
     * @param subtitle Subtitle
     * @param fadeIn Fade in ticks
     * @param duration Duration ticks
     * @param fadeOut Fade out ticks
     */
    public void sendTitle(String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        if (getPlayer() != null) {
            getPlayer().sendTitle(title, subtitle, fadeIn, duration, fadeOut);
        }
    }

    /**
     * Sends an action bar message to this player
     * @param text Message
     */
    public void sendActionBar(String text) {
        if (getPlayer() != null) {
            Players.sendActionBar(getPlayer(), text);
        }
    }

    /**
     * Returns a ClaimPillar matching the provided type
     * @param type Type
     * @return ClaimPillar
     */
    public ClaimPillar getExistingClaimPillar(ClaimPillar.ClaimPillarType type) {
        return (ClaimPillar)pillars
                .stream()
                .filter(pillar -> pillar instanceof ClaimPillar)
                .filter(pillar -> ((ClaimPillar) pillar).getType().equals(type))
                .findFirst()
                .orElse(null);
    }

    /**
     * Hides all pillars for this player
     */
    public void hideAllPillars() {
        pillars.forEach(Pillar::hide);
        pillars.clear();
    }

    /**
     * Returns true if this player has ClaimPillars
     * @return True if ClaimPillars found
     */
    public boolean hasClaimPillars() {
        return !pillars
                .stream()
                .filter(pillar -> pillar instanceof ClaimPillar)
                .collect(Collectors.toList())
                .isEmpty();
    }

    /**
     * Returns true if this player has MapPillars
     * @return True if MapPillars found
     */
    public boolean hasMapPillars() {
        return !pillars
                .stream()
                .filter(pillar -> pillar instanceof MapPillar)
                .collect(Collectors.toList())
                .isEmpty();
    }

    /**
     * Hides all claim pillars for this player
     */
    public void hideAllClaimPillars() {
        final List<Pillar> toRemove = Lists.newArrayList();

        pillars
                .stream()
                .filter(pillar -> pillar instanceof ClaimPillar)
                .forEach(claimPillar -> {

            claimPillar.hide();
            toRemove.add(claimPillar);

        });

        pillars.removeAll(toRemove);
    }

    /**
     * Hides all map pillars for this player
     */
    public void hideAllMapPillars() {
        final List<Pillar> toRemove = Lists.newArrayList();

        pillars
                .stream()
                .filter(pillar -> pillar instanceof MapPillar)
                .forEach(mapPillar -> {

            mapPillar.hide();
            toRemove.add(mapPillar);

        });

        pillars.removeAll(toRemove);
    }

    @SuppressWarnings("unchecked")
    @Override
    public FactionPlayer fromDocument(Document document) {
        final Map<String, Long> convertedTimers = (Map<String, Long>)document.get("timers");

        this.uniqueId = (UUID)document.get("id");
        this.username = document.getString("username");
        this.balance = document.getDouble("balance");
        this.faction = null;
        this.stats = new PlayerStatisticHolder().fromDocument(document.get("stats", Document.class));

        // Load timers
        convertedTimers.keySet().forEach(timerName -> {
            final PlayerTimer.PlayerTimerType type = PlayerTimer.PlayerTimerType.valueOf(timerName);
            final long remaining = convertedTimers.get(timerName);
            final int remainingSeconds = (int)(remaining / 1000L);

            if (remaining > 0) {
                // Kinda sloppy but each timer type needs to be added here
                if (type.equals(PlayerTimer.PlayerTimerType.ENDERPEARL)) {
                    final EnderpearlTimer timer = new EnderpearlTimer(uniqueId, remainingSeconds);
                    this.timers.add(timer);
                }

                else if (type.equals(PlayerTimer.PlayerTimerType.COMBAT)) {
                    final CombatTagTimer timer = new CombatTagTimer(uniqueId, remainingSeconds);
                    this.timers.add(timer);
                }

                else if (type.equals(PlayerTimer.PlayerTimerType.PROTECTION)) {
                    final ProtectionTimer timer = new ProtectionTimer(uniqueId, remainingSeconds);
                    this.timers.add(timer);
                }

                else if (type.equals(PlayerTimer.PlayerTimerType.TOTEM)) {
                    final TotemTimer timer = new TotemTimer(uniqueId, remainingSeconds);
                    this.timers.add(timer);
                }

                else if (type.equals(PlayerTimer.PlayerTimerType.GAPPLE)) {
                    final GappleTimer timer = new GappleTimer(uniqueId, remainingSeconds);
                    this.timers.add(timer);
                }
            }
        });

        return this;
    }

    @Override
    public Document toDocument() {
        final Map<String, Long> convertedTimers = Maps.newHashMap();

        this.timers.forEach(timer -> convertedTimers.put(timer.getType().toString(), timer.getRemaining()));

        return new Document()
                .append("id", uniqueId)
                .append("username", username)
                .append("balance", balance)
                .append("timers", convertedTimers)
                .append("stats", stats.toDocument());
    }
}