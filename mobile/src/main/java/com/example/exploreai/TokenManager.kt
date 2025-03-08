package com.example.exploreai

import android.content.Context
import com.auth0.android.Auth0

// Create a TokenManager object to handle token operations
object TokenManager {
    private const val PREFS_NAME = "AuthPrefs"
    private const val KEY_ACCESS_TOKEN = "access_token"

    fun saveToken(context: Context, token: String) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun clearToken(context: Context) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().remove(KEY_ACCESS_TOKEN).apply()
    }
}