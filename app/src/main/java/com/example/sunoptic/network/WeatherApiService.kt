package com.example.sunoptic.network

// Вміст файлу WeatherApiService.kt

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/weather") // Використовуємо повний шлях для запиту
    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ua"
    ): WeatherResponse
}