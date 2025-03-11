package com.weatherapp.util;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WeatherUtilServiceTest {

    private static final String ERROR_MESSAGE = "Error: Please enter a valid " +
    "city, state, province, or zip code.";

    @BeforeEach
    void clearCache() throws Exception {
        // Clear cache before each test using reflection
        Field cacheField = WeatherUtilService.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        ((ConcurrentHashMap<?, ?>) cacheField.get(null)).clear();
    }

    @Test
    void testGetWeatherReturnsDataForZip() {
        String weatherData = WeatherUtilService.retrieveWeatherByLocation("85213");
        assertNotNull(weatherData);
        assertFalse(weatherData.contains("Error"), "Expected valid weather data for zip, " +
        "but got an error.");
    }

    @Test
    void testGetWeatherReturnsDataForCity() {
        String weatherData = WeatherUtilService.retrieveWeatherByLocation("Phoenix");
        assertNotNull(weatherData);
        assertFalse(weatherData.contains("Error"), "Expected valid weather data for city, " +
        "but got an error.");
    }

    @Test
    void testGetWeatherReturnsDataForStateOrProvince() {
        String weatherData1 = WeatherUtilService.retrieveWeatherByLocation("Arizona");
        String weatherData2 = WeatherUtilService.retrieveWeatherByLocation("Ontario");
        assertNotNull(weatherData1);
        assertNotNull(weatherData2);
        assertFalse(weatherData1.contains("Error"), "Expected valid weather data for state, " + 
        "but got an error.");
        assertFalse(weatherData2.contains("Error"), "Expected valid weather data for province, " + 
        "but got an error.");
    }

    @Test
    void testCacheStoresData() {
        String weatherData1 = WeatherUtilService.retrieveWeatherByLocation("New York");
        String weatherData2 = WeatherUtilService.retrieveWeatherByLocation("New York");

        assertNotNull(weatherData1);
        assertEquals(weatherData1, weatherData2.substring(8), "Second request should return cached data.");
        assertNotNull(weatherData2);
        assertTrue(weatherData2.contains("(Cache)"), "Cached data should indicate it's from cache.");
    }

    @Test
    void testInvalidLocation() {
        String weatherData = WeatherUtilService.retrieveWeatherByLocation("Phoenix, AZ");
        assertNotNull(weatherData);
        assertEquals(weatherData, ERROR_MESSAGE, "Expected invalid response");
    }

    @Test
    void testCacheExpires() throws Exception {
        String location = "Paris";
        
        // Store weather data in the cache
        WeatherUtilService.retrieveWeatherByLocation(location);

        // Access the cache using reflection to modify timestamp
        Field cacheField = WeatherUtilService.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        ConcurrentHashMap<String, WeatherData> cache = (ConcurrentHashMap<String, WeatherData>) cacheField.get(null);

        // Simulate cache expiration by setting the timestamp to 31 minutes ago
        cache.get(location).setTimestamp(System.currentTimeMillis() - (31 * 60 * 1000));

        // Manually trigger cache cleanup
        Method cleanupMethod = WeatherUtilService.class.getDeclaredMethod("removeExpiredCacheEntries");
        cleanupMethod.setAccessible(true);
        cleanupMethod.invoke(null);

        // Ensure the expired entry is removed
        assertFalse(cache.containsKey(location), "Cache should not contain expired data.");

        // Fetching the weather again should now result in a fresh API call (not cached)
        String newData = WeatherUtilService.retrieveWeatherByLocation(location);
        assertFalse(newData.contains("(Cache)"), "New request should not be from cache after expiration.");
    }
}