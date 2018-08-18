package com.playares.factions.factions;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.bukkit.location.PLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.ChatColor;

import java.util.UUID;

public final class ServerFaction implements Faction, MongoDocument<ServerFaction> {
    @Getter
    public UUID uniqueId;

    @Getter @Setter
    public String name;

    @Getter @Setter
    public String displayName;

    @Getter @Setter
    public PLocatable location;

    @Getter @Setter
    public FactionFlag flag;

    public ServerFaction() {
        this.uniqueId = UUID.randomUUID();
        this.name = null;
        this.displayName = null;
        this.location = null;
        this.flag = null;
    }

    public ServerFaction(String name) {
        this.uniqueId = UUID.randomUUID();
        this.name = name;
        this.displayName = name;
        this.location = null;
        this.flag = null;
    }

    @Override
    public ServerFaction fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.name = document.getString("name");

        this.displayName = (document.get("displayName") != null) ?
                ChatColor.translateAlternateColorCodes('&', document.getString("displayName")) : name;

        this.location = (document.get("location") != null) ?
                new PLocatable().fromDocument(document.get("location", Document.class)) : null;

        this.flag = (document.get("flag") != null) ?
                FactionFlag.valueOf(document.getString("flag")) : null;

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("name", name)
                .append("displayName", displayName)
                .append("location", (location != null) ? location.toDocument() : null)
                .append("flag", (flag != null) ? flag.toString() : null);
    }

    public enum FactionFlag {
        SAFEZONE,
        EVENT
    }
}