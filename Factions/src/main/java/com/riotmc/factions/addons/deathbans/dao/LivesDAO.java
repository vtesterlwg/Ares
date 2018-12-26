package com.riotmc.factions.addons.deathbans.dao;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.riotmc.commons.base.connect.mongodb.MongoDB;
import com.riotmc.factions.addons.deathbans.data.LivesPlayer;
import org.bson.Document;
import org.bson.conversions.Bson;

public final class LivesDAO {
    private static final String DB_NAME = "factions";
    private static final String DB_COLL = "lives";

    public static LivesPlayer getLivesPlayer(MongoDB database, Bson filter) {
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

        return new LivesPlayer().fromDocument(existing);
    }

    public static void saveLivesPlayer(MongoDB database, LivesPlayer player) {
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
}