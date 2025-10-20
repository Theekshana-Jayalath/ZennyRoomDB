package com.example.zenny

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.zenny.data.DatabaseProvider
import com.example.zenny.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ZennyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize database early
        val db = DatabaseProvider.get(this)

        // Read dark mode from DB and set theme
        CoroutineScope(Dispatchers.IO).launch {
            val repo = UserRepository(db)
            val dark = repo.isDarkMode()
            launch(Dispatchers.Main) {
                AppCompatDelegate.setDefaultNightMode(
                    if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }
    }
}
