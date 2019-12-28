package com.playares.civilization.players;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.playares.civilization.Civilizations;
import com.playares.commons.base.connect.mongodb.MongoDB;
import org.bson.Document;
import org.bson.conversions.Bson;

public final class CivPlayerDAO {
    private static final String DB_NAME = "civilizations";
    private static final String DB_COLL = "players";

    public static CivPlayer get(Civilizations plugin, MongoDB database, Bson filter) {
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

        return new CivPlayer(plugin).fromDocument(existing);
    }

    public static void save(MongoDB database, CivPlayer player) {
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