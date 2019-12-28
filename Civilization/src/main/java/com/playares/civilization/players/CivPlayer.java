package com.playares.civilization.players;

import com.playares.civilization.Civilizations;
import com.playares.civilization.addons.chatchannels.ChatChannelType;
import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.bukkit.location.PLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class CivPlayer implements MongoDocument<CivPlayer> {
    @Getter public final Civilizations plugin;
    @Getter public UUID uniqueId;
    @Getter @Setter public String username;
    @Getter @Setter public ChatChannelType chatChannel;
    @Getter @Setter public PLocatable location;

    public CivPlayer(Civilizations plugin) {
        this.plugin = plugin;
        this.uniqueId = null;
        this.username = null;
        this.chatChannel = ChatChannelType.LOCAL;
        this.location = null;
    }

    public CivPlayer(Civilizations plugin, Player player) {
        this.plugin = plugin;
        this.uniqueId = player.getUniqueId();
        this.username = player.getName();
        this.chatChannel = ChatChannelType.LOCAL;
        this.location = null;
    }

    public CivPlayer(Civilizations plugin, UUID uniqueId, String username) {
        this.plugin = plugin;
        this.uniqueId = uniqueId;
        this.username = username;
        this.chatChannel = ChatChannelType.LOCAL;
        this.location = null;
    }

    public void sendMessage(String message) {
        if (getPlayer() != null) {
            getPlayer().sendMessage(message);
        }
    }

    public Player getPlayer() {
        if (uniqueId == null) {
            return null;
        }

        return Bukkit.getPlayer(uniqueId);
    }

    public void updateLocation() {
        if (getPlayer() == null) {
            location = null;
            return;
        }

        setLocation(new PLocatable(getPlayer()));
    }

    @Override
    public CivPlayer fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.username = document.getString("username");
        this.chatChannel = ChatChannelType.valueOf(document.getString("chat_channel"));

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("username", username)
                .append("chat_channel", chatChannel.name());
    }
}