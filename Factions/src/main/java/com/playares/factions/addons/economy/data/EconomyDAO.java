package com.playares.factions.addons.economy.data;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.factions.addons.economy.EconomyAddon;
import org.bson.Document;

import java.util.UUID;

public final class EconomyDAO {
    private static final String DB_NAME = "factions";
    private static final String DB_COLL = "economy";

    public static EconomyPlayer getPlayer(EconomyAddon addon, MongoDB database, UUID uniqueId) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final Document existing;

        if (collection == null) {
            return new EconomyPlayer(addon, uniqueId);
        }

        existing = collection.find(Filters.eq("id", uniqueId)).first();

        if (existing == null) {
            return null;
        }

        return new EconomyPlayer(addon).fromDocument(existing);
    }

    public static void savePlayer(MongoDB database, EconomyPlayer player) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final Document existing;
        final Document document = player.toDocument();

        if (collection == null) {
            return;
        }

        existing = collection.find(Filters.eq("id", player.getUniqueId())).first();

        if (existing != null) {
            collection.replaceOne(existing, document);
        } else {
            collection.insertOne(document);
        }
    }
}