package com.example.exploreai.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("ExploreAIPrefs", Context.MODE_PRIVATE)

    fun isFirstTimeLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_TIME_LAUNCH, true)
    }

    fun setFirstTimeLaunchComplete() {
        sharedPreferences.edit().putBoolean(KEY_FIRST_TIME_LAUNCH, false).apply()
    }

    fun setDestination(destination: String){
        sharedPreferences.edit().putString(DESTINATION_STRING, destination).apply()
    }

    fun getDestination(): String? {
        return sharedPreferences.getString(DESTINATION_STRING,"San Diego, CA")
    }

    fun getLocation(): String? {
        return sharedPreferences.getString(LOCATION_STRING,"Palm Springs, CA")
    }

    fun setLocation(location: String){
        sharedPreferences.getString(LOCATION_STRING,location)
    }

    companion object {
        private const val KEY_FIRST_TIME_LAUNCH = "is_first_time_launch"
        private const val DESTINATION_STRING = "destination_string"
        private const val LOCATION_STRING = "location_string"
    }
}