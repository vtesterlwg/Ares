package com.riotmc.services.deathban.data;

import com.riotmc.commons.base.connect.mongodb.MongoDocument;
import com.riotmc.commons.base.util.Time;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.UUID;

public final class Deathban implements MongoDocument<Deathban> {
    @Getter public UUID ownerId;
    @Getter public long createdTime;
    @Getter @Setter public long unbanTime;
    @Getter public boolean permanent;

    public Deathban() {
        this.ownerId = null;
        this.createdTime = Time.now();
        this.unbanTime = 0L;
        this.permanent = false;
    }

    public Deathban(UUID ownerId, long createdTime, long unbanTime, boolean permanent) {
        this.ownerId = ownerId;
        this.createdTime = createdTime;
        this.unbanTime = unbanTime;
        this.permanent = permanent;
    }

    public long getTimeSinceCreated() {
        return Time.now() - createdTime;
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
        this.createdTime = document.getLong("created");
        this.unbanTime = document.getLong("unban");
        this.permanent = document.getBoolean("permanent");
        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", ownerId)
                .append("created", createdTime)
                .append("unban", unbanTime)
                .append("permanent", permanent);
    }
}