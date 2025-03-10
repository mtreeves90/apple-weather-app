package com.weatherapp.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

/**
 * Retrieves and parses weather forecast data
 */
public final class WeatherUtilService {
	private static final String API_KEY = "2f43f62b1189f67ce631a9e5b5152179";
    private static final String WEATHER_API = "https://api.openweathermap.org/data/2.5/weather?q=";
    private static final String EXCEPTION_CONSTRUCTOR_MESSAGE = "WeatherUtilService is a utility class and " +
    "cannot be instantiated.";
    private static final String ERROR_MESSAGE = "Error: Please enter a valid " +
    "city, state, province, or zip code.";
    private static final ConcurrentHashMap<String, WeatherData> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TIMEOUT = 30 * 60 * 1000; // 30 minutes in milliseconds
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    static {
        // Schedule cache cleanup every 30 minutes
        scheduler.scheduleAtFixedRate(WeatherUtilService::removeExpiredCacheEntries, 30, 30, TimeUnit.MINUTES);
    }

    private WeatherUtilService () {
        throw new UnsupportedOperationException(EXCEPTION_CONSTRUCTOR_MESSAGE);
    }

    /**
     * Retrieves weather forecast including temparature, minimum temperature, maximum temperature,
     * and general description
     * @param location specifies location of forecast via city, state, province, or zip code
     * @return parsed weather forecast data for the location as a String
     */
    public static String retrieveWeatherByLocation(String location) {
        if (!isValidLocation(location)) {
            return ERROR_MESSAGE;
        }

        long currentTimeMill = System.currentTimeMillis();

        //Check cache before API
        if (cache.containsKey(location)) {
            WeatherData weatherData = cache.get(location);
            // Update timestamp since it's being accessed
            weatherData.setTimestamp(currentTimeMill);
            cache.put(location, weatherData); // Updated entry stored in the cache
            return "(Cache)\n" + weatherData.getForecast();
        }

        //Fetch new data from API
        String response = fetchWeatherFromAPI(location);
        if (response != null) {
            cache.put(location, new WeatherData(response, currentTimeMill));
        }

        return response != null ? response : "Failed to retrieve weather data.";
    }

    /**
     * Refers to API to retrieve weather data
     * @param location specifies location of forecast via city, state, province, or zip code
     * @return parsed weather forecast data for the location as a String
     */
    private static String fetchWeatherFromAPI(String location) {
        try {
            String urlString = WEATHER_API + location + "&appid=" + API_KEY + "&units=metric";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return "Error: Unable to fetch weather data";
            }

            Scanner scanner = new Scanner(url.openStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            return parseWeatherData(response.toString());
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Parses JSON data of weather returned from API
     * @param jsonResponse is JSON data to be parsed
     * @return data as a readable String
     */
    private static String parseWeatherData(String jsonResponse) {
    	DecimalFormat format = new DecimalFormat("#.##");
        JSONObject jsonObject = new JSONObject(jsonResponse);
        double temperature = jsonObject.getJSONObject("main").getDouble("temp");
        temperature = Double.parseDouble(format.format(temperature));
        double minTemperature = jsonObject.getJSONObject("main").getDouble("temp_min");
        minTemperature = Double.parseDouble(format.format(minTemperature));
        double maxTemperature = jsonObject.getJSONObject("main").getDouble("temp_max");
        maxTemperature = Double.parseDouble(format.format(maxTemperature));
        String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");

        return "Temperature: " + temperature + "°C \n(Min: " + minTemperature + "°C, Max: " + maxTemperature + "°C) \n" + description;
    }

    /**
     * Resets the cache to only include weather data inserted over the last 30 minutes.
     * Runs every 30 minutes
     */
    private static void removeExpiredCacheEntries() {
        long currentTime = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> currentTime - entry.getValue().getTimestamp() >= CACHE_TIMEOUT);
    }

    /**
     * Checks and makes sure location is a city, state, province, or zip code
     * @param location specifies location being checked to be valid
     * @return boolean specifying whether or not the location is valid
     */
    private static boolean isValidLocation(String location) {
        // Allow only city, state, province, or zip code (not multiple values)
        return location.matches("^[a-zA-z\s]+$") || location.matches("^\\d{5}$");
    }
}