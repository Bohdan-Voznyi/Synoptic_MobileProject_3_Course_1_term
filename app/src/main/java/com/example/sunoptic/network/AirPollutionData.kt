package com.example.sunoptic.network

import com.google.gson.annotations.SerializedName

// Головна відповідь від API
data class AirPollutionResponse(
    val list: List<AirPollutionItem>
)

// Один елемент (зазвичай, в списку лише один)
data class AirPollutionItem(
    @SerializedName("main")
    val aqiData: AqiData, // Індекс якості повітря (1-5)
    val components: Components // Хімічні компоненти
)

// "main" об'єкт
data class AqiData(
    val aqi: Int // 1 = Добре, 2 = Задовільно, 3 = Помірно, 4 = Погано, 5 = Дуже погано
)

// Компоненти забруднення
data class Components(
    val co: Double,    // Концентрація CO (Оксид вуглецю)
    val no2: Double,   // Концентрація NO₂ (Діоксид азоту)
    val o3: Double,    // Концентрація O₃ (Озон)
    val so2: Double,   // Концентрація SO₂ (Діоксид сірки)
    val pm2_5: Double, // Концентрація PM2.5 (Дрібнодисперсні частинки)
    val pm10: Double   // Концентрація PM10 (Частинки)
)