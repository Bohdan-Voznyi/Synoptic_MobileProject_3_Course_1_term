package com.example.sunoptic

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sunoptic.network.AirPollutionResponse
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
import com.example.sunoptic.R // ✅ ВАЖЛИВО: Цей імпорт вирішує помилки з ID

class MainActivity : AppCompatActivity() {

    private lateinit var apiKey: String
    private var city = "Kyiv" // Змінено на var для оновлення

    // --- Елементи UI ---
    // Головний екран
    private lateinit var tvCity: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvWeatherCondition: TextView
    private lateinit var tvVisibility: TextView
    private lateinit var rvForecast: RecyclerView
    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var toolbar: Toolbar

    // Якість повітря (AQI)
    private lateinit var tvAqiIcon: TextView
    private lateinit var tvAqiLevel: TextView
    private lateinit var tvAqiPM25: TextView
    private lateinit var tvAqiPM10: TextView
    private lateinit var tvAqiSO2: TextView
    private lateinit var tvAqiNO2: TextView
    private lateinit var tvAqiO3: TextView
    private lateinit var tvAqiCO: TextView

    // --- Дані ---
    private var fullForecastList: List<WeatherListItem> = emptyList()

    // --- Мережа ---
    private val weatherService: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiKey = getApiKeyFromAssets()
        if (apiKey.isEmpty()) {
            Log.e("MainActivity", "API ключ не знайдено.")
            return
        }

        // Ініціалізація Toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Ініціалізація головного UI
        tvCity = findViewById(R.id.tvCity)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvWeatherCondition = findViewById(R.id.tvWeatherCondition)
        tvVisibility = findViewById(R.id.tvVisibility)
        rvForecast = findViewById(R.id.rvForecast)
        rvForecast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Ініціалізація UI якості повітря
        tvAqiIcon = findViewById(R.id.tvAqiIcon)
        tvAqiLevel = findViewById(R.id.tvAqiLevel)
        tvAqiPM25 = findViewById(R.id.tvAqiPM25)
        tvAqiPM10 = findViewById(R.id.tvAqiPM10)
        tvAqiSO2 = findViewById(R.id.tvAqiSO2)
        tvAqiNO2 = findViewById(R.id.tvAqiNO2)
        tvAqiO3 = findViewById(R.id.tvAqiO3)
        tvAqiCO = findViewById(R.id.tvAqiCO)

        val accordionHeader = findViewById<LinearLayout>(R.id.accordionHeader)
        val accordionContent = findViewById<LinearLayout>(R.id.accordionContent)

        accordionHeader.setOnClickListener {
            accordionContent.visibility =
                if (accordionContent.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // Завантажуємо дані для міста за замовчуванням
        fetchWeatherData()
    }

    // --- Обробка Меню (Toolbar) ---

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                showSearchCityDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // --- Діалогові вікна ---

    /**
     * Показ діалогу для пошуку міста
     */
    private fun showSearchCityDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search_city, null)

        val etSearchCity = dialogView.findViewById<EditText>(R.id.etSearchCity)
        val btnPerformSearch = dialogView.findViewById<ImageButton>(R.id.btnPerformSearch)
        val btnLviv = dialogView.findViewById<Button>(R.id.btnCityLviv)
        val btnOdesa = dialogView.findViewById<Button>(R.id.btnCityOdesa)
        val btnKharkiv = dialogView.findViewById<Button>(R.id.btnCityKharkiv)
        val btnDnipro = dialogView.findViewById<Button>(R.id.btnCityDnipro)
        val btnKyiv = dialogView.findViewById<Button>(R.id.btnCityKyiv)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setNegativeButton("Скасувати") { d, _ ->
                d.dismiss()
            }
            .create()

        btnPerformSearch.setOnClickListener {
            val query = etSearchCity.text.toString()
            if (query.isNotBlank()) {
                updateCityAndFetch(query)
                dialog.dismiss()
            }
        }

        // Обробники для кнопок-шаблонів
        val cityClickListener = { cityName: String ->
            updateCityAndFetch(cityName)
            dialog.dismiss()
        }
        btnLviv.setOnClickListener { cityClickListener("Lviv") }
        btnOdesa.setOnClickListener { cityClickListener("Odesa") }
        btnKharkiv.setOnClickListener { cityClickListener("Kharkiv") }
        btnDnipro.setOnClickListener { cityClickListener("Dnipro") }
        btnKyiv.setOnClickListener { cityClickListener("Kyiv") }

