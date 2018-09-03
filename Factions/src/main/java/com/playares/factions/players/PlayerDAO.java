package com.playares.factions.players;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.connect.mongodb.MongoDB;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

public final class PlayerDAO {
    private static final String DB_NAME = "factions";
    private static final String DB_COLL = "players";

    public static FactionPlayer getPlayer(MongoDB database, Bson filter) {
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

        return new FactionPlayer().fromDocument(existing);
    }

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

    public static ImmutableList<FactionPlayer> getPlayers(MongoDB database) {
        final List<FactionPlayer> result = Lists.newArrayList();
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final MongoCursor<Document> cursor;

        if (collection == null) {
            return ImmutableList.of();
        }

        cursor = collection.find().iterator();

        while (cursor.hasNext()) {
            result.add(new FactionPlayer().fromDocument(cursor.next()));
        }

        return ImmutableList.copyOf(result);
    }
}