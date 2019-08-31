package com.playares.factions.factions.data;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.factions.Factions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.ChatColor;

import java.util.UUID;

public final class ServerFaction implements Faction, MongoDocument<ServerFaction> {
    @Getter public final Factions plugin;
    /** Unique ID **/
    @Getter public UUID uniqueId;
    /** Name of this faction **/
    @Getter @Setter public String name;
    /** Display name of this faction, shows up whenever called for in chat **/
    @Getter @Setter public String displayName;
    /** Location this faction at, used in '/f show' display **/
    @Getter @Setter public PLocatable location;
    /** Flag to determine how this claim should behave **/
    @Getter @Setter public FactionFlag flag;
    /** Buffer for claiming near this faction **/
    @Getter @Setter public double claimBuffer;
    /** Buffer for building near this faction **/
    @Getter @Setter public double buildBuffer;

    public ServerFaction(Factions plugin) {
        this.plugin = plugin;
        this.uniqueId = UUID.randomUUID();
        this.name = null;
        this.displayName = null;
        this.location = null;
        this.flag = null;
        this.claimBuffer = plugin.getFactionConfig().getDefaultServerClaimBuffer();
        this.buildBuffer = plugin.getFactionConfig().getDefaultServerBuildBuffer();
    }

    public ServerFaction(Factions plugin, String name) {
        this.plugin = plugin;
        this.uniqueId = UUID.randomUUID();
        this.name = name;
        this.displayName = name;
        this.location = null;
        this.flag = FactionFlag.SAFEZONE;
        this.claimBuffer = plugin.getFactionConfig().getDefaultServerClaimBuffer();
        this.buildBuffer = plugin.getFactionConfig().getDefaultServerBuildBuffer();
    }

    @Override
    public ServerFaction fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.name = document.getString("name");

        this.displayName = (document.get("display_name") != null) ?
                ChatColor.translateAlternateColorCodes('&', document.getString("display_name")) : name;

        this.location = (document.get("location") != null) ?
                new PLocatable().fromDocument(document.get("location", Document.class)) : null;

        this.flag = (document.get("flag") != null) ?
                FactionFlag.valueOf(document.getString("flag")) : null;

        this.claimBuffer = document.getDouble("claim_buffer");

        this.buildBuffer = document.getDouble("build_buffer");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("name", name)
                .append("display_name", displayName)
                .append("location", (location != null) ? location.toDocument() : null)
                .append("flag", (flag != null) ? flag.toString() : null)
                .append("claim_buffer", claimBuffer)
                .append("build_buffer", buildBuffer);
    }

    @AllArgsConstructor
    public enum FactionFlag {
        /**
         * Disables PvP
         * Prevents players with Combat-tag from entering
         * Freezes PvP Protection timers
         * Disables enderpearl landing if thrown by a combat-tagged player
         */
        SAFEZONE(ChatColor.GREEN + "Safezone"),

        /**
         * Prevents players with PvP-Protection from entering
         * If the connected event is active all deaths in this claim may have different attributes than normal
         * Players will be escorted outside of this factions claims if they log out inside them
         */
        EVENT(ChatColor.DARK_AQUA + "Event"),

        /**
         * Prevents block modification
         */
        LANDMARK(ChatColor.LIGHT_PURPLE + "Landmark");

        /** Display name for this flag **/
        @Getter public final String displayName;
    }
}