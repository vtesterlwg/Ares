package com.playares.factions.claims.subclaims.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.factions.Factions;
import com.playares.factions.claims.data.Claimable;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public final class Subclaim implements Claimable, MongoDocument<Subclaim> {
    @Getter public final Factions plugin;
    @Getter public UUID uniqueId;
    @Getter public UUID ownerId;
    @Getter public final List<BLocatable> blocks;
    @Getter public final Set<UUID> accessPlayers;
    @Getter @Setter public PlayerFaction.FactionRank accessLevel;

    public Subclaim(Factions plugin) {
        this.plugin = plugin;
        this.uniqueId = UUID.randomUUID();
        this.ownerId = null;
        this.accessPlayers = Sets.newConcurrentHashSet();
        this.blocks = Lists.newArrayList();
    }

    public Subclaim(Factions plugin, Player player, PlayerFaction faction, Collection<BLocatable> blocks) {
        this.plugin = plugin;
        this.uniqueId = UUID.randomUUID();
        this.ownerId = faction.getUniqueId();
        this.blocks = Lists.newArrayList(blocks);
        this.accessPlayers = Sets.newConcurrentHashSet();
        this.accessLevel = PlayerFaction.FactionRank.LEADER;

        accessPlayers.add(player.getUniqueId());
    }

    public PlayerFaction getFaction() {
        return plugin.getFactionManager().getPlayerFactionById(ownerId);
    }

    public boolean canAccess(UUID uniqueId) {
        final PlayerFaction faction = getFaction();

        if (faction != null) {
            final PlayerFaction.FactionProfile profile = faction.getMember(uniqueId);

            if (profile != null && profile.getRank().isHigherOrEqual(accessLevel)) {
                return true;
            }
        }

        return accessPlayers.contains(uniqueId);
    }

    public boolean canAccess(UUID uniqueId, PlayerFaction.FactionRank rank) {
        return rank.isHigherOrEqual(accessLevel) || accessPlayers.contains(uniqueId);
    }

    public boolean match(Block block) {
        for (BLocatable loc : blocks) {
            if (
                    loc.getWorldName().equals(block.getWorld().getName()) &&
                    loc.getX() == block.getX() &&
                    loc.getY() == block.getY() &&
                    loc.getZ() == block.getZ()) {

                return true;

            }
        }

        return false;
    }

    public void remove(Block block) {
        for (BLocatable loc : blocks) {
            if (
                    loc.getWorldName().equals(block.getWorld().getName()) &&
                    loc.getX() == block.getX() &&
                    loc.getY() == block.getY() &&
                    loc.getZ() == block.getZ()) {

                blocks.remove(loc);
                break;

            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subclaim fromDocument(Document document) {
        final List<UUID> playerEntries = (List<UUID>)document.get("access_players");
        final List<Document> blockDocuments = (List<Document>)document.get("blocks");

        uniqueId = (UUID)document.get("id");
        ownerId = (UUID)document.get("owner_id");
        accessLevel = PlayerFaction.FactionRank.valueOf(document.getString("access_level"));
        accessPlayers.addAll(playerEntries);

        for (Document blockDoc : blockDocuments) {
            blocks.add(new BLocatable().fromDocument(blockDoc));
        }

        return this;
    }

    @Override
    public Document toDocument() {
        final List<UUID> playerEntries = Lists.newArrayList(accessPlayers);
        final List<Document> blockDocuments = Lists.newArrayList();

        for (BLocatable block : blocks) {
            blockDocuments.add(block.toDocument());
        }

        return new Document()
                .append("id", uniqueId)
                .append("owner_id", ownerId)
                .append("blocks", blockDocuments)
                .append("access_players", playerEntries)
                .append("access_level", accessLevel.name());
    }
}
