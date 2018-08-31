package com.playares.factions.factions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.Factions;
import org.bson.Document;

import java.util.Collection;
import java.util.List;

public final class FactionDAO {
    private static final String DB_NAME = "factions";
    private static final String DB_SF_COLL = "server_factions";
    private static final String DB_PF_COLL = "player_factions";

    public static ImmutableList<Faction> getFactions(Factions plugin, MongoDB database) {
        final List<Faction> factions = Lists.newArrayList();
        final MongoCollection<Document> playerCollection = database.getCollection(DB_NAME, DB_PF_COLL);
        final MongoCollection<Document> serverCollection = database.getCollection(DB_NAME, DB_SF_COLL);

        if (playerCollection != null) {
            for (Document document : playerCollection.find()) {
                factions.add(new PlayerFaction(plugin).fromDocument(document));
            }
        }

        if (serverCollection != null) {
            for (Document document : serverCollection.find()) {
                factions.add(new ServerFaction(plugin).fromDocument(document));
            }
        }

        return ImmutableList.copyOf(factions);
    }

    public static void saveFactions(MongoDB database, Collection<Faction> factions) {
        final MongoCollection<Document> playerCollection = database.getCollection(DB_NAME, DB_PF_COLL);
        final MongoCollection<Document> serverCollection = database.getCollection(DB_NAME, DB_SF_COLL);

        factions.forEach(faction -> {
            if (faction instanceof ServerFaction && serverCollection != null) {
                final Document existing = serverCollection.find(Filters.eq("id", faction.getUniqueId())).first();

                if (existing != null) {
                    serverCollection.replaceOne(existing, ((ServerFaction) faction).toDocument());
                } else {
                    serverCollection.insertOne(((ServerFaction) faction).toDocument());
                }
            } else if (faction instanceof PlayerFaction && playerCollection != null) {
                final Document existing = playerCollection.find(Filters.eq("id", faction.getUniqueId())).first();

                if (existing != null) {
                    playerCollection.replaceOne(existing, ((PlayerFaction) faction).toDocument());
                } else {
                    playerCollection.insertOne(((PlayerFaction) faction).toDocument());
                }
            }
        });
    }

    public static void deleteFaction(MongoDB database, Faction faction) {
        if (faction instanceof ServerFaction) {
            final ServerFaction sf = (ServerFaction)faction;
            final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_SF_COLL);

            if (collection == null) {
                Logger.error("Collection '" + DB_SF_COLL + "' was not found, failed to delete faction");
                return;
            }

            final Document existing = collection.find(Filters.eq("id", sf.getUniqueId())).first();

            if (existing != null) {
                collection.deleteOne(existing);
            }

            return;
        }

        if (faction instanceof PlayerFaction) {
            final PlayerFaction pf = (PlayerFaction)faction;
            final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_PF_COLL);

            if (collection == null) {
                Logger.error("Collection '" + DB_PF_COLL + "' was not found, failed to delete faction");
                return;
            }

            final Document existing = collection.find(Filters.eq("id", pf.getUniqueId())).first();

            if (existing != null) {
                collection.deleteOne(existing);
            }
        }
    }
}