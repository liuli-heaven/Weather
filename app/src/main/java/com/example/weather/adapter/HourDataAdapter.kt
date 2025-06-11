package com.example.weather.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.weather.data.WeatherHourData
import com.example.weather.R

class HourDataAdapter(private val items: MutableList<WeatherHourData>):
    RecyclerView.Adapter<ViewHolder>(){
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather_hour_data_item, parent, false)
        return DataViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val realHolder: DataViewHolder = holder as DataViewHolder
        val data = items[position]
        realHolder.bind(data)
    }
    class DataViewHolder(itemView: View): ViewHolder(itemView){
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val currentTemperatureText: TextView = itemView.findViewById(R.id.currentTemperatureText)

        fun bind(hourData: WeatherHourData){
            timeText.text = hourData.fc_time
            currentTemperatureText.text = hourData.tem.toString()
        }
    }
}