package com.playares.commons.base.util;

public final class IPS {
    /**
     * Converts an IP-Address to an Integer
     * @param address InetAddress as String
     * @return IP-Address as Integer
     */
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
