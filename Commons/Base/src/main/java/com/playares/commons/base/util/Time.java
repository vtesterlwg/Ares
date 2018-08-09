package com.playares.commons.base.util;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@ParametersAreNonnullByDefault
public final class Time {
    public static long now() {
        return Instant.now().toEpochMilli();
    }

    @Nonnull
    public static String convertToDate(String pattern, Date date) {
        return new SimpleDateFormat(pattern).format(date);
    }

    @Nonnull
    public static String convertToDate(Date date) {
        return convertToDate("MMM d, hh:mm:ss a", date);
    }

    @Nonnull
    public static String convertToShortDate(Date date) {
        return convertToDate("hh:mm:ss a", date);
    }

    @Nonnull
    public static String convertToDecimal(long duration) {
        return String.format("%.1f", Math.abs(duration / 1000.0F));
    }

    @Nonnull
    public static String convertToHHMMSS(long duration) {
        final int hours = (int) TimeUnit.MILLISECONDS.toHours(duration) % 24;
        final int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
        final int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(duration) % 60;

        return (hours > 0) ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    @Nonnull
    public static String convertToRemaining(long duration) {
        final long days = TimeUnit.MILLISECONDS.toDays(duration);
        final long hours = TimeUnit.MILLISECONDS.toHours(duration) - (days * 24);
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - (TimeUnit.MILLISECONDS.toHours(duration) * 60);
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - (TimeUnit.MILLISECONDS.toMinutes(duration) * 60);

        if (days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            return "Now";
        }

        final StringBuilder response = new StringBuilder();

        if (days > 0) {
            response.append(days).append(" days ");
        }

        if (hours > 0) {
            response.append(hours).append(" hours ");
        }

        if (minutes > 0) {
            response.append(minutes).append(" minutes ");
        }

        if (seconds > 0) {
            response.append(seconds).append(" seconds");
        }

        return response.toString();
    }

    @Nonnull
    public static String convertToElapsed(long duration) {
        final long days = TimeUnit.MILLISECONDS.toDays(duration);
        final long hours = TimeUnit.MILLISECONDS.toHours(duration) - (days * 24);
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - (TimeUnit.MILLISECONDS.toHours(duration) * 60);
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - (TimeUnit.MILLISECONDS.toMinutes(duration) * 60);

        if (days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            return "Just Now";
        }

        final StringBuilder response = new StringBuilder();

        if (days > 0) {
            response.append(days).append(" days ");
        }

        if (hours > 0) {
            response.append(hours).append(" hours ");
        }

        if (minutes > 0) {
            response.append(minutes).append(" minutes ");
        }

        if (seconds > 0) {
            response.append(seconds).append(" seconds ");
        }

        response.append("ago");

        return response.toString();
    }

    @Nonnull
    public static String convertToInaccurateElapsed(long duration) {
        final long days = TimeUnit.MILLISECONDS.toDays(duration);
        final long hours = TimeUnit.MILLISECONDS.toHours(duration) - (days * 24);
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - (TimeUnit.MILLISECONDS.toHours(duration) * 60);
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - (TimeUnit.MILLISECONDS.toMinutes(duration) * 60);

        if (days > 0) {
            return days + " days";
        }

        if (hours > 0) {
            return hours + " hours";
        }

        if (minutes > 0) {
            return minutes + " minutes";
        }

        if (seconds > 0) {
            return seconds + " seconds";
        }

        return "Just Now";
    }

    public static long parseTime(String input) throws NumberFormatException {
        if (input.length() < 2) {
            return 0;
        }

        StringBuilder builder = new StringBuilder();
        final String[] split = input.split("");
        int result = 0;

        for (String current : split) {
            if (current.matches("[0-9]")) {
                builder.append(current);
                continue;
            }

            if (current.equalsIgnoreCase("d")) {
                final int number = Integer.parseInt(builder.toString());
                result += (number * 86400);
                builder = new StringBuilder();
                continue;
            }

            if (current.equalsIgnoreCase("h")) {
                final int number = Integer.parseInt(builder.toString());
                result += (number * 3600);
                builder = new StringBuilder();
                continue;
            }

            if (current.equalsIgnoreCase("m")) {
                final int number = Integer.parseInt(builder.toString());
                result += (number * 60);
                builder = new StringBuilder();
                continue;
            }

            if (current.equalsIgnoreCase("s")) {
                final int number = Integer.parseInt(builder.toString());
                result += number;
                builder = new StringBuilder();
            }
        }

        return (result * 1000);
    }

    private Time() {
        throw new UnsupportedOperationException("This class can not be instantiated");
    }
}
