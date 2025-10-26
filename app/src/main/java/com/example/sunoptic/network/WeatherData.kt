package com.example.sunoptic.network

// Головний об'єкт відповіді
data class ForecastResponse(
    val list: List<WeatherListItem>, // Список прогнозів
    val city: City
)

// Елемент списку прогнозу
data class WeatherListItem(
    val dt: Long, // Час прогнозу (Unix timestamp)
    val main: Main,
    val weather: List<Weather>,
    val visibility: Int,
    val dt_txt: String, // Текстова дата
    val wind: Wind // ✅ ДОДАНО: Інформація про вітер
)

// Залишаємо ці класи
data class Main(
    val temp: Double,
    val humidity: Int,
    val pressure: Int
)

data class Weather(
    val description: String,
    val icon: String
)

// ✅ ДОДАНО: Новий клас для вітру
data class Wind(
    val speed: Double, // Швидкість вітру, м/с
    val deg: Int       // Напрямок вітру, градуси
)

// Клас для назви міста
data class City(
    val name: String
)
