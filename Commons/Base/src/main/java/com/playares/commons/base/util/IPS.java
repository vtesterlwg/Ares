package com.playares.commons.base.util;

public final class IPS {
    public static int toInt(String address) {
        final String formatted = address
                .replace(".", "")
                .replace("/", "")
                .replace(":", "");

        try {
            return Integer.parseInt(formatted);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private IPS() {
        throw new UnsupportedOperationException("This class can not be instantiated");
    }
}
