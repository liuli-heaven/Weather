package com.example.weather.net

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.time.withTimeout
import okhttp3.internal.wait
import java.time.Duration
import java.util.Locale
import kotlin.coroutines.coroutineContext


object LocationService {
    private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var lastKnownLocation: Location? = null
    fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationProvider = LocationManager.GPS_PROVIDER

        // 检查权限
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )) {
                // 向用户发出提示，解释为什么需要这些权限
                showPermissionExplanationDialog(context)
            } else {
                // 请求权限
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            Log.e("LocationService", "没有获得对应权限")
            return null
        }

        // 尝试获取最后一次已知位置
        if (lastKnownLocation != null){
            return Pair(lastKnownLocation!!.longitude, lastKnownLocation!!.latitude)

        } else {
            lastKnownLocation = locationManager.getLastKnownLocation(locationProvider)
        }


        // 设置位置监听器获取实时位置
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                lastKnownLocation = location
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        // 请求位置更新
        locationManager.requestLocationUpdates(
            locationProvider, 0, 0f, locationListener, Looper.getMainLooper()
        )

        // 当不再需要位置更新时，移除监听器
        (context as? AppCompatActivity)?.run {
            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    locationManager.removeUpdates(locationListener)
                }
            })
        }
        return Pair(lastKnownLocation?.longitude ?: 0.0, lastKnownLocation?.latitude ?: 0.0)
    }

    // 根据城市名称获取经纬度
    fun getLocationFromCityName(context: Context, cityName: String): Pair<Double, Double>? {
        return try {
            val geocoder = Geocoder(context, Locale.CHINA)
            val addresses: List<Address>? = geocoder.getFromLocationName(cityName, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                Pair(address.longitude, address.latitude)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LocationService", e.message.toString())
            null
        }
    }

    // 根据经纬度获取城市名称
    fun getCityNameFromLocation(context: Context, longitude: Double, latitude: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.CHINA)
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                // 获取省、市、区信息
                val province = address.adminArea // 省份
                val city = address.locality // 城市
                val district = address.subLocality // 区县
                // 组合成完整地址
                val fullAddress = listOfNotNull(province, city, district).joinToString("")
                fullAddress.ifEmpty { address.thoroughfare } // 如果省市区都为空，返回详细地址
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // 根据城市名称获取经纬度（Flow版本）
    fun getLocationFromCityNameFlow(context: Context, cityName: String): Flow<Pair<Double, Double>?> = flow {
        val location = getLocationFromCityName(context, cityName)
        emit(location)
    }.catch { e ->
        emit(null)
    }.flowOn(Dispatchers.IO)

    // 根据经纬度获取城市名称（Flow版本）
    fun getCityNameFromLocationFlow(context: Context, longitude: Double, latitude: Double): Flow<String?> = flow {
        val cityName = getCityNameFromLocation(context, longitude, latitude)
        emit(cityName)
    }.catch { e ->
        emit(null)
    }.flowOn(Dispatchers.IO)

    private fun showPermissionExplanationDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("需要定位权限")
            .setMessage("为了提供更好的服务，我们需要访问您的位置信息。请授予我们定位权限。")
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                // 请求权限
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}