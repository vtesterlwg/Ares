package com.playares.services.serversync.data;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.bukkit.AresPlugin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class Server implements MongoDocument<Server> {
    @Getter public final AresPlugin plugin;
    @Getter @Setter public int id;
    @Getter @Setter public String bungeeName;
    @Getter @Setter public String displayName;
    @Getter @Setter public String description;
    @Getter @Setter public Type type;
    @Getter @Setter public Status status;
    @Getter @Setter public int onlineCount;
    @Getter @Setter public int maxPlayers;
    @Getter @Setter public int premiumAllocatedSlots;

    public Server(AresPlugin plugin) {
        this.plugin = plugin;
        this.id = 0;
        this.bungeeName = null;
        this.displayName = null;
        this.description = null;
        this.type = Type.LOBBY;
        this.status = Status.OFFLINE;
        this.onlineCount = 0;
        this.maxPlayers = 500;
        this.premiumAllocatedSlots = 350;
    }

    public Server(AresPlugin plugin, int id, String bungeeName, String displayName, String description, Type type, int premiumAllocatedSlots) {
        this.plugin = plugin;
        this.id = id;
        this.bungeeName = bungeeName;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.status = Status.ONLINE;
        this.onlineCount = 0;
        this.premiumAllocatedSlots = premiumAllocatedSlots;
        this.maxPlayers = 500;
    }

    public boolean isPremiumRequired() {
        if (premiumAllocatedSlots == 0) {
            return false;
        }

        return onlineCount >= premiumAllocatedSlots;
    }

    @SuppressWarnings("UnstableApiUsage")
    public void send(Player player) {
        player.sendMessage(ChatColor.RESET + "Now sending you to " + getDisplayName() + ChatColor.RESET + "...");

        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Connect");
        output.writeUTF(getBungeeName());
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray());
    }

    @Override
    public Server fromDocument(Document document) {
        this.id = document.getInteger("id");

        if (document.containsKey("bungee_name")) {
            this.bungeeName = document.getString("bungee_name");
        }

        if (document.containsKey("display_name") && document.get("display_name") != null) {
            this.displayName = ChatColor.translateAlternateColorCodes('&', document.getString("display_name"));
        }

        if (document.containsKey("type") && document.get("description") != null) {
            this.description = ChatColor.translateAlternateColorCodes('&', document.getString("description"));
        }

        if (document.containsKey("type")) {
            this.type = Type.valueOf(document.getString("type"));
        }

        if (document.containsKey("status")) {
            this.status = Status.valueOf(document.getString("status"));
        }

        if (document.containsKey("online_count")) {
            this.onlineCount = document.getInteger("online_count");
        }

        if (document.containsKey("premium_allocated_slots")) {
            this.premiumAllocatedSlots = document.getInteger("premium_allocated_slots");
        }

        if (document.containsKey("max_players")) {
            this.maxPlayers = document.getInteger("max_players");
        }

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
                .append("online_count", onlineCount)
                .append("premium_allocated_slots", premiumAllocatedSlots)
                .append("max_players", maxPlayers);
    }

    @AllArgsConstructor
    public enum Type {
        FACTION("Factions"),
        CIV("Civilizations"),
        ARENA("Arena"),
        DEV("Development"),
        HC("Hungercraft"),
        MZ("MineZ"),
        BUNKERS("Bunkers"),
        LOBBY("Lobby");
        @Getter public final String displayName;
    }

    @AllArgsConstructor
    public enum Status {
        ONLINE(ChatColor.GREEN + "Online"), WHITELISTED(ChatColor.GRAY + "Whitelisted"), OFFLINE(ChatColor.RED + "Offline");
        @Getter public final String displayName;
    }
}