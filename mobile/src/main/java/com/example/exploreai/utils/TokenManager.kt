package com.example.exploreai.utils

import android.content.Context
import com.auth0.android.result.Credentials

// Create a TokenManager object to handle token operations
object TokenManager {
    private const val PREFS_NAME = "AuthPrefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_USER_NAME = "user_name"

    fun saveToken(context: Context, creds: Credentials) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, creds.accessToken)
            .putString(KEY_USER_NAME, creds.user.name.toString())
            .apply()
    }

    fun getToken(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getUser(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_USER_NAME, null)
    }

    fun clearToken(context: Context) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().remove(KEY_ACCESS_TOKEN).apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        
        // If token exists, user is logged in
        return accessToken != null
    }
}