package com.playares.commons.base.util;

import java.util.regex.Pattern;

public final class IPS {
    /**
     * Converts an IP-Address to an Integer
     * @param address InetAddress as String
     * @return IP-Address as Integer
     */
    public static int toInt(String address) {
        int result = 0;

        for(String part : address.split(Pattern.quote("."))) {
            result = result << 8;
            result |= Integer.parseInt(part);
        }

        return result;
    }

    private IPS() {
        throw new UnsupportedOperationException("This class can not be instantiated");
    }
}
