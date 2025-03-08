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

    companion object {
        private const val KEY_FIRST_TIME_LAUNCH = "is_first_time_launch"
    }
}