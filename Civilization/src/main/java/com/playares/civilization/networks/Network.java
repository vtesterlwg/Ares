package com.playares.civilization.networks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.civilization.Civilizations;
import com.playares.commons.base.connect.mongodb.MongoDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Network implements MongoDocument<Network> {
    @Getter public final Civilizations plugin;
    @Getter public UUID uniqueId;
    @Getter public String name;
    @Getter public String password;
    @Getter public NetworkSettings settings;
    @Getter public Set<NetworkProfile> members;
    @Getter public Set<UUID> pendingInvitations;

    public Network(Civilizations plugin) {
        this.plugin = plugin;
        this.uniqueId = null;
        this.name = null;
        this.password = null;
        this.settings = new NetworkSettings();
        this.members = Sets.newConcurrentHashSet();
        this.pendingInvitations = Sets.newConcurrentHashSet();
    }

    public Network(Civilizations plugin, String name, Player creator) {
        this.plugin = plugin;
        this.uniqueId = UUID.randomUUID();
        this.name = name;
        this.password = null;
        this.settings = new NetworkSettings();
        this.members = Sets.newConcurrentHashSet();
        this.pendingInvitations = Sets.newConcurrentHashSet();

        final NetworkProfile creatorProfile = new NetworkProfile(creator.getUniqueId());
        creatorProfile.permissions.put(NetworkPermission.ADMIN, true);
    }

    @Override
    public Network fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.name = document.getString("name");
        this.password = document.getString("password");
        this.settings = new NetworkSettings().fromDocument(document.get("settings", Document.class));
        this.members = Sets.newConcurrentHashSet();
        this.pendingInvitations = Sets.newConcurrentHashSet();

        @SuppressWarnings("unchecked") final List<Document> membersList = document.get("members", List.class);
        membersList.forEach(memberDocument -> members.add(new NetworkProfile().fromDocument(memberDocument)));

        return this;
    }

    @Override
    public Document toDocument() {
        final Document document = new Document();
        final List<Document> profiles = Lists.newArrayList();

        members.forEach(member -> profiles.add(member.toDocument()));

        document.append("id", uniqueId)
                .append("name", name)
                .append("password", password)
                .append("settings", settings.toDocument()
                .append("members", profiles));

        return document;
    }

    public final class NetworkProfile implements MongoDocument<NetworkProfile> {
        @Getter public UUID uniqueId;
        @Getter public Map<NetworkPermission, Boolean> permissions;

        public NetworkProfile() {
            this.uniqueId = null;
            this.permissions = Maps.newHashMap();

            for (NetworkPermission permission : NetworkPermission.values()) {
                permissions.put(permission, permission.getDefaultValue());
            }
        }

        public NetworkProfile(UUID uniqueId) {
            this.uniqueId = uniqueId;
            this.permissions = Maps.newHashMap();

            for (NetworkPermission permission : NetworkPermission.values()) {
                permissions.put(permission, permission.getDefaultValue());
            }
        }

        public boolean isAdmin() {
            return permissions.get(NetworkPermission.ADMIN);
        }

        public boolean hasPermission(NetworkPermission permission) {
            return permissions.getOrDefault(permission, false);
        }

        @Override
        public NetworkProfile fromDocument(Document document) {
            this.uniqueId = (UUID)document.get("id");
            this.permissions = Maps.newHashMap();

            for (NetworkPermission permission : NetworkPermission.values()) {
                permissions.put(permission, document.getBoolean(permission.name().toLowerCase()));
            }

            return this;
        }

        @Override
        public Document toDocument() {
            final Document document = new Document();

            document.append("id", uniqueId);

            for (NetworkPermission permission : NetworkPermission.values()) {
                document.append(permission.name().toLowerCase(), permissions.getOrDefault(permission, false));
            }

            return document;
        }
    }

    public final class NetworkSettings implements MongoDocument<NetworkSettings> {
        @Getter @Setter public boolean passwordJoinable;
        @Getter @Setter public boolean snitchNotificationsEnabled;

        public NetworkSettings() {
            this.passwordJoinable = false;
            this.snitchNotificationsEnabled = true;
        }

        public NetworkSettings(boolean passwordJoinable, boolean snitchNotificationsEnabled) {
            this.passwordJoinable = passwordJoinable;
            this.snitchNotificationsEnabled = snitchNotificationsEnabled;
        }

        @Override
        public NetworkSettings fromDocument(Document document) {
            this.passwordJoinable = document.getBoolean("password_joinable");
            this.snitchNotificationsEnabled = document.getBoolean("snitch_notifications_enabled");

            return this;
        }

        @Override
        public Document toDocument() {
            return new Document()
                    .append("password_joinable", passwordJoinable)
                    .append("snitch_notifications_enabled", snitchNotificationsEnabled);
        }
    }

    @AllArgsConstructor
    public enum NetworkPermission {
        ADMIN("Admin", false),
        KICK_MEMBERS("Kick Members", false),
        INVITE_MEMBERS("Invite Members", false),
        MODIFY_CLAIMS("Claim/Unclaim Land", true),
        ACCESS_LAND("Access Land", true),
        VIEW_SNITCHES("View Snitch Notifications", true),
        MODIFY_SNITCHES("Create/Delete Snitches", true),
        CHANGE_PASSWORD("Change Network Password", false),
        CHANGE_SETTINGS("Change Network Settings", false);

        @Getter public final String displayName;
        private final boolean defaultValue;

        public boolean getDefaultValue() {
            return defaultValue;
        }
    }
}