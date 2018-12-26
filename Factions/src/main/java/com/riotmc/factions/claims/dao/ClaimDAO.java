package com.riotmc.factions.claims.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.riotmc.commons.base.connect.mongodb.MongoDB;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.factions.Factions;
import com.riotmc.factions.claims.data.DefinedClaim;
import org.bson.Document;

import java.util.Collection;
import java.util.List;

public final class ClaimDAO {
    private static final String DB_NAME = "factions";
    private static final String DB_COLL = "claims";

    public static ImmutableList<DefinedClaim> getDefinedClaims(Factions plugin, MongoDB database) {
        final List<DefinedClaim> claims = Lists.newArrayList();
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final MongoCursor<Document> cursor;

        if (collection == null) {
            return ImmutableList.copyOf(claims);
        }

        cursor = collection.find().iterator();

        while (cursor.hasNext()) {
            claims.add(new DefinedClaim(plugin).fromDocument(cursor.next()));
        }

        return ImmutableList.copyOf(claims);
    }

    public static void saveDefinedClaims(MongoDB database, Collection<DefinedClaim> claims) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);

        if (collection == null) {
            Logger.error("Collection '" + DB_COLL + "' was not found and claims were not saved");
            return;
        }

        claims.forEach(claim -> {
            final Document existing = collection.find(Filters.eq("id", claim.getUniqueId())).first();

            if (existing != null) {
                collection.replaceOne(existing, claim.toDocument());
            } else {
                collection.insertOne(claim.toDocument());
            }
        });
    }

    public static void deleteDefinedClaims(MongoDB database, Collection<DefinedClaim> claims) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);

        if (collection == null) {
            Logger.error("Collection '" + DB_COLL + "' was not found and claims were not deleted");
            return;
        }

        claims.forEach(claim -> {
            final Document existing = collection.find(Filters.eq("id", claim.getUniqueId())).first();

            if (existing != null) {
                collection.deleteOne(existing);
            }
        });
    }

    public static void deleteDefinedClaim(MongoDB database, DefinedClaim claim) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);

        if (collection == null) {
            Logger.error("Collection '" + DB_COLL + "' was not found and claims were not deleted");
            return;
        }

        final Document existing = collection.find(Filters.eq("id", claim.getUniqueId())).first();

        if (existing != null) {
            collection.deleteOne(existing);
        }
    }
}