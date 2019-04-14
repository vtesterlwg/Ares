package com.playares.minez.bukkitz.data;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.ChatColor;

public final class MZServer implements MongoDocument<MZServer> {
    @Getter public int id;
    @Getter @Setter public String bungeeName;
    @Getter @Setter public int onlineAmount;
    @Getter @Setter public boolean PvE;
    @Getter @Setter public boolean premiumOnly;
    @Getter @Setter public MZServerStatus serverStatus;

    public MZServer() {
        this.id = -1;
        this.bungeeName = null;
        this.onlineAmount = 0;
        this.PvE = false;
        this.premiumOnly = false;
        this.serverStatus = MZServerStatus.OFFLINE;
    }

    public MZServer(int id, String bungeeName) {
        this.id = id;
        this.bungeeName = bungeeName;
        this.onlineAmount = 0;
        this.PvE = false;
        this.premiumOnly = false;
        this.serverStatus = MZServerStatus.OFFLINE;
    }

    public String getName() {
        return "MineZ-" + this.id;
    }

    @Override
    public MZServer fromDocument(Document document) {
        this.id = document.getInteger("id");
        this.bungeeName = document.getString("bungeeName");
        this.onlineAmount = document.getInteger("onlineAmount");
        this.PvE = document.getBoolean("pve");
        this.premiumOnly = document.getBoolean("premiumOnly");
        this.serverStatus = MZServerStatus.valueOf(document.getString("status"));
        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", this.id)
                .append("bungeeName", this.bungeeName)
                .append("onlineAmount", this.onlineAmount)
                .append("pve", this.PvE)
                .append("premiumOnly", this.premiumOnly)
                .append("status", this.serverStatus.name());
    }

    @AllArgsConstructor
    public enum MZServerStatus {
        ONLINE(ChatColor.GREEN + "Online"), WHITELISTED(ChatColor.RED + "Whitelisted"), OFFLINE(ChatColor.DARK_RED + "Offline");
        @Getter public String displayName;
    }
}