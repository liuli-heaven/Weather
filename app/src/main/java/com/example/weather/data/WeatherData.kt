package com.example.weather.data

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Temperature(
    val lowTemperature: Int,
    val highTemperature: Int
)
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class WeatherHourData(
    val wp: String,
    val tem: Int,
    val fc_time: String,
    val city: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class WeatherDayData(
    val dateTemperature: Temperature,
    val date: String,
    val condition: String,
    val week: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class WeatherCurrentData(
    val cityName: String,
    val dateTemperature: Temperature,
    val temperature: Int,
    val condition: String
): ItemModel

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class EnvironmentPram(
    val rh: Int,            //相对湿度， 单位：%
    val pre_pro: Int,       //降水概率， 单位：%
    val uv_level: Int,   //紫外线级别
    val cloud_cover: String,//云量，单位：%
    val ws_desc: String,    //风力
    val wd_desc: String     //风向
) : ItemModel

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SunData(
    val sunrise: String,    //日出时间
    val sunset: String,     //日落时间
    val moonphase: String   //月相
) : ItemModel

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class WeatherData(val status: Int, val fc_time: String,
                       val tem_max: Int, val tem_min: Int, val week: String, val wp: String,
                       val rh: Int, val pre_pro: Int, val uv_level: Int, val cloud_cover: String,
                       val ws_desc: String, val wd_desc: String, val sunrise: String,
                       val sunset: String, val moonphase: String) : ItemModel

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class WeatherListData(val dataList: MutableList<WeatherData>) : ItemModel

fun parseWeatherData(json: String): WeatherData?{
    return Json.decodeFromString(WeatherData.serializer(), json)
}

class WeatherDayItem(val dataList: MutableList<WeatherDayData>) : ItemModel{

}
@SuppressLint("UnsafeOptInUsageError")
@Serializable
class WeatherHourItem(val dataList: MutableList<WeatherHourData>): ItemModel{

}

class Tip(val tipText: String = ""): ItemModel{

}
interface ItemModel