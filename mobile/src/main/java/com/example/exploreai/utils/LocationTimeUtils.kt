package com.mau.exploreai.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
object LocationTimeUtils {
    
    /**
     * Gets the current date, time, and user's location
     * @param context The context to use for location services
     * @param callback Callback that returns formatted date, time and location
     */
    fun getCurrentDateTimeLocation(
        context: Context, 
        callback: (date: String, time: String, location: String) -> Unit
    ) {
        // Get current date and time
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mma", Locale.getDefault())
        val currentDate = Date()
        val formattedDate = dateFormat.format(currentDate)
        val formattedTime = timeFormat.format(currentDate)
        
        // Get current location
        val fusedLocationClient: FusedLocationProviderClient = 
            LocationServices.getFusedLocationProviderClient(context)
        
        if (ActivityCompat.checkSelfPermission(
                context, 
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Use Geocoder to convert location to address
                    val geocoder = Geocoder(context, Locale.getDefault())
                    try {
                        val addresses: List<Address> = geocoder.getFromLocation(
                            location.latitude, location.longitude, 1
                        ) ?: emptyList()
                        
                        val location = if (addresses.isNotEmpty()) {
                            (addresses[0].locality + ", " + addresses[0].countryName)
                        } else {
                            "Unknown Location"
                        }
                        
                        callback(formattedDate, formattedTime, location)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback(formattedDate, formattedTime, "Location Error")
                    }
                } else {
                    callback(formattedDate, formattedTime, "Location Error")
                }
            }
        } else {
            callback(formattedDate, formattedTime, "Location Error")
        }
    }
} 