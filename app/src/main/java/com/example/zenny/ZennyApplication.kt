package com.example.zenny

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.zenny.preferences.UserPreferences

class ZennyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize preferences
        val userPreferences = UserPreferences(this)

        // Set the theme based on the saved preference
        if (userPreferences.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
