package com.example.zenny.preferences

import android.content.Context
import com.example.zenny.Habit

/**
 * Demonstration class showing how to use the new HabitPreferences system.
 * This class provides examples of all the available operations.
 */
class HabitPreferencesDemo(private val context: Context) {
    
    private val habitPreferences = HabitPreferences.getInstance(context)
    
    /**
     * Demonstrates basic habit operations
     */
    fun demonstrateBasicOperations() {
        println("🚀 ZENNY Habit Preferences Demo Started")
        println("=".repeat(50))
        
        // 1. Check if we have any existing habits
        println("📊 Current Habits Status:")
        println("   Has habits: ${habitPreferences.hasHabits()}")
        println("   Total habits: ${habitPreferences.getHabitCount()}")
        println("   Completed habits: ${habitPreferences.getCompletedHabitsCount()}")
        println("   Progress: ${habitPreferences.getProgressPercentage()}%")
        println()
        
        // 2. Add some sample habits
        println("➕ Adding Sample Habits:")
        val sampleHabits = listOf(
            Habit("Drink Water", "8:00 AM"),
            Habit("Morning Exercise", "6:30 AM"),
            Habit("Read Book", "9:00 PM"),
            Habit("Meditation", "7:00 AM")
        )
        
        sampleHabits.forEach { habit ->
            habitPreferences.addHabit(habit)
            println("   ✅ Added: ${habit.name} at ${habit.time}")
        }
        println()
        
        // 3. Load and display all habits
        println("📂 Loading All Habits:")
        val loadedHabits = habitPreferences.loadHabits()
        loadedHabits.forEachIndexed { index, habit ->
            println("   $index. ${habit.name} - ${habit.time} - Completed: ${habit.isCompleted}")
        }
        println()
        
        // 4. Mark some habits as completed
        println("✅ Marking Some Habits as Completed:")
        if (loadedHabits.isNotEmpty()) {
            habitPreferences.updateHabitCompletion(loadedHabits[0], true)
            println("   ✅ Marked '${loadedHabits[0].name}' as completed")
            
            if (loadedHabits.size > 2) {
                habitPreferences.updateHabitCompletion(loadedHabits[2], true)
                println("   ✅ Marked '${loadedHabits[2].name}' as completed")
            }
        }
        println()
        
        // 5. Show updated progress
        println("📊 Updated Progress:")
        println("   Completed habits: ${habitPreferences.getCompletedHabitsCount()}")
        println("   Total habits: ${habitPreferences.getHabitCount()}")
        println("   Progress: ${habitPreferences.getProgressPercentage()}%")
        println()
        
        // 6. Filter habits by completion status
        println("🔍 Filtering Habits:")
        val completedHabits = habitPreferences.getHabitsByCompletionStatus(true)
        val pendingHabits = habitPreferences.getHabitsByCompletionStatus(false)
        
        println("   Completed habits (${completedHabits.size}):")
        completedHabits.forEach { habit ->
            println("     ✅ ${habit.name} - ${habit.time}")
        }
        
        println("   Pending habits (${pendingHabits.size}):")
        pendingHabits.forEach { habit ->
            println("     ⏳ ${habit.name} - ${habit.time}")
        }
        println()
        
        // 7. Export habits as JSON
        println("💾 Export/Import Demo:")
        val exportedJson = habitPreferences.exportHabitsAsJson()
        println("   Exported JSON (first 100 chars): ${exportedJson.take(100)}...")
        println()
        
        // 8. Test date reset functionality
        println("📅 Testing Date Reset:")
        val wasReset = habitPreferences.checkAndResetDailyProgress()
        println("   Was reset today: $wasReset")
        println("   Last opened date: ${habitPreferences.getLastOpenedDate()}")
        println()
        
        println("🎉 Demo completed successfully!")
        println("=".repeat(50))
    }
    
    /**
     * Demonstrates advanced preference operations
     */
    fun demonstrateAdvancedOperations() {
        println("🔧 Advanced Operations Demo")
        println("-".repeat(30))
        
        // Update a habit
        val habits = habitPreferences.loadHabits()
        if (habits.isNotEmpty()) {
            val oldHabit = habits[0]
            val newHabit = Habit("${oldHabit.name} (Updated)", "10:00 AM", oldHabit.isCompleted)
            
            habitPreferences.updateHabit(oldHabit, newHabit)
            println("🔄 Updated habit: ${oldHabit.name} -> ${newHabit.name}")
        }
        
        // Test import functionality
        val testJson = """[{"name":"Test Habit","time":"12:00 PM","isCompleted":false}]"""
        val importSuccess = habitPreferences.importHabitsFromJson(testJson)
        println("📥 Import test result: $importSuccess")
        
        println("✨ Advanced demo completed!")
    }
    
    /**
     * Clean up demo data
     */
    fun cleanupDemo() {
        println("🧹 Cleaning up demo data...")
        habitPreferences.clearAllHabits()
        println("✅ Demo data cleared!")
    }
}