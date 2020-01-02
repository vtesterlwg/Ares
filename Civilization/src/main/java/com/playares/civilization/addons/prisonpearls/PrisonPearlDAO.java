package com.playares.civilization.addons.prisonpearls;

import com.google.common.collect.Lists;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.playares.civilization.Civilizations;
import com.playares.civilization.addons.prisonpearls.data.PrisonPearl;
import com.playares.commons.bukkit.logger.Logger;
import lombok.Getter;
import org.bson.Document;

import java.util.Collection;
import java.util.List;

public final class PrisonPearlDAO {
    @Getter public static final String DB_NAME = "civilizations";
    @Getter public static final String DB_COLL = "pearls";

    public static Collection<PrisonPearl> get(Civilizations plugin) {
        final List<PrisonPearl> result = Lists.newArrayList();
        final MongoCollection<Document> collection = plugin.getMongo().getCollection(DB_NAME, DB_COLL);

        if (collection != null) {
            for (Document document : collection.find()) {
                result.add(new PrisonPearl().fromDocument(document));
            }
        }

        return result;
    }

    public static void save(Civilizations plugin, PrisonPearl pearl) {
        final MongoCollection<Document> collection = plugin.getMongo().getCollection(DB_NAME, DB_COLL);
        final FindIterable<Document> iter;
        final Document existing;

        if (collection == null) {
            Logger.error("Failed to save Prison Pearl for " + pearl.getPlayerUsername() + ", MongoDB Collection could not be found");
            return;
        }

        iter = collection.find(Filters.eq("id", pearl.getUniqueId()));
        existing = iter.first();

        if (existing == null) {
            collection.insertOne(pearl.toDocument());
        } else {
            collection.replaceOne(existing, pearl.toDocument());
        }
    }

    public static void save(Civilizations plugin, Collection<PrisonPearl> pearls) {
        final MongoCollection<Document> collection = plugin.getMongo().getCollection(DB_NAME, DB_COLL);

        if (collection == null) {
            Logger.error("Failed to save collection of " + pearls.size() + " Prison Pearls, MongoDB Collection could not be found");
            return;
        }

        int inserted = 0;
        int replaced = 0;

        for (PrisonPearl pearl : pearls) {
            final FindIterable<Document> iter = collection.find(Filters.eq("id", pearl.getUniqueId()));
            final Document existing = iter.first();

            if (existing == null) {
                collection.insertOne(pearl.toDocument());
                inserted += 1;
            } else {
                collection.replaceOne(existing, pearl.toDocument());
                replaced += 1;
            }
        }

        Logger.print("Saved " + pearls.size() + " Prison Pearls, Inserted: " + inserted + ", Replaced: " + replaced);
    }
}