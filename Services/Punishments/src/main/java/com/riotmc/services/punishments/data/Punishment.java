package com.riotmc.services.punishments.data;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.base.util.Time;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.UUID;

public final class Punishment implements MongoDocument<Punishment> {
    @Getter
    public UUID uniqueId;

    @Getter
    public PunishmentType type;

    @Getter
    public UUID punishedId;

    @Getter
    public UUID creatorId;

    @Getter
    public int punishedIp;

    @Getter
    public String reason;

    @Getter
    public long createDate;

    @Getter
    public long expireDate;

    @Getter @Setter
    public boolean appealed;

    public Punishment() {
        this.uniqueId = null;
        this.type = PunishmentType.UNSPECIFIED;
        this.punishedId = null;
        this.creatorId = null;
        this.punishedIp = 0;
        this.reason = "Not given";
        this.createDate = 0L;
        this.expireDate = 0L;
        this.appealed = false;
    }

    public Punishment(PunishmentType type, UUID punishedId, UUID creatorId, int punishedIp, String reason, long expireDate) {
        this.uniqueId = UUID.randomUUID();
        this.type = type;
        this.punishedId = punishedId;
        this.creatorId = creatorId;
        this.punishedIp = punishedIp;
        this.reason = reason;
        this.createDate = Time.now();
        this.expireDate = expireDate;
        this.appealed = false;
    }

    public boolean isForever() {
        return !isAppealed() && getExpireDate() == 0L;
    }

    public boolean isActive() {
        return isForever() || (!isAppealed() && getExpireDate() > Time.now());
    }

    @Override
    public Punishment fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.type = PunishmentType.valueOf(document.getString("type"));
        this.punishedId = (UUID)document.get("punished");
        this.creatorId = (UUID)document.get("creator");
        this.punishedIp = document.getInteger("address");
        this.reason = document.getString("reason");
        this.createDate = document.getLong("created");
        this.expireDate = document.getLong("expire");
        this.appealed = document.getBoolean("appealed");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("type", type.toString())
                .append("punished", punishedId)
                .append("creator", creatorId)
                .append("address", punishedIp)
                .append("reason", reason)
                .append("created", createDate)
                .append("expire", expireDate)
                .append("appealed", appealed);
    }
}