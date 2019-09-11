package com.playares.services.profiles.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.connect.mongodb.MongoDB;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

public final class AresProfileDAO {
    private static final String DB_NAME = "ares";
    private static final String DB_COLL = "profiles";

    public static void insertProfile(MongoDB database, AresProfile profile) {
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

    public static void deleteProfile(MongoDB database, AresProfile profile) {
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

    public static AresProfile getProfile(MongoDB database, Bson filter) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final Document existing;

        if (collection == null) {
            return null;
        }

        existing = collection.find(filter).first();

        if (existing == null) {
            return null;
        }

        return new AresProfile().fromDocument(existing);
    }

    public static ImmutableList<AresProfile> getProfiles(MongoDB database, Bson filter) {
        final List<AresProfile> results = Lists.newArrayList();
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final MongoCursor<Document> cursor;

        if (collection == null) {
            return ImmutableList.copyOf(results);
        }

        cursor = collection.find(filter).iterator();

        while (cursor.hasNext()) {
            results.add(new AresProfile().fromDocument(cursor.next()));
        }

        return ImmutableList.copyOf(results);
    }
}