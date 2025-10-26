package com.example.sunoptic.network

// Нові data-класи для відповіді /forecast

// Головний об'єкт відповіді
data class ForecastResponse(
    val list: List<WeatherListItem>, // Список прогнозів (зазвичай 40 елементів, по 8 на день)
    val city: City
)

// Елемент списку прогнозу
data class WeatherListItem(
    val dt: Long, // Час прогнозу (Unix timestamp)
    val main: Main,
    val weather: List<Weather>,
    val visibility: Int,
    val dt_txt: String // Текстова дата, напр. "2025-10-26 18:00:00"
)

// Залишаємо ці класи, вони використовуються всередині WeatherListItem
data class Main(
    val temp: Double,
    val humidity: Int,
    val pressure: Int
)

data class Weather(
    val description: String,
    val icon: String
)

// Додаємо клас для назви міста
data class City(
    val name: String
)