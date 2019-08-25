package com.playares.services.serversync.data;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.ChatColor;

public final class Server implements MongoDocument<Server> {
    @Getter @Setter public int id;
    @Getter @Setter public String bungeeName;
    @Getter @Setter public String displayName;
    @Getter @Setter public String description;
    @Getter @Setter public Type type;
    @Getter @Setter public Status status;
    @Getter @Setter public int onlineCount;

    public Server() {
        this.id = 0;
        this.bungeeName = null;
        this.displayName = null;
        this.description = null;
        this.type = Type.LOBBY;
        this.status = Status.OFFLINE;
        this.onlineCount = 0;
    }

    public Server(int id, String bungeeName, String displayName, String description, Type type) {
        this.id = id;
        this.bungeeName = bungeeName;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.status = Status.ONLINE;
        this.onlineCount = 0;
    }

    @Override
    public Server fromDocument(Document document) {
        this.id = document.getInteger("id");
        this.bungeeName = document.getString("bungee_name");
        this.displayName = ChatColor.translateAlternateColorCodes('&', document.getString("display_name"));
        this.description = ChatColor.translateAlternateColorCodes('&', document.getString("description"));
        this.type = Type.valueOf(document.getString("type"));
        this.status = Status.valueOf(document.getString("status"));
        this.onlineCount = document.getInteger("online_count");
        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", id)
                .append("bungee_name", bungeeName)
                .append("display_name", displayName)
                .append("description", description)
                .append("type", type.name())
                .append("status", status.name())
                .append("online_count", onlineCount);
    }

    @AllArgsConstructor
    public enum Type {
        FACTION("Factions"), ARENA("Arena"), DEV("Development"), HG("Hungecraft"), MZ("MineZ"), BUNKERS("Bunkers"), LOBBY("Lobby");
        @Getter public final String displayName;
    }

    @AllArgsConstructor
    public enum Status {
        ONLINE(ChatColor.GREEN + "Online"), WHITELISTED(ChatColor.GRAY + "Whitelisted"), OFFLINE(ChatColor.RED + "Offline");
        @Getter public final String displayName;
    }
}