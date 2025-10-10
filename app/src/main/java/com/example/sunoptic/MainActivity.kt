package com.example.sunoptic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.sunoptic.network.WeatherApiService
import com.example.sunoptic.network.WeatherResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    // Змінюємо змінну, щоб завантажити ключ пізніше
    private lateinit var apiKey: String
    private val city = "Kyiv"

    // Оголошуємо елементи UI як змінні класу
    private lateinit var tvCity: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvWeatherCondition: TextView
    private lateinit var tvVisibility: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ Крок 1: Отримуємо ключ з JSON-файлу
        apiKey = getApiKeyFromAssets()

        // Перевірка, чи вдалося завантажити ключ
        if (apiKey.isEmpty()) {
            Log.e("MainActivity", "API ключ не знайдено або сталася помилка читання.")
            // Можна показати помилку користувачу
            return // Зупиняємо виконання, якщо ключа немає
        }

        // Ініціалізуємо елементи UI
        tvCity = findViewById(R.id.tvCity)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvWeatherCondition = findViewById(R.id.tvWeatherCondition)
        tvVisibility = findViewById(R.id.tvVisibility)

        // Налаштування Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherService = retrofit.create(WeatherApiService::class.java)

        // Запускаємо корутину для мережевого запиту
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = weatherService.getCurrentWeather(city, apiKey)
                withContext(Dispatchers.Main) {
                    updateUI(response)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching weather data", e)
            }
        }
    }

    /**
     * Нова функція для читання ключа з файлу assets/apiKey.json
     */
    private fun getApiKeyFromAssets(): String {
        try {
            // Відкриваємо файл з папки assets
            val inputStream = assets.open("apiKey.json")
            // Читаємо файл у один рядок
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            // Парсимо JSON та отримуємо значення за ключем "api_key"
            val jsonObject = JSONObject(jsonString)
            return jsonObject.getString("api_key")
        } catch (e: Exception) {
            e.printStackTrace()
            return "" // Повертаємо порожній рядок у разі помилки
        }
    }

    private fun updateUI(weatherData: WeatherResponse) {
        tvCity.text = weatherData.name
        tvTemperature.text = "${weatherData.main.temp.toInt()}°"
        tvWeatherCondition.text = weatherData.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: ""
        val visibilityInKm = weatherData.visibility / 1000.0
        tvVisibility.text = "$visibilityInKm км"
    }
}