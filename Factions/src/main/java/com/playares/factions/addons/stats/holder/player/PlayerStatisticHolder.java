package com.playares.factions.addons.stats.holder.player;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.factions.addons.stats.holder.StatisticHolder;
import lombok.Getter;
import org.bson.Document;

import java.util.UUID;

public final class PlayerStatisticHolder implements StatisticHolder, MongoDocument<PlayerStatisticHolder> {
    @Getter public UUID owner;

    public PlayerStatisticHolder(UUID owner) {
        this.owner = owner;
    }

    @Override
    public PlayerStatisticHolder fromDocument(Document document) {
        return null;
    }

    @Override
    public Document toDocument() {
        return null;
    }
}
