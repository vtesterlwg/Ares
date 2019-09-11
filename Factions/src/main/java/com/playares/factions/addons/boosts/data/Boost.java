package com.playares.factions.addons.boosts.data;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Material;

import java.util.UUID;

public final class Boost implements MongoDocument<Boost> {
    @Getter public UUID uniqueId;
    @Getter public UUID ownerId;
    @Getter public BoostType type;
    @Getter public int duration;

    public Boost() {
        this.uniqueId = UUID.randomUUID();
        this.ownerId = null;
        this.type = null;
        this.duration = 1800;
    }

    public Boost(UUID ownerId, BoostType type, int duration) {
        this.uniqueId = UUID.randomUUID();
        this.ownerId = ownerId;
        this.type = type;
        this.duration = duration;
    }

    @Override
    public Boost fromDocument(Document document) {
        uniqueId = (UUID)document.get("id");
        ownerId = (UUID)document.get("owner");
        type = BoostType.valueOf(document.getString("type"));
        duration = document.getInteger("duration");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("owner", ownerId)
                .append("type", type.name())
                .append("duration", duration);
    }

    @AllArgsConstructor
    public enum BoostType {
        ORES("Double Ore Spawnrates", "Increase the spawnrates for Gold, Diamond and Emerald ores", Material.DIAMOND_ORE),
        DROPS("Double Mob Loot Drops", "Increase the spawnrates for mob loot drops", Material.ENDER_PEARL),
        EXP("Double EXP Drops", "Increase the amount of dropped experience orbs", Material.EXP_BOTTLE);

        @Getter public final String displayName;
        @Getter public final String description;
        @Getter public final Material icon;
    }
}