package com.example.weather.net

import android.location.Location
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object HttpClient {
    val client = OkHttpClient()

    fun getHourWeatherUrl(token: String, city: String = ""): String{
        val location: Location?
        var url = "https://api.open.geovisearth.com/v2/cn/hourly/basic?location="
        if (city.isEmpty()){
            location = LocationService.getLastKnownLocation()
            if (location == null){
                Log.e("HttpClient", "getHourWeatherUrl error, location is null")
                return ""
            } else {
                url += "${location.longitude}.${location.latitude}&token=$token"
            }
        } else {
            TODO()
        }
        return url
    }
    fun getDayWeatherUrl(token: String, city: String = ""): String{
        return ""
    }

    fun getHourWeatherData(token: String, callback: (String?) -> Unit, city: String = ""){
        val request = Request.Builder()
            .url(getHourWeatherUrl(token, city))
            .build()
        HttpClient.client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful){
                    callback(null)
                    return
                }
                callback(response.body?.string())
            }
        })
    }
}