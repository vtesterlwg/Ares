package com.riotmc.factions.addons.stats.holder;

import com.riotmc.commons.base.connect.mongodb.MongoDocument;
import lombok.Getter;
import org.bson.Document;

public final class FactionStatisticHolder implements StatisticHolder, MongoDocument<FactionStatisticHolder> {
    @Getter
    public int kills, deaths, minorEventCaptures, majorEventCaptures;

    public FactionStatisticHolder() {
        this.kills = 0;
        this.deaths = 0;
        this.minorEventCaptures = 0;
        this.majorEventCaptures = 0;
    }

    @Override
    public void addKill() {
        this.kills += 1;
    }

    @Override
    public void addDeath() {
        this.deaths += 1;
    }

    @Override
    public void addMinorEventCapture() {
        this.minorEventCaptures += 1;
    }

    @Override
    public void addMajorEventCapture() {
        this.majorEventCaptures += 1;
    }

    @Override
    public FactionStatisticHolder fromDocument(Document document) {
        this.kills = document.getInteger("kills");
        this.deaths = document.getInteger("deaths");
        this.minorEventCaptures = document.getInteger("minorEvents");
        this.majorEventCaptures = document.getInteger("majorEvents");
        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("kills", kills)
                .append("deaths", deaths)
                .append("minorEvents", minorEventCaptures)
                .append("majorEvents", majorEventCaptures);
    }
}