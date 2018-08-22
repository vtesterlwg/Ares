package com.playares.factions.factions;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.factions.Factions;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.ChatColor;

import java.util.UUID;

public final class ServerFaction implements Faction, MongoDocument<ServerFaction> {
    @Getter
    public final Factions plugin;

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

    @Getter @Setter
    public double buffer;

    public ServerFaction(Factions plugin) {
        this.plugin = plugin;
        this.uniqueId = UUID.randomUUID();
        this.name = null;
        this.displayName = null;
        this.location = null;
        this.flag = null;
        this.buffer = plugin.getFactionConfig().getDefaultServerClaimBuffer();
    }

    public ServerFaction(Factions plugin, String name) {
        this.plugin = plugin;
        this.uniqueId = UUID.randomUUID();
        this.name = name;
        this.displayName = name;
        this.location = null;
        this.flag = FactionFlag.SAFEZONE;
        this.buffer = plugin.getFactionConfig().getDefaultServerClaimBuffer();
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

        this.buffer = document.getDouble("buffer");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("name", name)
                .append("displayName", displayName)
                .append("location", (location != null) ? location.toDocument() : null)
                .append("flag", (flag != null) ? flag.toString() : null)
                .append("buffer", buffer);
    }

    public enum FactionFlag {
        SAFEZONE,
        EVENT
    }
}