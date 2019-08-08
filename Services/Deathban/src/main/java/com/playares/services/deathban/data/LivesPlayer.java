package com.playares.services.deathban.data;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.UUID;

public final class LivesPlayer implements MongoDocument<LivesPlayer> {
    @Getter public UUID uniqueId;
    @Getter @Setter public int standardLives;
    @Getter @Setter public int soulboundLives;

    public LivesPlayer() {
        this.uniqueId = null;
        this.standardLives = 0;
        this.soulboundLives = 0;
    }

    public LivesPlayer(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.standardLives = 0;
        this.soulboundLives = 0;
    }

    @Override
    public LivesPlayer fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.standardLives = document.getInteger("standard");
        this.soulboundLives = document.getInteger("soulbound");
        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("standard", standardLives)
                .append("soulbound", soulboundLives);
    }
}
