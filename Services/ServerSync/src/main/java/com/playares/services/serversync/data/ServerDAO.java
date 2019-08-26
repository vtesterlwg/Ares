package com.playares.services.serversync.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.commons.bukkit.AresPlugin;
import org.bson.Document;

import java.util.List;

public final class ServerDAO {
    private static final String DB_NAME = "ares";
    private static final String DB_COLL = "servers";

    public static ImmutableList<Server> getServers(AresPlugin owner, MongoDB database) {
        final List<Server> result = Lists.newArrayList();
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);

        if (collection == null) {
            return ImmutableList.copyOf(result);
        }

        for (Document document : collection.find()) {
            result.add(new Server(owner).fromDocument(document));
        }

        return ImmutableList.copyOf(result);
    }

    public static void saveServer(MongoDB database, Server server) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);

        if (collection == null) {
            return;
        }

        final Document existing = collection.find(Filters.and(Filters.eq("id", server.getId()), Filters.eq("type", server.getType().name()))).first();

        if (existing != null) {
            collection.replaceOne(existing, server.toDocument());
        } else {
            collection.insertOne(server.toDocument());
        }
    }
}