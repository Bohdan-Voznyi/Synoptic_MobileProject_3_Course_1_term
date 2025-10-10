package com.example.sunoptic.network

// Вміст файлу WeatherData.kt

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val visibility: Int,
    val name: String
)

data class Main(
    val temp: Double,
    val humidity: Int,
    val pressure: Int
)

data class Weather(
    val description: String,
    val icon: String
)