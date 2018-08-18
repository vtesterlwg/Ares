package com.playares.factions.players;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.bukkit.util.Players;
import com.playares.factions.addons.stats.Statistics;
import com.playares.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class FactionPlayer implements MongoDocument<FactionPlayer> {
    @Getter
    public UUID uniqueId;

    @Getter @Setter
    public String username;

    @Getter @Setter
    public double balance;

    @Getter @Setter
    public PlayerFaction faction;

    @Getter
    public Statistics stats;

    public FactionPlayer() {
        this.uniqueId = null;
        this.username = null;
        this.balance = 0.0;
        this.faction = null;
        this.stats = null;
    }

    public FactionPlayer(Player player) {
        this.uniqueId = player.getUniqueId();
        this.username = player.getName();
        this.balance = 0.0; // TODO: Get from economyconfig
        this.faction = null;
        this.stats = new Statistics();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    public void sendMessage(String message) {
        if (getPlayer() != null) {
            getPlayer().sendMessage(message);
        }
    }

    public void sendTitle(String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        if (getPlayer() != null) {
            getPlayer().sendTitle(title, subtitle, fadeIn, duration, fadeOut);
        }
    }

    public void sendActionBar(String text) {
        if (getPlayer() != null) {
            Players.sendActionBar(getPlayer(), text);
        }
    }

    @Override
    public FactionPlayer fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.username = document.getString("username");
        this.faction = null;
        this.stats = new Statistics().fromDocument(document.get("stats", Document.class));

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("username", username)
                .append("stats", stats.toDocument());
    }
}