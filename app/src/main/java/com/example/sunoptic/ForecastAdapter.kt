package com.example.sunoptic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sunoptic.network.WeatherListItem
import java.text.SimpleDateFormat
import java.util.Locale

class ForecastAdapter(private val forecastList: List<WeatherListItem>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    // Створює новий View (елемент списку)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.forecast_item, parent, false)
        return ForecastViewHolder(view)
    }

    // Заповнює View даними
    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecastItem = forecastList[position]
        holder.bind(forecastItem)
    }

    // Повертає кількість елементів у списку
    override fun getItemCount() = forecastList.size

    // Внутрішній клас, який "тримає" елементи UI
    class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDayOfWeek: TextView = itemView.findViewById(R.id.tvDayOfWeek)
        private val tvTemp: TextView = itemView.findViewById(R.id.tvTemp)

        fun bind(item: WeatherListItem) {
            tvTemp.text = "${item.main.temp.toInt()}°"
            tvDayOfWeek.text = formatDayOfWeek(item.dt_txt)
        }

        // Допоміжна функція для форматування дати у день тижня
        private fun formatDayOfWeek(dateString: String): String {
            try {
                // Парсимо дату з формату "2025-10-26 12:00:00"
                val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = parser.parse(dateString)

                // Форматуємо у день тижня (напр. "Нд")
                val formatter = SimpleDateFormat("E", Locale("uk", "UA"))
                return formatter.format(date).replaceFirstChar { it.uppercase() }
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }
        }
    }
}