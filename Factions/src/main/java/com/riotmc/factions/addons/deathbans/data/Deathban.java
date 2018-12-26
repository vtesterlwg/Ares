package com.riotmc.factions.addons.deathbans.data;

import com.riotmc.commons.base.connect.mongodb.MongoDocument;
import com.riotmc.commons.base.util.Time;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.UUID;

public final class Deathban implements MongoDocument<Deathban> {
    @Getter public UUID ownerId;
    @Getter @Setter public long unbanTime;
    @Getter public boolean permanent;

    public Deathban() {
        this.ownerId = null;
        this.unbanTime = 0L;
        this.permanent = false;
    }

    public Deathban(UUID ownerId, long unbanTime, boolean permanent) {
        this.ownerId = ownerId;
        this.unbanTime = unbanTime;
        this.permanent = permanent;
    }

    public long getTimeUntilUndeathban() {
        return (permanent ? -1L : (unbanTime - Time.now()));
    }

    public boolean isExpired() {
        return !permanent && unbanTime <= Time.now();
    }

    @Override
    public Deathban fromDocument(Document document) {
        this.ownerId = (UUID)document.get("id");
        this.unbanTime = document.getLong("unban");
        this.permanent = document.getBoolean("permanent");
        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", ownerId)
                .append("unban", unbanTime)
                .append("permanent", permanent);
    }
}