        dialog.show()
    }

    /**
     * Показ спливаючого вікна з деталями ТА погодинним прогнозом
     */
    @SuppressLint("SetTextI18n")
    private fun showWeatherDetailsDialog(item: WeatherListItem, position: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_weather_details, null)

        // 1. Заповнюємо основні дані
        val tvDetailDay = dialogView.findViewById<TextView>(R.id.tvDetailDay)
        val tvDetailDescription = dialogView.findViewById<TextView>(R.id.tvDetailDescription)
        val tvDetailTemp = dialogView.findViewById<TextView>(R.id.tvDetailTemp)
        val tvDetailHumidity = dialogView.findViewById<TextView>(R.id.tvDetailHumidity)
        val tvDetailWind = dialogView.findViewById<TextView>(R.id.tvDetailWind)
        val tvDetailVisibility = dialogView.findViewById<TextView>(R.id.tvDetailVisibility)

        tvDetailDay.text = if (position == 0) "Сьогодні" else formatDayOfWeek(item.dt_txt)
        tvDetailDescription.text = item.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "N/A"
        tvDetailTemp.text = "${item.main.temp.toInt()}°"
        tvDetailHumidity.text = "Вологість: ${item.main.humidity}%"
        tvDetailWind.text = "Вітер: ${item.wind.speed} м/с, ${degToCompass(item.wind.deg)}"
        tvDetailVisibility.text = "Видимість: ${item.visibility / 1000.0} км"

        // 2. Готуємо погодинний прогноз
        val clickedDate = item.dt_txt.substringBefore(" ")
        val hourlyDataForThisDay = fullForecastList.filter {
            it.dt_txt.startsWith(clickedDate)
        }

        val rvHourly: RecyclerView = dialogView.findViewById(R.id.rvHourlyForecast)
        val hourlyAdapter = HourlyForecastAdapter(hourlyDataForThisDay)
        rvHourly.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvHourly.adapter = hourlyAdapter

        // Додаємо розділювач
        val divider = DividerItemDecoration(this, (rvHourly.layoutManager as LinearLayoutManager).orientation)
        rvHourly.addItemDecoration(divider)

        // 3. Створюємо та показуємо діалог
        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Закрити") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // --- Завантаження даних ---

    /**
     * Оновлює місто та перезапускає завантаження даних
     */
    private fun updateCityAndFetch(newCity: String) {
        this.city = newCity
        fetchWeatherData()
    }

    /**
     * Завантажує прогноз погоди ТА якість повітря
     */
    private fun fetchWeatherData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Отримуємо прогноз
                val response = weatherService.getForecast(city, apiKey)
                fullForecastList = response.list
                val dailyForecasts = filterDailyForecasts(response.list)

                // Отримуємо lat/lon з першої відповіді
                val lat = response.city.coord.lat
                val lon = response.city.coord.lon

                // 2. Робимо другий запит для AQI
                try {
                    val aqiResponse = weatherService.getAirPollution(lat, lon, apiKey)
                    withContext(Dispatchers.Main) {
                        updateAirPollutionUI(aqiResponse)
                    }
                } catch (aqiError: Exception) {
                    Log.e("MainActivity", "Error fetching AQI data", aqiError)
                }

                // 3. Оновлюємо UI прогнозу
                withContext(Dispatchers.Main) {
                    if (dailyForecasts.isNotEmpty()) {
                        updateUI(dailyForecasts.first(), response.city.name)
                    }
                    forecastAdapter = ForecastAdapter(dailyForecasts) { item, position ->
                        showWeatherDetailsDialog(item, position)
                    }
                    rvForecast.adapter = forecastAdapter
                }
            } catch (e: Exception) {
                // Обробка помилки (напр., місто не знайдено)
                Log.e("MainActivity", "Error fetching weather data", e)
                withContext(Dispatchers.Main) {
                    tvCity.text = "Помилка"
                    tvTemperature.text = "-"
                    tvWeatherCondition.text = "Місто не знайдено"
                }
            }
        }
    }

    // --- Оновлення UI ---

    /**
     * Оновлює головний UI (поточна погода)
     */
    private fun updateUI(todayWeather: WeatherListItem, cityName: String) {
        tvCity.text = cityName
        tvTemperature.text = "${todayWeather.main.temp.toInt()}°"
        tvWeatherCondition.text = todayWeather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: ""
        val visibilityInKm = todayWeather.visibility / 1000.0
        tvVisibility.text = "$visibilityInKm км"
        // Тут також оновлюємо опис видимості, якщо потрібно
        // tvVisibilityDesc.text = "..."
    }

    /**
     * Оновлює UI для блоку якості повітря
     */
    private fun updateAirPollutionUI(response: AirPollutionResponse) {
        val aqiItem = response.list.firstOrNull() ?: return

        // 1. Оновлюємо компоненти
        tvAqiPM25.text = String.format(Locale.US, "%.1f", aqiItem.components.pm2_5)
        tvAqiPM10.text = String.format(Locale.US, "%.1f", aqiItem.components.pm10)
        tvAqiSO2.text = String.format(Locale.US, "%.1f", aqiItem.components.so2)
        tvAqiNO2.text = String.format(Locale.US, "%.1f", aqiItem.components.no2)
        tvAqiO3.text = String.format(Locale.US, "%.1f", aqiItem.components.o3)
        tvAqiCO.text = String.format(Locale.US, "%.0f", aqiItem.components.co)

        // 2. Оновлюємо головний індекс (текст і "картинку")
        val aqiValue = aqiItem.aqiData.aqi
        tvAqiLevel.text = getAqiString(aqiValue)

        // Встановлюємо колір для "картинки"
        val aqiColor = getAqiColor(aqiValue)
        // Переконуємось, що фон - це GradientDrawable, щоб змінити колір
        val background = tvAqiIcon.background.mutate() as? GradientDrawable
        background?.setColor(aqiColor)
    }

    // --- Допоміжні функції ---

    /**
     * Фільтрує повний список, щоб отримати 5 днів
     */
    private fun filterDailyForecasts(fullList: List<WeatherListItem>): List<WeatherListItem> {
        val dailyItems = mutableListOf<WeatherListItem>()
        val uniqueDays = mutableSetOf<String>()

        if (fullList.isNotEmpty()) {
            val today = fullList.first()
            dailyItems.add(today)
            uniqueDays.add(today.dt_txt.substringBefore(" "))
        }

        val noonForecasts = fullList.filter {
            val date = it.dt_txt.substringBefore(" ")
            it.dt_txt.endsWith("12:00:00") && !uniqueDays.contains(date)
        }

        for (item in noonForecasts) {
            val date = item.dt_txt.substringBefore(" ")
            if (!uniqueDays.contains(date)) {
                dailyItems.add(item)
                uniqueDays.add(date)
            }
            if (dailyItems.size >= 5) break
        }
        return dailyItems
    }

    /**
     * Читає API ключ з assets
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

    /**
     * Форматує день тижня (напр., "Нд")
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
     * Конвертує градуси у напрямок вітру
     */
    private fun degToCompass(degrees: Int): String {
        val directions = listOf("Пн", "Пн-Сх", "Сх", "Пд-Сх", "Пд", "Пд-Зх", "Зх", "Пн-Зх")
        val index = (degrees % 360) / 45
        return directions[index]
    }

    /**
     * Повертає текстовий опис для AQI
     */
    private fun getAqiString(aqi: Int): String {
        return when (aqi) {
            1 -> "Добре"
            2 -> "Задовільно"
            3 -> "Помірно"
            4 -> "Погано"
            5 -> "Дуже погано"
            else -> "N/A"
        }
    }

    /**
     * Повертає колір для "картинки" AQI
     */
    private fun getAqiColor(aqi: Int): Int {
        val colorId = when (aqi) {
            1 -> R.color.aqi_good
            2 -> R.color.aqi_fair
            3 -> R.color.aqi_moderate
            4 -> R.color.aqi_poor
            5 -> R.color.aqi_very_poor
            else -> R.color.aqi_unknown
        }
        return ContextCompat.getColor(this, colorId)
    }
}
