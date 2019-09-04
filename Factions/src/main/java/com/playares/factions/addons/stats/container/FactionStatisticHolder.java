package com.playares.factions.addons.stats.container;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.UUID;

public final class FactionStatisticHolder implements MongoDocument<FactionStatisticHolder> {
    @Getter public UUID owner;
    @Getter @Setter public int kills;
    @Getter @Setter public int deaths;
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

    public FactionStatisticHolder(UUID owner) {
        this.owner = owner;
        this.kills = 0;
        this.deaths = 0;
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
    }

    public void addKill() {
        setKills(kills + 1);
    }

    public void addDeath() {
        setDeaths(deaths + 1);
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

    @Override
    public FactionStatisticHolder fromDocument(Document document) {
        this.kills = document.getInteger("kills", 0);
        this.deaths = document.getInteger("deaths", 0);
        this.kothCaptures = document.getInteger("koth_captures", 0);
        this.palaceCaptures = document.getInteger("palace_captures", 0);
        this.minedCoal = document.getInteger("mined_coal", 0);
        this.minedIron = document.getInteger("mined_iron", 0);
        this.minedRedstone = document.getInteger("mined_redstone", 0);
        this.minedLapis = document.getInteger("mined_lapis", 0);
        this.minedGold = document.getInteger("mined_gold", 0);
        this.minedDiamond = document.getInteger("mined_diamond", 0);
        this.minedEmerald = document.getInteger("mined_emerald", 0);
        this.slainDragons = document.getInteger("slain_dragons", 0);

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", owner)
                .append("kills", kills)
                .append("deaths", deaths)
                .append("koth_captures", kothCaptures)
                .append("palace_captures", palaceCaptures)
                .append("mined_coal", minedCoal)
                .append("mined_iron", minedIron)
                .append("mined_redstone", minedRedstone)
                .append("mined_lapis", minedLapis)
                .append("mined_gold", minedGold)
                .append("mined_diamond", minedDiamond)
                .append("mined_emerald", minedEmerald)
                .append("slain_dragons", slainDragons);
    }
}
