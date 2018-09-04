package com.playares.factions;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public final class FactionConfig {
    @Getter
    public Factions plugin;

    FactionConfig(Factions plugin) {
        this.plugin = plugin;
    }

    @Getter
    public String databaseURI;

    @Getter
    public int factionMemberCap;

    @Getter
    public int factionAllyCap;

    @Getter
    public double factionHomeCap;

    @Getter
    public int factionReinvites;

    @Getter
    public double factionMaxDTR;

    @Getter
    public int factionTickInterval;

    @Getter
    public int factionTickSubtractPerPlayer;

    @Getter
    public double factionPerPlayerValue;

    @Getter
    public int minFactionNameLength;

    @Getter
    public int maxFactionNameLength;

    @Getter
    public List<String> bannedFactionNames;

    @Getter
    public int claimMinSize;

    @Getter
    public int maxClaims;

    @Getter
    public double claimBlockValue;

    @Getter
    public double playerClaimBuffer;

    @Getter
    public double defaultServerClaimBuffer;

    @Getter
    public int timerCombatTagAttacker;

    @Getter
    public int timerCombatTagAttacked;

    @Getter
    public int timerProtection;

    @Getter
    public int timerEnderpearl;

    @Getter
    public int timerTotem;

    @Getter
    public int timerGapple;

    @Getter
    public int timerCrapple;

    @Getter
    public int timerHome;

    @Getter
    public int timerStuck;

    @Getter
    public int timerFreeze;

    @Getter
    public int timerRally;

    public void loadValues() {
        final YamlConfiguration config = plugin.getConfig("config");

        this.databaseURI = config.getString("database");
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
