package com.weatherapp.util;

/**
 * Pojo class specifying data retrieved from API
 */
public class WeatherData {
    private String forecast;
    private long timestamp;

    public WeatherData(String forecast, long timestamp) {
        this.forecast = forecast;
        this.timestamp = timestamp;
    }

    public void setForecast(String forecast) {
        this.forecast = forecast;
    }

    public String getForecast() {
        return this.forecast;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}