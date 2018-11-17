package com.playares.services.ranks.data;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.ChatColor;

public final class RiotRank implements MongoDocument<RiotRank> {
    @Getter @Setter
    public String name;

    @Getter @Setter
    public String displayName;

    @Getter @Setter
    public String prefix;

    @Getter @Setter
    public String permission;

    @Getter @Setter
    public int weight;

    @Getter @Setter
    public boolean staff;

    @Getter @Setter
    public boolean everyone;

    public RiotRank() {
        this.name = null;
        this.displayName = null;
        this.prefix = null;
        this.permission = null;
        this.weight = 0;
        this.staff = false;
        this.everyone = false;
    }

    public RiotRank(String name) {
        this.name = name;
        this.displayName = null;
        this.prefix = null;
        this.permission = null;
        this.weight = 0;
        this.staff = false;
        this.everyone = false;
    }

    public boolean isSetup() {
        return this.name != null && this.displayName != null && (this.staff || this.everyone);
    }

    @Override
    public String toString() {
        return displayName;
    }

    public RiotRank fromDocument(Document document) {
        this.name = document.getString("name");
        this.displayName = ChatColor.translateAlternateColorCodes('&', document.getString("display_name"));
        this.prefix = ChatColor.translateAlternateColorCodes('&', document.getString("prefix"));
        this.permission = document.getString("permission");
        this.weight = document.getInteger("weight");
        this.staff = document.getBoolean("staff");
        this.everyone = document.getBoolean("everyone");

        return this;
    }

    public Document toDocument() {
        return new Document()
                .append("name", name)
                .append("display_name", displayName)
                .append("prefix", prefix)
                .append("permission", permission)
                .append("weight", weight)
                .append("staff", staff)
                .append("everyone", everyone);
    }
}
