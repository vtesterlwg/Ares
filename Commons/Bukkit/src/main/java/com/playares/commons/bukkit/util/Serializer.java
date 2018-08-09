package com.playares.commons.bukkit.util;

import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.util.Base64;

public final class Serializer {
    public static String encode(byte[] buf) {
        return Base64.getEncoder().encodeToString(buf);
    }

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
}