package com.playares.civilization.addons.prisonpearls.data;

import com.playares.commons.bukkit.location.BLocatable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.UUID;

public final class PrisonPearlLocation extends BLocatable {
    @Getter @Setter public UUID playerHolderUniqueId;
    @Getter @Setter PrisonPearlLocationType locationType;

    public PrisonPearlLocation() {
        super("world", 0, 0, 0);
        this.playerHolderUniqueId = null;
        this.locationType = PrisonPearlLocationType.UNKNOWN;
    }

    public PrisonPearlLocation(int x, int y, int z, String worldName, PrisonPearlLocationType locationType) {
        super(worldName, x, y, z);
        this.playerHolderUniqueId = null;
        this.locationType = locationType;
    }

    @Override
    public Document toDocument() {
        final Document document = super.toDocument();
        document.append("player_holder_id", playerHolderUniqueId);
        document.append("location_type", locationType.name());
        return document;
    }

    @Override
    public PrisonPearlLocation fromDocument(Document document) {
        super.fromDocument(document);
        this.playerHolderUniqueId = (UUID)document.get("player_holder_id");
        this.locationType = PrisonPearlLocationType.valueOf(document.getString("location_type"));
        return this;
    }

    @AllArgsConstructor
    public enum PrisonPearlLocationType {
        PLAYER_INVENTORY("in a player's inventory"),
        CONTAINER("in a container"),
        GROUND("on the ground"),
        UNKNOWN("in an unknown location");

        @Getter public final String description;
    }
}
