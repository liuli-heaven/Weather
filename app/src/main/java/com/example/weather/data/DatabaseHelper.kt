package com.example.weather.data

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.weather.net.HttpClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.time.LocalDate
import java.time.LocalDateTime

interface WeatherDataCallback{
    fun drawHourData(weatherHourItem: WeatherHourItem, dateTemperature: Temperature?)
    fun drawDayData(weatherListData: WeatherListData)
}

class DatabaseHelper(val context: Context): SQLiteOpenHelper(context, "weather.db", null, 1) {
    private var callback: WeatherDataCallback? = null
    val jsonDecoder = Json {ignoreUnknownKeys = true}
    override fun onCreate(db: SQLiteDatabase) {
        //创建日天气数据表
        db.execSQL("""
            CREATE TABLE weather_day (
                month INTEGER,
                date INTEGER,
                city TEXT,
                high_temperature INTEGER,
                low_temperature INTEGER,
                week TEXT,
                condition TEXT,
                rh INTEGER,
                pre_pro INTEGER,
                uv_level TEXT,
                cloud_cover TEXT,
                ws_desc TEXT,
                wd_desc TEXT,
                sunrise TEXT,
                sunset TEXT,
                moon_phase TEXT,
                PRIMARY KEY (month, date, city)
            )
        """.trimMargin())
        //创建小时天气数据表
        db.execSQL("""
            CREATE TABLE weather_hour (
                date INTEGER,
                hour INTEGER,
                city TEXT,
                temperature INTEGER,
                high_temperature INTEGER,
                low_temperature INTEGER,
                condition TEXT,
                PRIMARY KEY (date, hour, city)
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS weather_day")
        db.execSQL("DROP TABLE IF EXISTS weather_hour")
        onCreate(db)
    }

    private fun saveWeatherDay(month: Int, date: Int, city: String,
                                dateTemperature: Temperature, week: String, condition: String,
                                rh: Int, prePro: Int, uvLevel: Int, cloudCover: String,
                                wsDesc: String, wdDesc: String, sunRise: String,
                                sunSet: String, moonPhase: String){
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("month", month)
            put("date", date)
            put("city", city)
            put("high_temperature", dateTemperature.highTemperature)
            put("low_temperature", dateTemperature.lowTemperature)
            put("week", week)
            put("condition", condition)
            put("rh", rh)
            put("pre_pro", prePro)
            put("uv_level", uvLevel)
            put("cloud_cover", cloudCover)
            put("ws_desc", wsDesc)
            put("wd_desc", wdDesc)
            put("sunrise", sunRise)
            put("sunset", sunSet)
            put("moon_phase", moonPhase)
        }
        db.insert("weather_day", null, values)
        db.close()
    }

    private fun saveWeatherHour(date: Int, hour: Int, city: String, temperature: Int, dateTemperature: Temperature?, condition: String){
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("date", date)
            put("hour", hour)
            put("city", city)
            put("temperature", temperature)
            put("high_temperature", dateTemperature?.highTemperature)
            put("low_temperature", dateTemperature?.lowTemperature)
            put("condition", condition)
        }
        db.insert("weather_hour", null, values)
        db.close()
    }

    @SuppressLint("Range")
    fun queryWeatherCurrent(city: String, date: Int): WeatherCurrentData? {
        var hour = LocalDateTime.now().hour
        val db = this.readableDatabase
        val cursor = db.query("weather_hour", null, "city = ? AND date = ? AND hour = ?",
            arrayOf(city, date.toString(), hour.toString()), null, null, null)
        var weatherCurrentData: WeatherCurrentData? = null
        if (cursor.moveToFirst()){
            val temperature = cursor.getInt(cursor.getColumnIndex("temperature"))
            val condition = cursor.getString(cursor.getColumnIndex("condition"))
            val highTemperature = cursor.getInt(cursor.getColumnIndex("high_temperature"))
            val lowTemperature = cursor.getInt(cursor.getColumnIndex("low_temperature"))
            weatherCurrentData = WeatherCurrentData(city, Temperature(lowTemperature, highTemperature),
                temperature, condition)
        }
        cursor.close()
        db.close()
        return weatherCurrentData
    }

    @SuppressLint("Range")
    fun queryEnvironment(city: String, month: Int, date: Int): EnvironmentPram?{
        val db = this.readableDatabase
        val cursor = db.query("weather_day", null, "city = ? AND month = ? AND date = ?",
            arrayOf(city, month.toString(), date.toString()), null, null, null)
        var environmentPram: EnvironmentPram? = null
        if(cursor.moveToFirst()){
            val rh = cursor.getInt(cursor.getColumnIndex("rh"))
            val prePro = cursor.getInt(cursor.getColumnIndex("pre_pro"))
            val uvLevel = cursor.getInt(cursor.getColumnIndex("uv_level"))
            val cloudCover = cursor.getString(cursor.getColumnIndex("cloud_cover"))
            val wsDesc = cursor.getString(cursor.getColumnIndex("ws_desc"))
            val wdDesc = cursor.getString(cursor.getColumnIndex("wd_desc"))
            environmentPram = EnvironmentPram(rh, prePro, uvLevel, cloudCover, wsDesc, wdDesc)
        }
        cursor.close()
        db.close()
        return environmentPram
    }
    @SuppressLint("Range")
    fun querySunData(city: String, month: Int, date: Int): SunData?{
        val db = this.readableDatabase
        val cursor = db.query("weather_day", null, "city = ? AND month = ? AND date = ?",
            arrayOf(city, month.toString(), date.toString()), null, null, null)
        var environmentPram: SunData? = null
        if (cursor.moveToFirst()){
            val sunRise = cursor.getString(cursor.getColumnIndex("sunrise"))
            val sunSet = cursor.getString(cursor.getColumnIndex("sunset"))
            val moonPhase = cursor.getString(cursor.getColumnIndex("moon_phase"))
            environmentPram = SunData(sunRise, sunSet, moonPhase)
        }
        cursor.close()
        db.close()
        return environmentPram
    }
    @SuppressLint("Range")
    private fun queryWeatherHour(city: String, date: Int): WeatherHourItem{
        val db = this.readableDatabase
        var cursor = db.query("weather_hour",
            arrayOf("hour", "temperature", "condition"), "city = ? AND date = ?",
            arrayOf(city, date.toString()), null, null, "hour ASC")
        var weatherHourItem = WeatherHourItem(mutableListOf())
        if (cursor.moveToFirst()){
            do {
                val condition = cursor.getString(cursor.getColumnIndex("condition"))
                val temperature = cursor.getInt(cursor.getColumnIndex("temperature"))
                val hour = cursor.getInt(cursor.getColumnIndex("hour"))
                val city = cursor.getString(cursor.getColumnIndex("city"))
                var time = "${hour}:00"
                if (hour < 10){
                    time = "0$time"
                }
                weatherHourItem.dataList.add(WeatherHourData(condition, temperature, time, city))
            }while (cursor.moveToNext())
        }
        cursor.close()
        if (weatherHourItem.dataList.size < 24){
            val now = LocalDate.now()
            var nextDate: Int
            if (date == now.lengthOfMonth()){
                nextDate = 1
            } else {
                nextDate = date + 1
            }
            cursor = db.query("weather_hour",
                arrayOf("hour", "temperature", "condition"), "city = ? AND date = ?",
                arrayOf(city, date.toString()), null, null, "hour ASC")
            var weatherHourItem = WeatherHourItem(mutableListOf())
            if (cursor.moveToFirst()){
                do {
                    val condition = cursor.getString(cursor.getColumnIndex("condition"))
                    val temperature = cursor.getInt(cursor.getColumnIndex("temperature"))
                    val hour = cursor.getInt(cursor.getColumnIndex("hour"))
                    val city = cursor.getString(cursor.getColumnIndex("city"))
                    var time = "${hour}:00"
                    if (hour < 10){
                        time = "0$time"
                    }
                    weatherHourItem.dataList.add(WeatherHourData(condition, temperature, time, city))
                    if (weatherHourItem.dataList.size == 24) break
                }while (cursor.moveToNext())
            }
            cursor.close()
        }
        db.close()
        return weatherHourItem
    }
    @SuppressLint("Range")
    private fun queryWeatherDay(city: String, month: Int, date: Int): WeatherListData{
        val db = this.readableDatabase
        var cursor = db.query("weather_day",
            null, "city = ? AND month = ? AND date >= ?",
            arrayOf(city, month.toString(), date.toString()), null, null, "date ASC")
        var weatherListData = WeatherListData(mutableListOf())
        if (cursor.moveToFirst()){
            do {
                val month = cursor.getInt(cursor.getColumnIndex("month"))
                val date = cursor.getInt(cursor.getColumnIndex("date"))
                val week = cursor.getString(cursor.getColumnIndex("week"))
                val highTemperature = cursor.getInt(cursor.getColumnIndex("high_temperature"))
                val lowTemperature = cursor.getInt(cursor.getColumnIndex("low_temperature"))
                val condition = cursor.getString(cursor.getColumnIndex("condition"))
                val rh = cursor.getInt(cursor.getColumnIndex("rh"))
                val prePro = cursor.getInt(cursor.getColumnIndex("pre_pro"))
                val uvLevel = cursor.getInt(cursor.getColumnIndex("uv_level"))
                val cloudCover = cursor.getString(cursor.getColumnIndex("cloud_cover"))
                val wsDesc = cursor.getString(cursor.getColumnIndex("ws_desc"))
                val wdDesc = cursor.getString(cursor.getColumnIndex("wd_desc"))
                val sunRise = cursor.getString(cursor.getColumnIndex("sunrise"))
                val sunSet = cursor.getString(cursor.getColumnIndex("sunset"))
                val moonPhase = cursor.getString(cursor.getColumnIndex("moon_phase"))
                val fc_time = "${LocalDateTime.now().year}$month$date"
                weatherListData.dataList.add(WeatherData(0, fc_time, highTemperature, lowTemperature, week, condition,
                        rh, prePro, uvLevel, cloudCover, wsDesc, wdDesc, sunRise, sunSet, moonPhase))
                if (weatherListData.dataList.size == 15) break
            } while (cursor.moveToNext())
        }
        cursor.close()
        if (weatherListData.dataList.size < 15){
            var nextMonth: Int
            if (month == 12) nextMonth = 1
            else nextMonth = month + 1
            cursor = db.query("weather_day",
                arrayOf("week", "high_temperature", "low_temperature", "date", "condition"), "city = ? AND month = ?",
                arrayOf(city, nextMonth.toString()), null, null, "date ASC")
            if (cursor.moveToFirst()){
                do {
                    val month = cursor.getInt(cursor.getColumnIndex("month"))
                    val date = cursor.getInt(cursor.getColumnIndex("date"))
                    val week = cursor.getString(cursor.getColumnIndex("week"))
                    val highTemperature = cursor.getInt(cursor.getColumnIndex("high_temperature"))
                    val lowTemperature = cursor.getInt(cursor.getColumnIndex("low_temperature"))
                    val condition = cursor.getString(cursor.getColumnIndex("condition"))
                    val rh = cursor.getInt(cursor.getColumnIndex("rh"))
                    val prePro = cursor.getInt(cursor.getColumnIndex("pre_pro"))
                    val uvLevel = cursor.getInt(cursor.getColumnIndex("uv_level"))
                    val cloudCover = cursor.getString(cursor.getColumnIndex("cloud_cover"))
                    val wsDesc = cursor.getString(cursor.getColumnIndex("ws_desc"))
                    val wdDesc = cursor.getString(cursor.getColumnIndex("wd_desc"))
                    val sunRise = cursor.getString(cursor.getColumnIndex("sunrise"))
                    val sunSet = cursor.getString(cursor.getColumnIndex("sunset"))
                    val moonPhase = cursor.getString(cursor.getColumnIndex("moon_phase"))
                    val fc_time = "${LocalDateTime.now().year}$month$date"
                    weatherListData.dataList.add(WeatherData(0, fc_time, highTemperature, lowTemperature, week, condition,
                        rh, prePro, uvLevel, cloudCover, wsDesc, wdDesc, sunRise, sunSet, moonPhase))
                    if (weatherListData.dataList.size == 15) break
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        db.close()
        return weatherListData
    }

    fun queryWeather(token: String, city: String = ""){
        val now = LocalDate.now()
        val weatherDayItem = queryWeatherDay(city, now.month.value, now.dayOfMonth)
        var nowTemperature: Temperature? = null
        var nextTemperature: Temperature? = null
        if (weatherDayItem.dataList.isEmpty() || weatherDayItem.dataList.size < 15){
            HttpClient.getDayWeatherData(context, token, city){ json ->
                if (json != null){
                    val data = Json.parseToJsonElement(json)
                    if(data.jsonObject["result"] != null){
                        var datas = "{\"dataList\":${data.jsonObject["result"]?.jsonObject["datas"].toString()}}"
                        val weatherData = jsonDecoder.decodeFromString<WeatherListData>(WeatherListData.serializer(), datas)
                        var count = 1
                        for (data in weatherData.dataList){
                            if (count == 1){
                                nowTemperature = Temperature(data.tem_min, data.tem_max)
                            }
                            if (count == 2){
                                nextTemperature = Temperature(data.tem_min, data.tem_max)
                            }
                            val month = data.fc_time.substring(4, 6).toInt()
                            val date = data.fc_time.substring(6, 8).toInt()
                            count++
                            saveWeatherDay(month, date, city, Temperature(data.tem_min, data.tem_max),
                                data.week, data.wp, data.rh, data.pre_pro, data.uv_level,
                                data.cloud_cover, data.ws_desc, data.wd_desc, data.sunrise,
                                data.sunset, data.moonphase)
                        }
                        Handler(Looper.getMainLooper()).post {
                            callback?.drawDayData(weatherData)
                        }
                    }
                } else {
                    Toast.makeText(context, "获取天气信息失败", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            callback?.drawDayData(weatherDayItem)
        }
        val weatherHourItem = queryWeatherHour(city, now.dayOfMonth)
        if (weatherHourItem.dataList.isEmpty() || weatherHourItem.dataList.size < 24){
            HttpClient.getHourWeatherData(context, token, city) { json ->
                if (json != null){
                    val data = Json.parseToJsonElement(json)
                    if (data.jsonObject["result"] != null){
                        var datas = "{\"dataList\":${data.jsonObject["result"]?.jsonObject["datas"].toString()}}"

                        val weatherHourData = jsonDecoder.decodeFromString<WeatherHourItem>(WeatherHourItem.serializer(), datas)
                        for(elem in weatherHourData.dataList){
                            val hour = elem.fc_time.substring(8).toInt()
                            val date = elem.fc_time.substring(6, 8).toInt()
                            if (date == now.dayOfMonth){
                                saveWeatherHour(date, hour, city, elem.tem, nowTemperature, elem.wp)
                            } else {
                                if (date == now.lengthOfMonth()){
                                    saveWeatherHour(1, hour, city, elem.tem, nextTemperature, elem.wp)
                                } else {
                                    saveWeatherHour(date + 1, hour, city, elem.tem, nextTemperature, elem.wp)
                                }
                            }
                        }
                        Handler(Looper.getMainLooper()).post {
                            callback?.drawHourData(weatherHourData, nowTemperature)
                        }
                    }
                } else {
                    Toast.makeText(context, "获取天气信息失败", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            callback?.drawHourData(weatherHourItem, nowTemperature)
        }
    }

    fun setCallback(callback: WeatherDataCallback) {
        this.callback = callback
    }

}