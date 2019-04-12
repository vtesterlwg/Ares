package com.playares.commons.base.connect;

public interface Connectable {
    /**
     * Open a new connection
     */
    void openConnection();

    /**
     * Close the existing connection
     */
    void closeConnection();
}
