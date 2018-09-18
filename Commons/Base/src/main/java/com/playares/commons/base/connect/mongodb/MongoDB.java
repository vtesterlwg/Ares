package com.playares.commons.base.connect.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.playares.commons.base.connect.Connectable;
import lombok.Getter;
import org.bson.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MongoDB implements Connectable {
    @Nonnull
    private final String uri;

    @Nullable @Getter
    public MongoClient client;

    /**
     * Creates a new MongoDB instance
     * @param uri Connect URI
     */
    public MongoDB(@Nonnull String uri) {
        this.uri = uri;
        this.client = null;
    }

    @Override
    public void openConnection() {
        this.client = MongoClients.create(uri);
    }

    @Override
    public void closeConnection() {
        if (client == null) {
            return;
        }

        client.close();
    }

    /**
     * Returns a Mongo Database matching the given name, creates a new one if not found
     * @param name Database Name
     * @return MongoDatabase instance matching provided name
     */
    @Nullable
    public MongoDatabase getDatabase(@Nonnull String name) {
        if (client == null) {
            return null;
        }

        return client.getDatabase(name);
    }

    /**
     * Returns a Mongo Collection matching the given name in the given database, creates a new one if not found
     * @param database Database Name
     * @param collection Collection Name
     * @return MongoCollection instance matching provided name in the given database
     */
    @Nullable
    public MongoCollection<Document> getCollection(@Nonnull String database, String collection) {
        final MongoDatabase db = getDatabase(database);

        if (client == null || db == null) {
            return null;
        }

        return db.getCollection(collection);
    }
}