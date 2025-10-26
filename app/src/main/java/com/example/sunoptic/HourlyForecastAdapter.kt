package com.example.sunoptic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sunoptic.network.WeatherListItem
import java.text.SimpleDateFormat
import java.util.Locale

class HourlyForecastAdapter(
    private val hourlyList: List<WeatherListItem>
) : RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.hourly_forecast_item, parent, false)
        return HourlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        val item = hourlyList[position]
        holder.bind(item)
    }

    override fun getItemCount() = hourlyList.size

    class HourlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tvHourlyTime)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivHourlyIcon)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvHourlyDescription)
        private val tvTemp: TextView = itemView.findViewById(R.id.tvHourlyTemp)

        fun bind(item: WeatherListItem) {
            // Форматуємо час (напр., "2025-10-27 15:00:00" -> "15:00")
            tvTime.text = formatTime(item.dt_txt)
            tvDescription.text = item.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: ""
            tvTemp.text = "${item.main.temp.toInt()}°"

            // Формуємо URL для іконки
            item.weather.firstOrNull()?.icon?.let { iconId ->
                val iconUrl = "https://openweathermap.org/img/wn/$iconId@2x.png"
                Glide.with(itemView.context)
                    .load(iconUrl)
                    .into(ivIcon)
            }
        }

        private fun formatTime(dateString: String): String {
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = parser.parse(dateString)
                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                return formatter.format(date)
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }
        }
    }
}