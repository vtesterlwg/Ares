package com.riotmc.services.deathban.dao;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.riotmc.commons.base.connect.mongodb.MongoDB;
import com.riotmc.commons.base.util.Time;
import com.riotmc.services.deathban.data.Deathban;
import org.bson.Document;

import java.util.UUID;

public final class DeathbanDAO {
    private static final String DB_NAME = "factions";
    private static final String DB_COLL = "deathbans";

    public static Deathban getDeathban(MongoDB database, UUID ownerId) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final FindIterable<Document> iter;
        final Document existing;

        if (collection == null) {
            return null;
        }

        iter = collection.find(Filters.and(Filters.eq("id", ownerId), Filters.gt("unban", Time.now())));
        existing = iter.first();

        if (existing == null) {
            return null;
        }

        return new Deathban().fromDocument(existing);
    }

    public static void saveDeathban(MongoDB database, Deathban deathban) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final FindIterable<Document> iter;
        final Document existing;
        final Document document = deathban.toDocument();

        if (collection == null) {
            return;
        }

        iter = collection.find(Filters.eq("id", deathban.getOwnerId()));
        existing = iter.first();

        if (existing != null) {
            collection.replaceOne(existing, document);
        } else {
            collection.insertOne(document);
        }
    }

    public static void clearDeathbans(MongoDB database) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);

        if (collection == null) {
            return;
        }

        collection.drop();
    }

    public static void deleteDeathban(MongoDB database, Deathban deathban) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final FindIterable<Document> iter;
        final Document existing;

        if (collection == null) {
            return;
        }

        iter = collection.find(Filters.eq("id", deathban.getOwnerId()));
        existing = iter.first();

        if (existing == null) {
            return;
        }

        collection.deleteOne(existing);
    }
}
