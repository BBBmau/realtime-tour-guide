package com.example.exploreai.utils

import android.content.Context
import com.auth0.android.result.Credentials

// Create a TokenManager object to handle token operations
object TokenManager {
    private const val PREFS_NAME = "AuthPrefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private lateinit var USER : String
    fun saveToken(context: Context, creds: Credentials) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        USER = creds.user.name.toString()
        sharedPrefs.edit().putString(KEY_ACCESS_TOKEN, creds.accessToken).apply()
    }

    fun getToken(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getUser(): String {
        return USER
    }

    fun clearToken(context: Context) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().remove(KEY_ACCESS_TOKEN).apply()
    }
}