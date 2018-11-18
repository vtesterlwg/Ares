package com.riotmc.services.punishments.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.riotmc.commons.base.connect.mongodb.MongoDB;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

public final class PunishmentDAO {
    private static final String DB_NAME = "ares";
    private static final String DB_COLL = "punishments";

    public static ImmutableList<Punishment> getPunishments(MongoDB database, Bson playerFilter, Bson punishmentFilter) {
        final List<Punishment> result = Lists.newArrayList();
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final MongoCursor<Document> cursor;

        if (collection == null) {
            return ImmutableList.of();
        }

        cursor = collection.find(Filters.and(playerFilter, punishmentFilter)).iterator();

        while (cursor.hasNext()) {
            final Punishment punishment = new Punishment().fromDocument(cursor.next());
            result.add(punishment);
        }

        return ImmutableList.copyOf(result);
    }

    public static void savePunishment(MongoDB database, Punishment punishment) {
        final MongoCollection<Document> collection = database.getCollection(DB_NAME, DB_COLL);
        final FindIterable<Document> iter;
        final Document document;

        if (collection == null) {
            return;
        }

        iter = collection.find(Filters.eq("id", punishment.getUniqueId()));
        document = iter.first();

        if (document != null)
            collection.replaceOne(document, punishment.toDocument());
        else
            collection.insertOne(punishment.toDocument());
    }
}