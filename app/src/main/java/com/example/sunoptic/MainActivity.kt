package com.example.sunoptic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sunoptic.network.ForecastResponse // ✅ Використовує ForecastResponse
import com.example.sunoptic.network.WeatherApiService
import com.example.sunoptic.network.WeatherListItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var apiKey: String
    private val city = "Kyiv"

    // Оголошуємо елементи UI
    private lateinit var tvCity: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvWeatherCondition: TextView
    private lateinit var tvVisibility: TextView

    // Додаємо RecyclerView та Адаптер
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

        // Ініціалізуємо елементи UI
        tvCity = findViewById(R.id.tvCity)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvWeatherCondition = findViewById(R.id.tvWeatherCondition)
        tvVisibility = findViewById(R.id.tvVisibility)

        // Ініціалізуємо RecyclerView
        rvForecast = findViewById(R.id.rvForecast)
        rvForecast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Налаштування Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherService = retrofit.create(WeatherApiService::class.java)

        // Запускаємо корутину
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // ✅ Викликаємо новий метод getForecast
                val response = weatherService.getForecast(city, apiKey)

                // Фільтруємо список, щоб отримати 5 днів (один прогноз на день)
                val dailyForecasts = filterDailyForecasts(response.list)

                withContext(Dispatchers.Main) {
                    // Оновлюємо UI першим елементом списку (сьогодні)
                    updateUI(response.list.first(), response.city.name) // ✅ Використовує response.city.name

                    // Налаштовуємо адаптер для RecyclerView
                    forecastAdapter = ForecastAdapter(dailyForecasts)
                    rvForecast.adapter = forecastAdapter
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching weather data", e)
            }
        }
    }

    /**
     * Допоміжна функція для фільтрації списку
     */
    private fun filterDailyForecasts(fullList: List<WeatherListItem>): List<WeatherListItem> {
        return fullList.filter {
            it.dt_txt.endsWith("12:00:00") // Обираємо прогноз на 12 годину дня
        }
    }

    /**
     * Функція для читання ключа з файлу assets/apiKey.json
     */
    private fun getApiKeyFromAssets(): String {
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

    // Оновлюємо функцію, щоб вона приймала WeatherListItem
    private fun updateUI(todayWeather: WeatherListItem, cityName: String) {
        tvCity.text = cityName
        tvTemperature.text = "${todayWeather.main.temp.toInt()}°" // ✅ Використовує todayWeather.main.temp
        tvWeatherCondition.text = todayWeather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "" // ✅ Використовує todayWeather.weather
        val visibilityInKm = todayWeather.visibility / 1000.0 // ✅ Використовує todayWeather.visibility
        tvVisibility.text = "$visibilityInKm км"
    }
}