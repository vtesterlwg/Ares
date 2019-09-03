package com.playares.factions.addons.stats.container;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.base.util.Time;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.UUID;

public final class PlayerStatisticHolder implements MongoDocument<PlayerStatisticHolder> {
    @Getter public UUID owner;
    @Getter @Setter public int kills;
    @Getter @Setter public int deaths;
    @Getter @Setter public int bardTotalAffected;
    @Getter @Setter public double archerLongestShot;
    @Getter @Setter public int kothCaptures;
    @Getter @Setter public int palaceCaptures;
    @Getter @Setter public int minedCoal;
    @Getter @Setter public int minedIron;
    @Getter @Setter public int minedRedstone;
    @Getter @Setter public int minedLapis;
    @Getter @Setter public int minedGold;
    @Getter @Setter public int minedDiamond;
    @Getter @Setter public int minedEmerald;
    @Getter @Setter public int slainDragons;
    @Getter @Setter public long timePlayed;
    @Getter @Setter public long loginTime;

    public PlayerStatisticHolder(UUID owner) {
        this.owner = owner;
        this.kills = 0;
        this.deaths = 0;
        this.bardTotalAffected = 0;
        this.archerLongestShot = 0.0;
        this.kothCaptures = 0;
        this.palaceCaptures = 0;
        this.minedCoal = 0;
        this.minedIron = 0;
        this.minedRedstone = 0;
        this.minedLapis = 0;
        this.minedGold = 0;
        this.minedDiamond = 0;
        this.minedEmerald = 0;
        this.slainDragons = 0;
        this.timePlayed = 0L;
        this.loginTime = Time.now();
    }

    public void addKill() {
        setKills(kills + 1);
    }

    public void addDeath() {
        setDeaths(deaths + 1);
    }

    public void addBardTotalAffected(int amt) {
        setBardTotalAffected(bardTotalAffected + amt);
    }

    public void addKothCapture() {
        setKothCaptures(kothCaptures + 1);
    }

    public void addPalaceCapture() {
        setPalaceCaptures(palaceCaptures + 1);
    }

    public void addMinedCoal(int amt) {
        setMinedCoal(minedCoal + amt);
    }

    public void addMinedIron() {
        setMinedIron(minedIron + 1);
    }

    public void addMinedRedstone(int amt) {
        setMinedRedstone(minedRedstone + amt);
    }

    public void addMinedLapis(int amt) {
        setMinedLapis(minedLapis + amt);
    }

    public void addMinedGold() {
        setMinedGold(minedGold + 1);
    }

    public void addMinedDiamond(int amt) {
        setMinedDiamond(minedDiamond + amt);
    }

    public void addMinedEmerald(int amt) {
        setMinedEmerald(minedEmerald + amt);
    }

    public void addDragonKill() {
        setSlainDragons(slainDragons + 1);
    }

    public void addTimePlayed() {
        setTimePlayed(timePlayed + (Time.now() - loginTime));
    }

    public boolean isLongestArcherShot(double distance) {
        return distance > archerLongestShot;
    }

    @Override
    public PlayerStatisticHolder fromDocument(Document document) {
        this.kills = document.getInteger("kills");
        this.deaths = document.getInteger("deaths");
        this.bardTotalAffected = document.getInteger("bard_total_affected");
        this.archerLongestShot = document.getDouble("archer_longest_shot");
        this.kothCaptures = document.getInteger("koth_captures");
        this.palaceCaptures = document.getInteger("palace_captures");
        this.minedCoal = document.getInteger("mined_coal");
        this.minedIron = document.getInteger("mined_iron");
        this.minedRedstone = document.getInteger("mined_redstone");
        this.minedLapis = document.getInteger("mined_lapis");
        this.minedGold = document.getInteger("mined_gold");
        this.minedDiamond = document.getInteger("mined_diamond");
        this.minedEmerald = document.getInteger("mined_emerald");
        this.slainDragons = document.getInteger("slain_dragons");
        this.timePlayed = document.getLong("time_played");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", owner)
                .append("kills", kills)
                .append("deaths", deaths)
                .append("bard_total_affected", bardTotalAffected)
                .append("archer_longest_shot", archerLongestShot)
                .append("koth_captures", kothCaptures)
                .append("palace_captures", palaceCaptures)
                .append("mined_coal", minedCoal)
                .append("mined_iron", minedIron)
                .append("mined_redstone", minedRedstone)
                .append("mined_lapis", minedLapis)
                .append("mined_gold", minedGold)
                .append("mined_diamond", minedDiamond)
                .append("mined_emerald", minedEmerald)
                .append("slain_dragons", slainDragons)
                .append("time_played", timePlayed);
    }
}
