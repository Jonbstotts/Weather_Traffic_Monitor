package com.wtm.model;

import java.time.Instant;
import java.util.List;

/** Immutable weather data returned by the weather provider. */
public record WeatherSnapshot(
        String locationName,
        double temperatureF,
        double apparentTemperatureF,
        double highF,
        double lowF,
        double precipitationProbability,
        double windMph,
        double gustMph,
        int weatherCode,
        String condition,
        Instant updatedAt,
        List<HourlyPoint> hourly) {

    public record HourlyPoint(String time, double temperatureF, double precipitationProbability, int weatherCode) {}
}
