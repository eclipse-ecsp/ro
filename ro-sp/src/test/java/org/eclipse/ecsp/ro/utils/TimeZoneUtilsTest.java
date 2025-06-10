package org.eclipse.ecsp.ro.utils;

import org.eclipse.ecsp.ro.queue.DeviceMessageFailureQueueHandler;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link TimeZoneUtils} class.
 */

public class TimeZoneUtilsTest {

    private static final double NYlat = 40.7128;

    private static final double NYlongi = -74.0060;

    @Test
    public void testGetZoneIdByLocation_validCoordinates() {
        // Sample coordinates for New York City
        double latitude = NYlat;
        double longitude = NYlongi;

        ZoneId zoneId = TimeZoneUtils.getZoneIdByLocation(latitude, longitude);

        assertNotNull(zoneId, "ZoneId should not be null for valid coordinates");
        System.out.println("Zone ID for NYC: " + zoneId);
    }

    private static final double INVlat = -90.0;

    private static final double INVlongi = -180.0;

    @Test
    public void testGetZoneIdByLocation_invalidCoordinates() {
        // Invalid coordinates in the middle of the ocean
        double invlatitude = INVlat;
        double invlongitude = INVlongi;

        ZoneId zoneId = TimeZoneUtils.getZoneIdByLocation(invlatitude, invlongitude);

        assertNull(zoneId, "ZoneId should be null for invalid coordinates");
    }

    @Test
    public void testGetUTCTimestamp_validInput() {
        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
        String timeStr = "2025/06/08 10:30:00";
        String pattern = "yyyy/MM/dd HH:mm:ss";

        long utcTimestamp = TimeZoneUtils.getUTCTimestamp(zoneId, timeStr, pattern);

        assertTrue(utcTimestamp > 0, "UTC timestamp should be greater than zero");
        System.out.println("UTC Timestamp: " + utcTimestamp);
    }

    @Test
    public void testGetCurrentUTCTimestamp() {
        long currentUtc = TimeZoneUtils.getCurrentUTCTimestamp();

        assertTrue(currentUtc > 0, "Current UTC timestamp should be greater than zero");
        System.out.println("Current UTC Timestamp: " + currentUtc);
    }
}