package com.example.zenny.preferences

import android.content.Context

class UserPreferences(context: Context) : PreferencesManager(context, "user_prefs") {

    companion object {
        private const val KEY_NAME = "key_name"
        private const val KEY_DISPLAYED_NAME = "key_displayed_name"
        private const val KEY_EMAIL = "key_email"
        private const val KEY_PROFILE_IMAGE_PATH = "key_profile_image_path"
        private const val KEY_DARK_MODE = "key_dark_mode"
        private const val KEY_ONBOARDING_COMPLETE = "key_onboarding_complete"
        const val ONBOARDING_COMPLETE = "onboarding_complete"
    }

    fun saveName(name: String) {
        saveString(KEY_NAME, name)
    }

    fun getName(): String? {
        return getString(KEY_NAME)
    }

    fun saveDisplayedName(displayedName: String) {
        saveString(KEY_DISPLAYED_NAME, displayedName)
    }

    fun getDisplayedName(): String? {
        return getString(KEY_DISPLAYED_NAME)
    }

    fun saveEmail(email: String) {
        saveString(KEY_EMAIL, email)
    }

    fun getEmail(): String? {
        return getString(KEY_EMAIL)
    }

    fun saveProfileImagePath(path: String) {
        saveString(KEY_PROFILE_IMAGE_PATH, path)
    }

    fun getProfileImagePath(): String? {
        return getString(KEY_PROFILE_IMAGE_PATH)
    }

    fun saveDarkMode(isDarkMode: Boolean) {
        saveBoolean(KEY_DARK_MODE, isDarkMode)
    }

    fun isDarkMode(): Boolean {
        return getBoolean(KEY_DARK_MODE, false) // Default to light mode
    }

    fun saveOnboardingComplete(isComplete: Boolean) {
        saveBoolean(KEY_ONBOARDING_COMPLETE, isComplete)
    }
    
    fun setOnboardingComplete(isComplete: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(ONBOARDING_COMPLETE, isComplete)
        editor.apply()
    }

    fun isOnboardingComplete(): Boolean {
        return getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }
}
