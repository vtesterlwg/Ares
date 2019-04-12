package com.playares.commons.bukkit.serialize;

import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.util.Base64;

public final class B64 {
    /**
     * Encode bytes to base64
     * @param buf Bytes
     * @return Base64
     */
    public static String encode(byte[] buf) {
        return Base64.getEncoder().encodeToString(buf);
    }

    /**
     * Decode bytes from base64
     * @param src Base64
     * @return Bytes
     */
    public static byte[] decode(String src) {
        try {
            return Base64.getDecoder().decode(src);
        } catch (IllegalArgumentException e) {
            try {
                return Base64Coder.decodeLines(src);
            } catch (Exception ignored) {
                throw e;
            }
        }
    }

    private B64() {
        throw new UnsupportedOperationException("This class can not be instantiated");
    }
}
