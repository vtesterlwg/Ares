package com.playares.factions;

import com.playares.services.serversync.data.Server;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public final class FactionConfig {
    @Getter public Factions plugin;

    FactionConfig(Factions plugin) {
        this.plugin = plugin;
    }

    /** MongoDB Database Connect URI **/
    @Getter public String databaseURI;

    /** ServerSync ID **/
    @Getter public int syncId;
    /** ServerSync Bungeecord Name **/
    @Getter public String syncBungeeName;
    /** ServerSync Display Name **/
    @Getter public String syncDisplayName;
    /** ServerSync Description **/
    @Getter public String syncDescription;
    /** ServerSync Server Type **/
    @Getter public Server.Type syncType;
    /** ServerSync Premium Allocated Slots **/
    @Getter public int syncPremiumAllocatedSlots;

    /** Faction Member Cap **/
    @Getter public int factionMemberCap;
    /** Faction Alliance Cap **/
    @Getter public int factionAllyCap;
    /** Faction Home Max Y Height **/
    @Getter public double factionHomeCap;
    /** Faction Re-invites **/
    @Getter public int factionReinvites;
    /** Max Faction DTR **/
    @Getter public double factionMaxDTR;
    /** Interval (in seconds) for a PlayerFaction to 'tick' **/
    @Getter public int factionTickInterval;
    /** Time subtracted (per player) off of each tick interval **/
    @Getter public int factionTickSubtractPerPlayer;
    /** DTR value per player **/
    @Getter public double factionPerPlayerValue;
    /** Minimum faction name length **/
    @Getter public int minFactionNameLength;
    /** Maximum faction name length **/
    @Getter public int maxFactionNameLength;
    /** Banned faction names **/
    @Getter public List<String> bannedFactionNames;
    /** Minimum PlayerFaction claim size **/
    @Getter public int claimMinSize;
    /** Maximum amount of claims a PlayerFaction can have at one time **/
    @Getter public int maxClaims;
    /** Value of each block in the area of a claim **/
    @Getter public double claimBlockValue;
    /** Percent refunded upon unclaiming land **/
    @Getter public double refundedPercent;
    /** Distance (in blocks) that each unique faction claim must be from others **/
    @Getter public double playerClaimBuffer;
    /** Default distance (in blocks) that each unique faction claim must be from ServerFaction claims **/
    @Getter public double defaultServerClaimBuffer;
    /** Time (in seconds) that attacking players should be combat-tagged for **/
    @Getter public int timerCombatTagAttacker;
    /** Time (in seconds) that attacked players should be combat-tagged for **/
    @Getter public int timerCombatTagAttacked;
    /** Time (in seconds) that players should receive for PvP Protection upon logging in or respawning **/
    @Getter public int timerProtection;
    /** Time (in seconds) that player enderpearls should be locked upon using one **/
    @Getter public int timerEnderpearl;
    /** Time (in seconds) that player totems of the undying should be locked upon using one **/
    @Getter public int timerTotem;
    /** Time (in seconds) that player gapples should be locked upon using one **/
    @Getter public int timerGapple;
    /** Time (in seconds) that player crapples should be locked upon using one **/
    @Getter public int timerCrapple;
    /** Time (in seconds) that players should have to wait before warping home **/
    @Getter public int timerHome;
    /** Time (in seconds) that players should have to wait before being teleported outside of enemy claims when stuck **/
    @Getter public int timerStuck;
    /** Time (in seconds) that factions should have to wait upon a player dying **/
    @Getter public int timerFreeze;
    /** Time (in seconds) that factions should have to wait upon setting a new rally **/
    @Getter public int timerRally;

    public void loadValues() {
        final YamlConfiguration config = plugin.getConfig("config");

        this.databaseURI = config.getString("database");

        this.syncId = config.getInt("server-data.id");
        this.syncBungeeName = config.getString("server-data.bungee-name");
        this.syncDisplayName = ChatColor.translateAlternateColorCodes('&', config.getString("server-data.display-name"));
        this.syncDescription = ChatColor.translateAlternateColorCodes('&', config.getString("server-data.description"));
        this.syncType = Server.Type.valueOf(config.getString("server-data.type"));
        this.syncPremiumAllocatedSlots = config.getInt("server-data.premium-allocated-slots");

        this.factionMemberCap = config.getInt("factions.member-cap");
        this.factionAllyCap = config.getInt("factions.ally-cap");
        this.factionHomeCap = config.getDouble("factions.home-height-cap");
        this.factionReinvites = config.getInt("factions.reinvites");
        this.factionMaxDTR = config.getDouble("factions.max-dtr");
        this.factionTickInterval = config.getInt("factions.tick-interval");
        this.factionTickSubtractPerPlayer = config.getInt("factions.tick-subtract-per-player");
        this.factionPerPlayerValue = config.getDouble("factions.per-player-dtr-value");
        this.minFactionNameLength = config.getInt("factions.naming.min");
        this.maxFactionNameLength = config.getInt("factions.naming.max");
        this.bannedFactionNames = config.getStringList("factions.naming.blocked-names");
        this.claimMinSize = config.getInt("claims.min-size");
        this.maxClaims = config.getInt("claims.max-claims");
        this.claimBlockValue = config.getDouble("claims.block-value");
        this.refundedPercent = config.getDouble("claims.refunded-percent");
        this.playerClaimBuffer = config.getDouble("claims.buffers.player");
        this.defaultServerClaimBuffer = config.getDouble("claims.buffers.server");
        this.timerCombatTagAttacked = config.getInt("timers.player.combat-tag.attacked");
        this.timerCombatTagAttacker = config.getInt("timers.player.combat-tag.attacker");
        this.timerProtection = config.getInt("timers.player.protection");
        this.timerEnderpearl = config.getInt("timers.player.enderpearl");
        this.timerTotem = config.getInt("timers.player.totem");
        this.timerGapple = config.getInt("timers.player.gapple");
        this.timerCrapple = config.getInt("timers.player.crapple");
        this.timerHome = config.getInt("timers.player.home");
        this.timerStuck = config.getInt("timers.player.stuck");
        this.timerFreeze = config.getInt("timers.faction.freeze");
        this.timerRally = config.getInt("timers.faction.rally");
    }
}