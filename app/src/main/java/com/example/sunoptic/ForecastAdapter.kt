package com.example.sunoptic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sunoptic.network.WeatherListItem
import java.text.SimpleDateFormat
import java.util.Locale

class ForecastAdapter(
    private val forecastList: List<WeatherListItem>,
    private val onItemClick: (WeatherListItem, Int) -> Unit
) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    inner class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tvDayOfWeek)
        val tvTemp: TextView = itemView.findViewById(R.id.tvTemp)
        val tvHumidity: TextView = itemView.findViewById(R.id.tvForecastHumidity)

        val tvWind: TextView = itemView.findViewById(R.id.tvForecastWind)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.forecast_item, parent, false)
        return ForecastViewHolder(view)
    }

    override fun getItemCount(): Int = forecastList.size

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val item = forecastList[position]

        // День недели
        holder.tvDay.text = if (position == 0) "Сьогодні"
        else formatDayOfWeek(item.dt_txt)

        // Температура
        holder.tvTemp.text = "${item.main.temp.toInt()}°"

        // Влажность
        holder.tvHumidity.text = "Вологість: ${item.main.humidity}%"

        holder.tvWind.text = "Вітер: ${degToCompass(item.wind.deg)}"


        // Клик по карточке
        holder.itemView.setOnClickListener {
            onItemClick(item, position)
        }
    }

    private fun degToCompass(degrees: Int): String {
        val directions = listOf("Пн", "Пн-Сх", "Сх", "Пд-Сх", "Пд", "Пд-Зх", "Зх", "Пн-Зх")
        val index = ((degrees % 360) / 45) % 8
        return directions[index]
    }
    // Форматирование дня недели, пример: "Пн", "Вт" и т.д.
    private fun formatDayOfWeek(dateString: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = parser.parse(dateString)
            val formatter = SimpleDateFormat("E", Locale("uk", "UA"))
            formatter.format(date ?: return "N/A").replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            "N/A"
        }
    }
}
