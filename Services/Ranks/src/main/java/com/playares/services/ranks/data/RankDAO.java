package com.playares.services.ranks.data;

import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.connect.mongodb.MongoDB;
import org.bson.Document;

import java.util.List;

public final class RankDAO {
    private static final String DB_NAME = "ares";
    private static final String DB_COLL = "ranks";

    /**
     * Inserts a new Rank document in to the database
     * @param database Database
     * @param rank Rank
     */
    public static void create(MongoDB database, Rank rank) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final Document existing;

        if (collection == null) {
            return;
        }

        existing = collection.find(Filters.eq("name", rank.getName())).first();

        if (existing != null) {
            update(database, rank);
            return;
        }

        collection.insertOne(rank.toDocument());
    }

    /**
     * Updates an existing Rank document in the database
     * @param database Database
     * @param rank Rank
     */
    public static void update(MongoDB database, Rank rank) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final Document existing;

        if (collection == null) {
            return;
        }

        existing = collection.find(Filters.eq("name", rank.getName())).first();

        if (existing == null) {
            collection.insertOne(rank.toDocument());
            return;
        }

        collection.replaceOne(existing, rank.toDocument());
    }

    /**
     * Deletes a rank from the database
     * @param database Database
     * @param rank Rank
     */
    public static void delete(MongoDB database, Rank rank) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final Document existing;

        if (collection == null) {
            return;
        }

        existing = collection.find(Filters.eq("name", rank.getName())).first();

        if (existing == null) {
            return;
        }

        collection.deleteOne(existing);
    }

    /**
     * Returns a collection containing all ranks in the database
     * @param database Database
     * @return Collection containing all ranks
     */
    public static List<Rank> getRanks(MongoDB database) {
        final List<Rank> result = Lists.newArrayList();
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final MongoCursor<Document> cursor;

        if (collection == null) {
            return result;
        }

        cursor = collection.find().iterator();

        while (cursor.hasNext()) {
            result.add(new Rank().fromDocument(cursor.next()));
        }

        return result;
    }
}