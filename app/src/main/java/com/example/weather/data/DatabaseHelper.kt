package com.example.weather.data

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Handler
import android.os.Looper
import com.example.weather.net.HttpClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.time.LocalDate
import java.time.LocalDateTime

import kotlin.time.Duration.Companion.hours

class DatabaseHelper(val context: Context): SQLiteOpenHelper(context, "weather.db", null, 1) {
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
                                rh: Int, prePro: Int, uvLevel: String, cloudCover: String,
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

    private fun saveWeatherHour(date: Int, hour: Int, city: String, temperature: Int, dateTemperature: Temperature, condition: String){
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("date", date)
            put("hour", hour)
            put("city", city)
            put("temperature", temperature)
            put("high_temperature", dateTemperature.highTemperature)
            put("low_temperature", dateTemperature.lowTemperature)
            put("condition", condition)
        }
        db.insert("weather_hour", null, values)
        db.close()
    }

    @SuppressLint("Range")
    private fun queryWeatherCurrent(city: String, date: Int): WeatherCurrentData? {
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
    private fun queryEnvironment(city: String, month: Int, date: Int): EnvironmentPram?{
        val db = this.readableDatabase
        val cursor = db.query("weather_day", null, "city = ? AND month = ? AND date = ?",
            arrayOf(city, month.toString(), date.toString()), null, null, null)
        var environmentPram: EnvironmentPram? = null
        if(cursor.moveToFirst()){
            val rh = cursor.getInt(cursor.getColumnIndex("rh"))
            val prePro = cursor.getInt(cursor.getColumnIndex("pre_pro"))
            val uvLevel = cursor.getString(cursor.getColumnIndex("uv_level"))
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
    private fun querySunData(city: String, month: Int, date: Int): SunData?{
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
        val cursor = db.query("weather_hour",
            arrayOf("hour", "temperature", "condition"), "city = ? AND date = ?",
            arrayOf(city, date.toString()), null, null, "hour ASC")
        var weatherHourItem = WeatherHourItem(mutableListOf())
        if (cursor.moveToFirst()){
            do {
                val condition = cursor.getString(cursor.getColumnIndex("condition"))
                val temperature = cursor.getInt(cursor.getColumnIndex("temperature"))
                val hour = cursor.getInt(cursor.getColumnIndex("hour"))
                var time = "${hour}:00"
                if (hour >= 10){
                    time = "0$time"
                }
                weatherHourItem.dataList.add(WeatherHourData(condition, temperature, time))
            }while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return weatherHourItem
    }
    @SuppressLint("Range")
    private fun queryWeatherDay(city: String, month: Int, date: Int): WeatherDayItem{
        val db = this.readableDatabase
        var cursor = db.query("weather_day",
            arrayOf("week", "high_temperature", "low_temperature", "date", "condition"), "city = ? AND month = ? AND date >= ?",
            arrayOf(city, month.toString(), date.toString()), null, null, "date ASC")
        var weatherDayItem = WeatherDayItem(mutableListOf())
        if (cursor.moveToFirst()){
            do {
                val week = cursor.getString(cursor.getColumnIndex("week"))
                val highTemperature = cursor.getInt(cursor.getColumnIndex("high_temperature"))
                val lowTemperature = cursor.getInt(cursor.getColumnIndex("low_temperature"))
                val condition = cursor.getString(cursor.getColumnIndex("condition"))
                val date = cursor.getString(cursor.getColumnIndex("date"))
                weatherDayItem.dataList.add(WeatherDayData(Temperature(lowTemperature, highTemperature),
                    date, condition, week))
                if (weatherDayItem.dataList.size == 15) break
            } while (cursor.moveToNext())
        }
        if (weatherDayItem.dataList.size < 15){
            var nextMonth: Int
            if (month == 12) nextMonth = 1
            else nextMonth = month + 1
            cursor = db.query("weather_day",
                arrayOf("week", "high_temperature", "low_temperature", "date", "condition"), "city = ? AND month = ?",
                arrayOf(city, nextMonth.toString()), null, null, "date ASC")
            if (cursor.moveToFirst()){
                do {
                    val week = cursor.getString(cursor.getColumnIndex("week"))
                    val highTemperature = cursor.getInt(cursor.getColumnIndex("high_temperature"))
                    val lowTemperature = cursor.getInt(cursor.getColumnIndex("low_temperature"))
                    val condition = cursor.getString(cursor.getColumnIndex("condition"))
                    val date = cursor.getString(cursor.getColumnIndex("date"))
                    weatherDayItem.dataList.add(WeatherDayData(Temperature(lowTemperature, highTemperature),
                        date, condition, week))
                    if (weatherDayItem.dataList.size == 15) break
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return weatherDayItem
    }

    fun queryWeatherDay(token: String, city: String){
        val now = LocalDateTime.now()
        val weatherDayItem = queryWeatherDay(city, now.month.value, now.dayOfMonth)
        if (weatherDayItem.dataList.isEmpty() || weatherDayItem.dataList.size < 15){
            HttpClient.getDayWeatherData(token, city){ json ->
                if (json != null){
                    val data = Json.parseToJsonElement(json)
                    if(data.jsonObject["result"] != null){
                        val datas = data.jsonObject["result"].toString()

                        val weatherData = Json.decodeFromString<WeatherListData>(WeatherListData.serializer(), datas)
                        if (weatherData!= null) {
                            for (data in weatherData.list){
                                val month = data.fc_time.substring(4, 5).toInt()
                                val date = data.fc_time.substring(6, 7).toInt()
                                saveWeatherDay(month, date, city, Temperature(data.tem_min, data.tem_max),
                                    data.week, data.wp, data.rh, data.pre_pro, data.uv_level,
                                    data.cloud_cover, data.ws_desc, data.wd_desc, data.sunrise,
                                    data.sunset, data.moonphase)
                            }
                        }
                    }
                }
            }
        } else {

        }

    }

    fun updateUI(weatherData: WeatherData){

    }
}