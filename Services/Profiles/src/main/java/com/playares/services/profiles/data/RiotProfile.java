package com.playares.services.profiles.data;

import com.google.common.collect.Lists;
import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.base.util.Time;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class RiotProfile implements MongoDocument<RiotProfile> {
    @Getter
    public UUID uniqueId;

    @Getter @Setter
    public String username;

    @Getter @Setter
    public int address;

    @Getter
    public long createTimestamp;

    @Getter
    public AresProfileSettings settings;

    public RiotProfile() {
        this.uniqueId = null;
        this.username = null;
        this.address = 0;
        this.createTimestamp = Time.now();
        this.settings = new AresProfileSettings();
    }

    public RiotProfile(UUID uniqueId, String username) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.address = 0;
        this.createTimestamp = Time.now();
        this.settings = new AresProfileSettings();
    }

    public RiotProfile(UUID uniqueId, String username, int address, long createTimestamp, AresProfileSettings settings) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.address = address;
        this.createTimestamp = createTimestamp;
        this.settings = settings;
    }

    public RiotProfile fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.username = document.getString("username");
        this.address = document.getInteger("address");
        this.createTimestamp = document.getLong("created");
        this.settings = new AresProfileSettings().fromDocument(document.get("settings", Document.class));

        return this;
    }

    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("username", username)
                .append("address", address)
                .append("created", createTimestamp)
                .append("settings", settings.toDocument());
    }

    public final class AresProfileSettings implements MongoDocument<AresProfileSettings> {
        @Getter @Setter
        public boolean hidingGlobalChat;

        @Getter @Setter
        public boolean hidingPrivateMessages;

        @Getter @Setter
        public boolean hidingTips;

        @Getter
        public List<UUID> ignored;

        public AresProfileSettings() {
            this.hidingGlobalChat = false;
            this.hidingPrivateMessages = false;
            this.hidingTips = false;
            this.ignored = Collections.synchronizedList(Lists.<UUID>newArrayList());
        }

        public boolean isIgnoring(UUID uniqueId) {
            return ignored.contains(uniqueId);
        }

        @SuppressWarnings("unchecked")
        public AresProfileSettings fromDocument(Document document) {
            this.hidingGlobalChat = document.getBoolean("hiding_global_chat");
            this.hidingPrivateMessages = document.getBoolean("hiding_private_messages");
            this.hidingTips = document.getBoolean("hiding_tips");
            this.ignored = (List<UUID>)document.get("ignored");

            return this;
        }

        public Document toDocument() {
            return new Document()
                    .append("hiding_global_chat", hidingGlobalChat)
                    .append("hiding_private_messages", hidingPrivateMessages)
                    .append("hiding_tips", hidingTips)
                    .append("ignored", ignored);
        }
    }
}