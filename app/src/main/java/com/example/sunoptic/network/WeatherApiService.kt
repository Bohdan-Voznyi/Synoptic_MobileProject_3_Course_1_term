package com.example.sunoptic.network

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    // Прогноз на 5 днів
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ua"
    ): ForecastResponse

    // ✅ НОВИЙ МЕТОД: Якість повітря
    @GET("data/2.5/air_pollution")
    suspend fun getAirPollution(
        @Query("lat") lat: Double, // Використовуємо широту
        @Query("lon") lon: Double, // та довготу
        @Query("appid") apiKey: String
    ): AirPollutionResponse // Повертаємо новий клас відповіді
}