package com.playares.factions.claims.subclaims.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.Factions;
import com.playares.factions.claims.subclaims.data.Subclaim;
import org.bson.Document;

import java.util.Collection;
import java.util.List;

public final class SubclaimDAO {
    private static final String DB_NAME = "factions";
    private static final String DB_COLL = "subclaims";

    public static ImmutableList<Subclaim> getSubclaims(Factions plugin, MongoDB database) {
        final List<Subclaim> subclaims = Lists.newArrayList();
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final MongoCursor<Document> cursor;

        if (collection == null) {
            return ImmutableList.copyOf(subclaims);
        }

        cursor = collection.find().iterator();

        while (cursor.hasNext()) {
            subclaims.add(new Subclaim(plugin).fromDocument(cursor.next()));
        }

        return ImmutableList.copyOf(subclaims);
    }

    public static void saveSubclaims(MongoDB database, Collection<Subclaim> subclaims) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);

        if (collection == null) {
            Logger.error("Collection '" + DB_COLL + "' was not found and subclaims were not saved");
            return;
        }

        subclaims.forEach(subclaim -> {
            final Document existing = collection.find(Filters.eq("id", subclaim.getUniqueId())).first();

            if (existing != null) {
                collection.replaceOne(existing, subclaim.toDocument());
            } else {
                collection.insertOne(subclaim.toDocument());
            }
        });
    }

    public static void deleteSubclaims(MongoDB database, Collection<Subclaim> subclaims) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);

        if (collection == null) {
            Logger.error("Collection '" + DB_COLL + "' was not found and subclaims were not deleted");
            return;
        }

        subclaims.forEach(subclaim -> {
            final Document existing = collection.find(Filters.eq("id", subclaim.getUniqueId())).first();

            if (existing != null) {
                collection.deleteOne(existing);
            }
        });
    }

    public static void deleteSubclaim(MongoDB database, Subclaim subclaim) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);

        if (collection == null) {
            Logger.error("Collection '" + DB_COLL + "' was not found and a subclaim was not deleted");
            return;
        }

        final Document existing = collection.find(Filters.eq("id", subclaim.getUniqueId())).first();

        if (existing != null) {
            collection.deleteOne(existing);
        }
    }
}
