package com.playares.arena.player;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.connect.mongodb.MongoDB;
import org.bson.Document;

public final class ArenaPlayerDAO {
    private static final String DB_NAME = "arena";
    private static final String DB_COLL = "players";

    public static void getRankedData(MongoDB db, ArenaPlayer player) {
        final MongoCollection<Document> collection = db.getCollection(DB_NAME, DB_COLL);
        final FindIterable<Document> iter;
        final Document existing;

        if (collection == null) {
            return;
        }

        iter = collection.find(Filters.eq("owner", player.getUniqueId()));
        existing = iter.first();

        if (existing == null) {
            return;
        }

        player.getRankedData().fromDocument(existing);
    }

    public static void saveRankedData(MongoDB db, ArenaPlayer player) {
        final MongoCollection<Document> collection = db.getCollection(DB_NAME, DB_COLL);
        final FindIterable<Document> iter;
        final Document existing;

        if (collection == null) {
            return;
        }

        iter = collection.find(Filters.eq("owner", player.getUniqueId()));
        existing = iter.first();

        if (existing != null) {
            collection.replaceOne(existing, player.getRankedData().toDocument());
        } else {
            collection.insertOne(player.getRankedData().toDocument());
        }
    }
}
