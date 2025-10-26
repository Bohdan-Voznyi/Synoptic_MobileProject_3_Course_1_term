package com.example.sunoptic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sunoptic.network.WeatherListItem
import java.text.SimpleDateFormat
import java.util.Locale

// ✅ Крок 1: Додаємо лямбда-функцію 'onItemClick' до конструктора
class ForecastAdapter(
    private val forecastList: List<WeatherListItem>,
    private val onItemClick: (WeatherListItem, Int) -> Unit // Передаємо елемент та позицію
) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.forecast_item, parent, false)
        return ForecastViewHolder(view)
    }

    // ✅ Крок 2: Передаємо позицію до 'bind'
    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecastItem = forecastList[position]
        holder.bind(forecastItem, position, onItemClick) // Передаємо клік-колбек
    }

    override fun getItemCount() = forecastList.size

    class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDayOfWeek: TextView = itemView.findViewById(R.id.tvDayOfWeek)
        private val tvTemp: TextView = itemView.findViewById(R.id.tvTemp)

        // ✅ Крок 3: Оновлюємо 'bind'
        fun bind(
            item: WeatherListItem,
            position: Int,
            onItemClick: (WeatherListItem, Int) -> Unit
        ) {
            tvTemp.text = "${item.main.temp.toInt()}°"

            // ✅ Крок 4: Перший елемент - "Сьогодні", решта - дні тижня
            if (position == 0) {
                tvDayOfWeek.text = "Сьогодні"
            } else {
                tvDayOfWeek.text = formatDayOfWeek(item.dt_txt)
            }

            // ✅ Крок 5: Встановлюємо слухач натискань
            itemView.setOnClickListener {
                onItemClick(item, position)
            }
        }

        private fun formatDayOfWeek(dateString: String): String {
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = parser.parse(dateString)
                val formatter = SimpleDateFormat("E", Locale("uk", "UA"))
                return formatter.format(date).replaceFirstChar { it.uppercase() }
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }
        }
    }
}
