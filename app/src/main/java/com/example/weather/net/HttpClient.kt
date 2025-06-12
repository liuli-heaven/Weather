package com.example.weather.net

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.annotation.RestrictTo
import androidx.annotation.UiContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object HttpClient {
    val client = OkHttpClient()

    fun getHourWeatherUrl(context: Context, token: String, city: String = ""): String{
        var url = "https://api.open.geovisearth.com/v2/cn/hourly/basic?location="
        if (city.isEmpty()){
            val location = LocationService.getCurrentLocation(context)
            if (location != null){
                url += "${location.first},${location.second}&token=$token"
            } else {
                Log.e("HttpClient", "getHourWeatherUrl error, location is null")
            }

        } else {
            val location =  LocationService.getLocationFromCityName(context, city)
            if (location != null){
                url += "${location.first},${location.second}&token=$token"
            } else {
                Log.e("HttpClient", "getHourWeatherUrl error, city name is error")
            }
        }
        return url
    }
    fun getDayWeatherUrl(context: Context, token: String, city: String = ""): String{
        var url = "https://api.open.geovisearth.com/v2/cn/city/basic?location="
        if (city.isEmpty()){
             val location = LocationService.getCurrentLocation(context)
             if (location != null){
                 url += "${location.first},${location.second}&token=$token"
             } else {
                 Log.e("HttpClient", "getHourWeatherUrl error, location is null")
             }
        } else {
            val location =  LocationService.getLocationFromCityName(context, city)
            if (location != null){
                url += "${location.first},${location.second}&token=$token"
            } else {
                Log.e("HttpClient", "getHourWeatherUrl error, city name is error")
            }
        }
        return url
    }

    fun getHourWeatherData(context: Context, token: String, city: String, callback: (String?) -> Unit){
        val request = Request.Builder()
            .url(getHourWeatherUrl(context, token, city))
            .build()
        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Toast.makeText(context, "网络请求失败", Toast.LENGTH_SHORT).show()
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful){
                    Toast.makeText(context, "网络请求失败", Toast.LENGTH_SHORT).show()
                    callback(null)
                    return
                }
                callback(response.body?.string())
            }
        })
    }
    fun getDayWeatherData(context: Context, token: String, city: String, callback: (String?) -> Unit){
        val request = Request.Builder()
            .url(getDayWeatherUrl(context, token, city))
            .build()
        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Toast.makeText(context, "网络请求失败", Toast.LENGTH_SHORT).show()
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful){
                    Toast.makeText(context, "网络请求失败", Toast.LENGTH_SHORT).show()
                    callback(null)
                    return
                }
                callback(response.body?.string())
            }
        })
    }
}