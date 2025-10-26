package com.example.sunoptic.network

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    // Змінюємо кінцеву точку API на "forecast"
    @GET("data/2.5/forecast")
    suspend fun getForecast( // Змінюємо назву методу та тип відповіді
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ua"
    ): ForecastResponse // Повертаємо новий клас ForecastResponse
}