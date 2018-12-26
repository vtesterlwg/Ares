package com.riotmc.factions.players.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.riotmc.commons.base.connect.mongodb.MongoDB;
import com.riotmc.factions.Factions;
import com.riotmc.factions.players.data.FactionPlayer;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

public final class PlayerDAO {
    /** Factions Database Name **/
    private static final String DB_NAME = "factions";
    /** Players Collection Name **/
    private static final String DB_COLL = "players";

    /**
     * Retrieves a FactionPlayer object from the db
     *
     * Will return null if no entry is found
     * @param plugin Plugin
     * @param database MongoDB Database
     * @param filter Bson Filter
     * @return FactionPlayer
     */
    public static FactionPlayer getPlayer(Factions plugin, MongoDB database, Bson filter) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final FindIterable<Document> iter;
        final Document existing;

        if (collection == null) {
            return null;
        }

        iter = collection.find(filter);
        existing = iter.first();

        if (existing == null) {
            return null;
        }

        return new FactionPlayer(plugin).fromDocument(existing);
    }

    /**
     * Saves a player to the db
     * @param database MongoDB Database
     * @param player Player
     */
    public static void savePlayer(MongoDB database, FactionPlayer player) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final FindIterable<Document> iter;
        final Document existing;
        final Document document = player.toDocument();

        if (collection == null) {
            return;
        }

        iter = collection.find(Filters.eq("id", player.getUniqueId()));
        existing = iter.first();

        if (existing != null) {
            collection.replaceOne(existing, document);
        } else {
            collection.insertOne(document);
        }
    }

    /**
     * Retrieves every FactionPlayer in the db
     * @param plugin Plugin
     * @param database MongoDB Database
     * @return ImmutableList containing every FactionPlayer found
     */
    public static ImmutableList<FactionPlayer> getPlayers(Factions plugin, MongoDB database) {
        final List<FactionPlayer> result = Lists.newArrayList();
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final MongoCursor<Document> cursor;

        if (collection == null) {
            return ImmutableList.of();
        }

        cursor = collection.find().iterator();

        while (cursor.hasNext()) {
            result.add(new FactionPlayer(plugin).fromDocument(cursor.next()));
        }

        return ImmutableList.copyOf(result);
    }
}