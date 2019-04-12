package com.playares.commons.base.util;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@ParametersAreNonnullByDefault
public final class Time {
    /**
     * @return Epoch time in milliseconds
     */
    public static long now() {
        return Instant.now().toEpochMilli();
    }

    /**
     * Converts supplied date to the supplied pattern
     * @param pattern Pattern
     * @param date Date
     * @return Formatted date
     */
    @Nonnull
    public static String convertToDate(String pattern, Date date) {
        return new SimpleDateFormat(pattern).format(date);
    }

    /**
     * Converts the supplied date to standard date format
     * @param date Date
     * @return Formatted date
     */
    @Nonnull
    public static String convertToDate(Date date) {
        return convertToDate("MMM d, hh:mm:ss a", date);
    }

    /**
     * Converts the supplied date to the shorthand format
     * @param date Date
     * @return Formatted date
     */
    @Nonnull
    public static String convertToShortDate(Date date) {
        return convertToDate("hh:mm:ss a", date);
    }

    /**
     * Converts milliseconds to decimal
     * @param duration Milliseconds
     * @return Converted milliseconds to decimal
     */
    @Nonnull
    public static String convertToDecimal(long duration) {
        return String.format("%.1f", Math.abs(duration / 1000.0F));
    }

    /**
     * Converts milliseconds to HH:MM:SS format
     * @param duration Milliseconds
     * @return Converted milliseconds to hh:mm:ss
     */
    @Nonnull
    public static String convertToHHMMSS(long duration) {
        final int hours = (int) TimeUnit.MILLISECONDS.toHours(duration) % 24;
        final int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
        final int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(duration) % 60;

        return (hours > 0) ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Converts milliseconds to a remainder time display
     * @param duration Milliseconds
     * @return Converted milliseconds to remaining
     */
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

    /**
     * Convert milliseconds to an elapsed time display
     * @param duration Milliseconds
     * @return Converted milliseconds to elapsed
     */
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

    /**
     * Converts milliseconds to an inaccurate elapsed time
     * @param duration Milliseconds
     * @return Converted milliseconds to elapsed
     */
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

    /**
     * Attempts to parse a string for time format
     *
     * Examples:
     * 30s - 30 seconds
     * 1h30m25s - 1 hour, 30 minutes, 25 seconds
     * 1d3h45m10s - 1 day, 3 hours, 45 minutes, 10 seconds
     *
     * @param input String input
     * @return Parsed time in milliseconds
     * @throws NumberFormatException - If format does not follow correct format
     */
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

    /**
     * Loops through days, hours and minutes to find next closest time matching given day, hour and minute
     * @param day Day of the week
     * @param hour Hour of the day
     * @param minute Minute of the hour
     * @return Returns the time until a specific day, hour and minute combination can be matched again
     */
    public static long getTimeUntil(int day, int hour, int minute) {
        final Calendar calendar = Calendar.getInstance();
        final int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        final int currentMin = calendar.get(Calendar.MINUTE);

        int dayCycles = 0;
        int hourCycles = 0;
        int minCycles = 0;
        long ms = 0L;

        for (int i = currentDay; i != day; i++) {
            if (i >= 7) {
                i = 0;
            }

            dayCycles++;
        }

        for (int i = currentHour; i != hour; i++) {
            if (i >= 24) {
                i = 0;
            }

            hourCycles++;
        }

        for (int i = currentMin; i != minute; i++) {
            if (i >= 60) {
                i = 0;
            }

            minCycles++;
        }

        if (dayCycles > 0) {
            ms += ((3600 * 24) * dayCycles) * 1000L;
        }

        if (hourCycles > 0) {
            ms += (3600 * hourCycles) * 1000L;
        }

        if (minCycles > 0) {
            ms += (60 * minCycles) * 1000L;
        }

        return ms;
    }

    private Time() {
        throw new UnsupportedOperationException("This class can not be instantiated");
    }
}