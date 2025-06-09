package com.example.weather.data

import android.content.ClipData.Item
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.util.Date

@Serializable
data class Temperature(
    val lowTemperature: Int,
    val highTemperature: Int
)

@Serializable
data class WeatherHourData(
    val condition: String,
    val temperature: Int,
    val time: String
)

@Serializable
data class WeatherDayData(
    val dateTemperature: Temperature,
    val date: String,
    val condition: String,
    val week: String
)

@Serializable
data class WeatherCurrentData(
    val cityName: String,
    val dateTemperature: Temperature,
    val temperature: Int,
    val condition: String
): ItemModel

@Serializable
data class EnvironmentPram(
    val rh: Int,            //相对湿度， 单位：%
    val pre_pro: Int,       //降水概率， 单位：%
    val uv_level: String,   //紫外线级别
    val cloud_cover: String,//云量，单位：%
    val ws_desc: String,    //风力
    val wd_desc: String     //风向
) : ItemModel

@Serializable
data class SunData(
    val sunrise: String,    //日出时间
    val sunset: String,     //日落时间
    val moonphase: String   //月相
) : ItemModel

interface ItemModel
fun parseWeatherHourData(json: String): WeatherHourData{
    return Json.decodeFromString(WeatherHourData.serializer(), json)
}

fun parseWeatherDayData(json: String): WeatherDayData{
    return Json.decodeFromString(WeatherDayData.serializer(), json)
}

class WeatherDayItem : ItemModel{
    val dataList: MutableList<WeatherDayData> = mutableListOf()
}

class WeatherHourItem: ItemModel{
    val dataList: MutableList<WeatherHourData> = mutableListOf()
}

class Tip: ItemModel{
    val tipText: String = ""
}