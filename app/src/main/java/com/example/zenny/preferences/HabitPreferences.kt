package com.example.zenny.preferences

import android.content.Context
import com.example.zenny.Habit
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages all habit-related SharedPreferences operations for the ZENNY app.
 * This class handles saving, loading, and managing habit data persistence.
 */
class HabitPreferences(context: Context) : PreferencesManager(context, PREFS_NAME) {
    
    companion object {
        private const val PREFS_NAME = "ZennyHabitPrefs"
        private const val HABITS_KEY = "habits"
        private const val LAST_OPENED_DATE_KEY = "lastOpenedDate"
        private const val HABIT_COUNT_KEY = "habitCount"
        private const val LAST_HABIT_ID_KEY = "lastHabitId"
        
        // Single instance for the app
        @Volatile
        private var INSTANCE: HabitPreferences? = null
        
        fun getInstance(context: Context): HabitPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HabitPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Save the complete list of habits to SharedPreferences
     */
    fun saveHabits(habits: List<Habit>) {
        saveList(HABITS_KEY, habits)
        saveInt(HABIT_COUNT_KEY, habits.size)
        
        // Log the save operation
        println("🔧 HabitPreferences: Saved ${habits.size} habits to SharedPreferences")
        habits.forEachIndexed { index, habit ->
            println("   $index. ${habit.name} - ${habit.time} - Completed: ${habit.isCompleted}")
        }
    }
    
    /**
     * Load all habits from SharedPreferences
     */
    fun loadHabits(): MutableList<Habit> {
        val habits = getList<Habit>(HABITS_KEY)
        println("🔧 HabitPreferences: Loaded ${habits.size} habits from SharedPreferences")
        habits.forEachIndexed { index, habit ->
            println("   $index. ${habit.name} - ${habit.time} - Completed: ${habit.isCompleted}")
        }
        return habits
    }
    
    /**
     * Add a new habit to the existing list
     */
    fun addHabit(habit: Habit) {
        val currentHabits = loadHabits()
        currentHabits.add(habit)
        saveHabits(currentHabits)
        println("🔧 HabitPreferences: Added new habit: ${habit.name}")
    }
    
    /**
     * Update an existing habit in the list
     */
    fun updateHabit(oldHabit: Habit, newHabit: Habit) {
        val currentHabits = loadHabits()
        val index = currentHabits.indexOfFirst { it.name == oldHabit.name && it.time == oldHabit.time }
        if (index != -1) {
            currentHabits[index] = newHabit
            saveHabits(currentHabits)
            println("🔧 HabitPreferences: Updated habit at index $index: ${newHabit.name}")
        }
    }
    
    /**
     * Remove a habit from the list
     */
    fun removeHabit(habit: Habit) {
        val currentHabits = loadHabits()
        val removed = currentHabits.removeIf { it.name == habit.name && it.time == habit.time }
        if (removed) {
            saveHabits(currentHabits)
            println("🔧 HabitPreferences: Removed habit: ${habit.name}")
        }
    }
    
    /**
     * Mark a habit as completed or uncompleted
     */
    fun updateHabitCompletion(habit: Habit, isCompleted: Boolean) {
        val currentHabits = loadHabits()
        val index = currentHabits.indexOfFirst { it.name == habit.name && it.time == habit.time }
        if (index != -1) {
            currentHabits[index].isCompleted = isCompleted
            saveHabits(currentHabits)
            println("🔧 HabitPreferences: Updated habit completion - ${habit.name}: $isCompleted")
        }
    }
    
    /**
     * Get the total count of habits
     */
    fun getHabitCount(): Int {
        return getInt(HABIT_COUNT_KEY, 0)
    }
    
    /**
     * Get the count of completed habits
     */
    fun getCompletedHabitsCount(): Int {
        return loadHabits().count { it.isCompleted }
    }
    
    /**
     * Get the progress percentage of completed habits
     */
    fun getProgressPercentage(): Int {
        val habits = loadHabits()
        if (habits.isEmpty()) return 0
        return (habits.count { it.isCompleted } * 100) / habits.size
    }
    
    /**
     * Check if it's a new day and reset habit completion status if needed
     */
    fun checkAndResetDailyProgress(): Boolean {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastOpenedDate = getString(LAST_OPENED_DATE_KEY)
        
        if (today != lastOpenedDate) {
            // It's a new day, reset all habit completion status
            val habits = loadHabits()
            habits.forEach { it.isCompleted = false }
            saveHabits(habits)
            saveString(LAST_OPENED_DATE_KEY, today)
            
            println("🔧 HabitPreferences: New day detected ($today), reset all habit completion status")
            return true
        }
        return false
    }
    
    /**
     * Get the last opened date
     */
    fun getLastOpenedDate(): String? {
        return getString(LAST_OPENED_DATE_KEY)
    }
    
    /**
     * Clear all habit data (useful for testing or reset functionality)
     */
    fun clearAllHabits() {
        removeKey(HABITS_KEY)
        removeKey(HABIT_COUNT_KEY)
        removeKey(LAST_HABIT_ID_KEY)
        println("🔧 HabitPreferences: Cleared all habit data")
    }
    
    /**
     * Get habits filtered by completion status
     */
    fun getHabitsByCompletionStatus(isCompleted: Boolean): List<Habit> {
        return loadHabits().filter { it.isCompleted == isCompleted }
    }
    
    /**
     * Check if any habits exist
     */
    fun hasHabits(): Boolean {
        return getHabitCount() > 0
    }
    
    /**
     * Export habits data as JSON string (useful for backup/restore)
     */
    fun exportHabitsAsJson(): String {
        val habits = loadHabits()
        return gson.toJson(habits)
    }
    
    /**
     * Import habits from JSON string (useful for backup/restore)
     */
    fun importHabitsFromJson(json: String): Boolean {
        return try {
            val habits = gson.fromJson(json, Array<Habit>::class.java).toList()
            saveHabits(habits)
            println("🔧 HabitPreferences: Successfully imported ${habits.size} habits from JSON")
            true
        } catch (e: Exception) {
            println("🔧 HabitPreferences: Failed to import habits from JSON: ${e.message}")
            false
        }
    }
}