package com.example.sunoptic

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sunoptic.network.ForecastResponse
import com.example.sunoptic.network.WeatherApiService
import com.example.sunoptic.network.WeatherListItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var apiKey: String
    private val city = "Kyiv"

    // Елементи UI
    private lateinit var tvCity: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvWeatherCondition: TextView
    private lateinit var tvVisibility: TextView
    private lateinit var rvForecast: RecyclerView
    private lateinit var forecastAdapter: ForecastAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiKey = getApiKeyFromAssets()
        if (apiKey.isEmpty()) {
            Log.e("MainActivity", "API ключ не знайдено.")
            return
        }

        // Ініціалізація UI
        tvCity = findViewById(R.id.tvCity)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvWeatherCondition = findViewById(R.id.tvWeatherCondition)
        tvVisibility = findViewById(R.id.tvVisibility)
        rvForecast = findViewById(R.id.rvForecast)
        rvForecast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        fetchWeatherData()
    }

    private fun fetchWeatherData() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val weatherService = retrofit.create(WeatherApiService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = weatherService.getForecast(city, apiKey)

                // ✅ Оновлена логіка фільтрації
                val dailyForecasts = filterDailyForecasts(response.list)

                withContext(Dispatchers.Main) {
                    // Оновлюємо головний екран погодою на "сьогодні" (перший елемент списку)
                    if (dailyForecasts.isNotEmpty()) {
                        updateUI(dailyForecasts.first(), response.city.name)
                    }

                    // ✅ Передаємо лямбда-функцію в адаптер
                    forecastAdapter = ForecastAdapter(dailyForecasts) { item, position ->
                        // Це виконається при натисканні на елемент
                        showWeatherDetailsDialog(item, position)
                    }
                    rvForecast.adapter = forecastAdapter
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching weather data", e)
            }
        }
    }

    /**
     * ✅ Оновлена функція фільтрації, щоб починати з поточного дня
     */
    private fun filterDailyForecasts(fullList: List<WeatherListItem>): List<WeatherListItem> {
        val dailyItems = mutableListOf<WeatherListItem>()
        val uniqueDays = mutableSetOf<String>()

        // 1. Додаємо поточний прогноз як "Сьогодні"
        if (fullList.isNotEmpty()) {
            val today = fullList.first()
            dailyItems.add(today)
            uniqueDays.add(today.dt_txt.substringBefore(" ")) // Додаємо дату, напр. "2025-10-26"
        }

        // 2. Шукаємо прогнози на 12:00 для наступних днів
        val noonForecasts = fullList.filter {
            val date = it.dt_txt.substringBefore(" ")
            // Додаємо, якщо це 12:00 І ми ще не додали прогноз на цей день
            it.dt_txt.endsWith("12:00:00") && !uniqueDays.contains(date)
        }

        // Додаємо знайдені прогнози
        for (item in noonForecasts) {
            val date = item.dt_txt.substringBefore(" ")
            if (!uniqueDays.contains(date)) {
                dailyItems.add(item)
                uniqueDays.add(date)
            }
            // Обмежуємо 5-ма днями
            if (dailyItems.size >= 5) break
        }

        return dailyItems
    }

    /**
     * ✅ НОВА ФУНКЦІЯ: Показ спливаючого вікна з деталями
     */
    private fun showWeatherDetailsDialog(item: WeatherListItem, position: Int) {
        // Надуваємо макет
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_weather_details, null)

        // Знаходимо TextView у макеті
        val tvDetailDay = dialogView.findViewById<TextView>(R.id.tvDetailDay)
        val tvDetailDescription = dialogView.findViewById<TextView>(R.id.tvDetailDescription)
        val tvDetailTemp = dialogView.findViewById<TextView>(R.id.tvDetailTemp)
        val tvDetailHumidity = dialogView.findViewById<TextView>(R.id.tvDetailHumidity)
        val tvDetailWind = dialogView.findViewById<TextView>(R.id.tvDetailWind)
        val tvDetailVisibility = dialogView.findViewById<TextView>(R.id.tvDetailVisibility)

        // Встановлюємо дані
        tvDetailDay.text = if (position == 0) "Сьогодні" else formatDayOfWeek(item.dt_txt)
        tvDetailDescription.text = item.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "N/A"
        tvDetailTemp.text = "${item.main.temp.toInt()}°"
        tvDetailHumidity.text = "Вологість: ${item.main.humidity}%"
        tvDetailWind.text = "Вітер: ${item.wind.speed} м/с, ${degToCompass(item.wind.deg)}"
        tvDetailVisibility.text = "Видимість: ${item.visibility / 1000.0} км"

        // Створюємо та показуємо діалог
        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Закрити") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // --- Допоміжні функції ---

    private fun getApiKeyFromAssets(): String {
        // ... (ваш код для читання ключа залишається без змін)
        try {
            return assets.open("apiKey.json")
                .bufferedReader()
                .use { it.readText() }
                .let { JSONObject(it).getString("api_key") }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    private fun updateUI(todayWeather: WeatherListItem, cityName: String) {
        // ... (ваш код для оновлення головного UI залишається без змін)
        tvCity.text = cityName
        tvTemperature.text = "${todayWeather.main.temp.toInt()}°"
        tvWeatherCondition.text = todayWeather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: ""
        val visibilityInKm = todayWeather.visibility / 1000.0
        tvVisibility.text = "$visibilityInKm км"
    }

    /**
     * Допоміжна функція для форматування дня тижня (для діалогу)
     */
    private fun formatDayOfWeek(dateString: String): String {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = parser.parse(dateString) ?: Date()
            val formatter = SimpleDateFormat("E", Locale("uk", "UA"))
            return formatter.format(date).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            e.printStackTrace()
            return "N/A"
        }
    }

    /**
     * Допоміжна функція для конвертації градусів у напрямок вітру
     */
    private fun degToCompass(degrees: Int): String {
        val directions = listOf("Пн", "Пн-Сх", "Сх", "Пд-Сх", "Пд", "Пд-Зх", "Зх", "Пн-Зх")
        val index = (degrees % 360) / 45
        return directions[index]
    }
}
