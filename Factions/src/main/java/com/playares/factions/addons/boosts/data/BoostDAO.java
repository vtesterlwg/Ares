package com.playares.factions.addons.boosts.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.connect.mongodb.MongoDB;
import org.bson.Document;

import java.util.List;
import java.util.UUID;

public final class BoostDAO {
    private static final String DB_NAME = "factions";
    private static final String DB_COLL = "boosts";

    public static ImmutableList<Boost> getBoosts(MongoDB database, UUID ownerId) {
        final List<Boost> results = Lists.newArrayList();
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final MongoCursor<Document> cursor;

        if (collection == null) {
            return null;
        }

        cursor = collection.find(Filters.eq("owner", ownerId)).iterator();

        while (cursor.hasNext()) {
            results.add(new Boost().fromDocument(cursor.next()));
        }

        return ImmutableList.copyOf(results);
    }

    public static void saveBoost(MongoDB database, Boost boost) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final FindIterable<Document> iter;
        final Document existing;

        if (collection == null) {
            return;
        }

        iter = collection.find(Filters.eq("id", boost.getUniqueId()));
        existing = iter.first();

        if (existing != null) {
            collection.replaceOne(existing, boost.toDocument());
        } else {
            collection.insertOne(boost.toDocument());
        }
    }

    public static void deleteBoost(MongoDB database, Boost boost) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final FindIterable<Document> iter;
        final Document existing;

        if (collection == null) {
            return;
        }

        iter = collection.find(Filters.eq("id", boost.getUniqueId()));
        existing = iter.first();

        if (existing != null) {
            collection.deleteOne(existing);
        }
    }
}