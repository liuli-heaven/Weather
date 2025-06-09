package com.example.weather.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.data.EnvironmentPram
import com.example.weather.data.ItemModel
import com.example.weather.data.SunData
import com.example.weather.data.Tip
import com.example.weather.data.WeatherCurrentData
import com.example.weather.data.WeatherDayData
import com.example.weather.data.WeatherDayItem
import com.example.weather.data.WeatherHourData
import com.example.weather.data.WeatherHourItem
import com.example.weather.R

class WeatherAdapter(private val items: MutableList<ItemModel>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object{
        const val TYPE_HOUR_WEATHER = 0
        const val TYPE_DAY_WEATHER = 1
        const val TYPE_ENVIRONMENT = 2
        const val TYPE_SUN_MOON = 3
        const val TYPE_CURRENT_WEATHER = 4
        const val TYPE_TIP = 5
        const val ERROR = 10
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]){
            is WeatherHourItem -> TYPE_HOUR_WEATHER
            is WeatherDayItem -> TYPE_DAY_WEATHER
            is WeatherCurrentData -> TYPE_CURRENT_WEATHER
            is EnvironmentPram -> TYPE_ENVIRONMENT
            is SunData -> TYPE_SUN_MOON
            is Tip -> TYPE_TIP
            else -> ERROR
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            TYPE_HOUR_WEATHER -> {
                val view = LayoutInflater.from(parent.context).inflate()
                TextViewHolder(view)
            }
        }
    }

    class WeatherHourDataHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val dataList: RecyclerView = itemView.findViewById<RecyclerView>(R.id.dataList)
        fun bind(item: WeatherHourItem){
            dataList.layoutManager = LinearLayoutManager(itemView.context)
            dataList.adapter = HourDataAdapter(item.dataList)
        }
    }

    class WeatherDayDataHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val dataList: RecyclerView = itemView.findViewById<RecyclerView>(R.id.dataList)
        fun bind(item: WeatherDayItem){
            dataList.layoutManager = LinearLayoutManager(itemView.context)
            dataList.adapter = DayDataAdapter(item.dataList)
        }
    }

    class WeatherCurrentDataHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val cityText: TextView = itemView.findViewById(R.id.cityText)
        private val dateTemperatureText: TextView = itemView.findViewById(R.id.DateTemperatureText)
        private val temperatureText: TextView = itemView.findViewById(R.id.currentTemperatureText)
        private val conditionText: TextView = itemView.findViewById(R.id.conditionText)
        @SuppressLint("SetTextI18n")
        fun bind(item: WeatherCurrentData){
            cityText.text = item.cityName
            val str = "${item.dateTemperature.lowTemperature}℃/${item.dateTemperature.highTemperature}℃"
            dateTemperatureText.text = str
            temperatureText.text = "${item.temperature}℃"
            conditionText.text = item.condition
        }
    }

    class EnvironmentHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val rhText: TextView = itemView.findViewById(R.id.rhText)
        private val preproText: TextView = itemView.findViewById(R.id.preproText)
        private val uvLevelText: TextView = itemView.findViewById(R.id.uvLevelText)
        private val cloudText: TextView = itemView.findViewById(R.id.cloudcoverText)
        private val wsDescText: TextView = itemView.findViewById(R.id.wsdescText)
        private val wdDescText: TextView = itemView.findViewById(R.id.wddescText)
        fun bind(item: EnvironmentPram){
            rhText.text = item.rh.toString()
            preproText.text = item.pre_pro.toString()
            uvLevelText.text = item.uv_level
            cloudText.text = item.cloud_cover
            wsDescText.text = item.ws_desc
            wdDescText.text = item.wd_desc
        }
    }

    class SunHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val sunriseText: TextView = itemView.findViewById(R.id.sunRiseText)
        private val sunsetText: TextView = itemView.findViewById(R.id.sunSetText)
        private val moonPhaseText: TextView = itemView.findViewById(R.id.moonText)
        fun bind(item: SunData){
            sunriseText.text = item.sunrise
            sunsetText.text = item.sunset
            moonPhaseText.text = item.moonphase
        }
    }

    class TipHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val tipText: TextView = itemView.findViewById(R.id.tipText)
        fun bind(item: Tip){
            tipText.text = item.tipText
        }
    }

}