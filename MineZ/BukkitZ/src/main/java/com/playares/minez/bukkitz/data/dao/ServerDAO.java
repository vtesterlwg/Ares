package com.playares.minez.bukkitz.data.dao;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.minez.bukkitz.data.MZServer;
import org.bson.Document;

import java.util.Set;

public final class ServerDAO {
    private static final String DB_NAME = "minez";
    private static final String DB_COLL = "servers";

    public static void pushServer(MongoDB database, MZServer server) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final Document exisiting;

        if (collection == null) {
            return;
        }

        exisiting = collection.find(Filters.eq("id", server.getId())).first();

        if (exisiting != null) {
            collection.replaceOne(exisiting, server.toDocument());
        } else {
            collection.insertOne(server.toDocument());
        }
    }

    public static ImmutableSet<MZServer> pullServers(int currentServerId, MongoDB database) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final FindIterable<Document> iter;
        final Set<MZServer> result = Sets.newHashSet();

        if (collection == null) {
            return null;
        }

        iter = collection.find(Filters.not(Filters.eq("id", currentServerId)));

        for (Document document : iter) {
            result.add(new MZServer().fromDocument(document));
        }

        return ImmutableSet.copyOf(result);
    }
}