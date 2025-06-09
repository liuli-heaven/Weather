package com.example.weather.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.weather.data.WeatherDayData
import com.example.weather.R

class DayDataAdapter(private val items: MutableList<WeatherDayData>):
    RecyclerView.Adapter<ViewHolder>() {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather_day_data_item, parent, false)
        return DataViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val realHolder: DataViewHolder = holder as DataViewHolder
        val data = items[position]
        realHolder.bind(data)
    }

    class DataViewHolder(itemView: View): ViewHolder(itemView){
        private val weekText: TextView = itemView.findViewById(R.id.weekText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val conditionText: TextView = itemView.findViewById(R.id.conditionText)
        private val highTemperatureText: TextView = itemView.findViewById(R.id.highTemperatureText)
        private val lowTemperatureText: TextView = itemView.findViewById(R.id.lowTemperatureText)

        fun bind(dayData: WeatherDayData){
            weekText.text = dayData.week
            dateText.text = dayData.date
            conditionText.text = dayData.condition
            highTemperatureText.text = dayData.dateTemperature.highTemperature.toString()
            lowTemperatureText.text = dayData.dateTemperature.lowTemperature.toString()
        }
    }
}