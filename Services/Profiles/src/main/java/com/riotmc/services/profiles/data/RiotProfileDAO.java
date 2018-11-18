package com.riotmc.services.profiles.data;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.riotmc.commons.base.connect.mongodb.MongoDB;
import org.bson.Document;
import org.bson.conversions.Bson;

public final class RiotProfileDAO {
    private static final String DB_NAME = "ares";
    private static final String DB_COLL = "profiles";

    public static void insertProfile(MongoDB database, RiotProfile profile) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final Document existing;

        if (collection == null) {
            return;
        }

        existing = collection.find(Filters.eq("id", profile.getUniqueId())).first();

        if (existing != null) {
            collection.replaceOne(existing, profile.toDocument());
            return;
        }

        collection.insertOne(profile.toDocument());
    }

    public static void deleteProfile(MongoDB database, RiotProfile profile) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final Document existing;

        if (collection == null) {
            return;
        }

        existing = collection.find(Filters.eq("id", profile.getUniqueId())).first();

        if (existing == null) {
            return;
        }

        collection.deleteOne(existing);
    }

    public static RiotProfile getProfile(MongoDB database, Bson filter) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final Document existing;

        if (collection == null) {
            return null;
        }

        existing = collection.find(filter).first();

        if (existing == null) {
            return null;
        }

        return new RiotProfile().fromDocument(existing);
    }
}
