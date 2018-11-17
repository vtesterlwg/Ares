package com.playares.services.ranks.data;

import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.connect.mongodb.MongoDB;
import org.bson.Document;

import java.util.List;

public final class RiotRankDAO {
    private static final String DB_NAME = "ares";
    private static final String DB_COLL = "ranks";

    public static void insertRank(MongoDB database, RiotRank rank) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final Document existing;

        if (collection == null) {
            return;
        }

        existing = collection.find(Filters.eq("name", rank.getName())).first();

        if (existing != null) {
            collection.replaceOne(existing, rank.toDocument());
            return;
        }

        collection.insertOne(rank.toDocument());
    }

    public static void deleteRank(MongoDB database, RiotRank rank) {
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

    public static List<RiotRank> getRanks(MongoDB database) {
        final List<RiotRank> result = Lists.newArrayList();
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final MongoCursor<Document> cursor;

        if (collection == null) {
            return result;
        }

        cursor = collection.find().iterator();

        while (cursor.hasNext()) {
            result.add(new RiotRank().fromDocument(cursor.next()));
        }

        return result;
    }
}
