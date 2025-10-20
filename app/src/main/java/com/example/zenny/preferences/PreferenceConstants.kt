package com.example.zenny.preferences


object PreferenceConstants {


    object Habits {
        const val PREFS_NAME = "ZennyHabitPrefs"
        const val HABITS_KEY = "habits"
        const val LAST_OPENED_DATE_KEY = "lastOpenedDate"
        const val HABIT_COUNT_KEY = "habitCount"
        const val LAST_HABIT_ID_KEY = "lastHabitId"
    }


    object Mood {
        const val PREFS_NAME = "ZennyMoodPrefs"
        const val MOOD_ENTRIES_KEY = "moodEntries"
        const val LAST_MOOD_DATE_KEY = "lastMoodDate"
    }


    object Hydration {
        const val PREFS_NAME = "ZennyHydrationPrefs"
        const val DAILY_GOAL_KEY = "dailyGoal"
        const val CURRENT_INTAKE_KEY = "currentIntake"
        const val GLASS_SIZE_KEY = "glassSize"
        const val REMINDER_INTERVAL_KEY = "reminderInterval"
        const val LAST_RESET_DATE_KEY = "lastResetDate"
    }


    object General {
        const val PREFS_NAME = "ZennyGeneralPrefs"
        const val FIRST_LAUNCH_KEY = "firstLaunch"
        const val ONBOARDING_COMPLETED_KEY = "onboardingCompleted"
        const val APP_VERSION_KEY = "appVersion"
        const val THEME_MODE_KEY = "themeMode"
    }
}