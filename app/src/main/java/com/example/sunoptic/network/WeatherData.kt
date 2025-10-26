package com.example.sunoptic.network

// Головний об'єкт відповіді
data class ForecastResponse(
    val list: List<WeatherListItem>, // Список прогнозів
    val city: City
)

// Елемент списку прогнозу
data class WeatherListItem(
    // ... (решта ваших полів: dt, main, weather, etc.) ...
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val visibility: Int,
    val dt_txt: String,
    val wind: Wind
)

// ... (Main, Weather, Wind залишаються без змін) ...
data class Main(
    val temp: Double,
    val humidity: Int,
    val pressure: Int
)

data class Weather(
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double,
    val deg: Int
)


// ✅ ДОДАНО: Новий клас для координат
data class Coord(
    val lat: Double,
    val lon: Double
)

// ✅ ОНОВЛЕНО: Клас City тепер містить Coord
data class City(
    val name: String,
    val coord: Coord // Додаємо це поле
)