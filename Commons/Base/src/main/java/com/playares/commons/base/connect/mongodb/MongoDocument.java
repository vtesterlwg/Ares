package com.playares.commons.base.connect.mongodb;

import org.bson.Document;

public interface MongoDocument<T> {
    T fromDocument(Document document);

    Document toDocument();
}
