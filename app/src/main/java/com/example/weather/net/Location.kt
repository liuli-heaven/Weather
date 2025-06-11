package com.example.weather.net

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat

@SuppressLint("StaticFieldLeak")
object LocationService: LocationListener {
    private lateinit var locationManager: LocationManager
    private var context: Context? = null
    private var lastLocation: Location? = null

    fun init(context: Context) {
        if (!::locationManager.isInitialized) {
            this.context = context.applicationContext
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
    }

    fun requestLocationUpdates(minTime: Long = 1000, minDistance: Float = 10f, callback: (Location?) -> Unit) {
        context?.let {
            if (it.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime,
                    minDistance,
                    this
                )
                lastLocation?.let { location -> callback(location) }
            } else {
                callback(null)
            }
        }
    }

    fun getLastKnownLocation(): Location? {

        return lastLocation
    }

    fun stopLocationUpdates() {
        context?.let {
            locationManager.removeUpdates(this)
        }
    }

    override fun onLocationChanged(location: Location) {
        lastLocation = location
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}


}