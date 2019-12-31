package com.playares.civilization.networks;

import com.google.common.collect.Lists;
import com.playares.civilization.Civilizations;
import com.playares.commons.base.connect.mongodb.MongoDB;

import java.util.Collection;
import java.util.List;

public final class NetworkDAO {
    public static final String DB_NAME = "civilizations";
    public static final String DB_COLL = "networks";

    public static Collection<Network> get(Civilizations plugin, MongoDB database) {
        final List<Network> result = Lists.newArrayList();

        return result;
    }

    public static void save(MongoDB database, Collection<Network> networks) {

    }

    public static void save(MongoDB database, Network network) {

    }
}
