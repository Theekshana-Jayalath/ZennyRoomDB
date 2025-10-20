package com.example.zenny.preferences

import android.content.Context
import com.example.zenny.Habit


class HabitPreferencesDemo(private val context: Context) {
    
    private val habitPreferences = HabitPreferences.getInstance(context)
    

    fun demonstrateBasicOperations() {
        println("🚀 ZENNY Habit Preferences Demo Started")
        println("=".repeat(50))
        

        println("📊 Current Habits Status:")
        println("   Has habits: ${habitPreferences.hasHabits()}")
        println("   Total habits: ${habitPreferences.getHabitCount()}")
        println("   Completed habits: ${habitPreferences.getCompletedHabitsCount()}")
        println("   Progress: ${habitPreferences.getProgressPercentage()}%")
        println()
        

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
        

        println("📂 Loading All Habits:")
        val loadedHabits = habitPreferences.loadHabits()
        loadedHabits.forEachIndexed { index, habit ->
            println("   $index. ${habit.name} - ${habit.time} - Completed: ${habit.isCompleted}")
        }
        println()
        

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
        

        println("📊 Updated Progress:")
        println("   Completed habits: ${habitPreferences.getCompletedHabitsCount()}")
        println("   Total habits: ${habitPreferences.getHabitCount()}")
        println("   Progress: ${habitPreferences.getProgressPercentage()}%")
        println()
        

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
        

        println("💾 Export/Import Demo:")
        val exportedJson = habitPreferences.exportHabitsAsJson()
        println("   Exported JSON (first 100 chars): ${exportedJson.take(100)}...")
        println()
        

        println("📅 Testing Date Reset:")
        val wasReset = habitPreferences.checkAndResetDailyProgress()
        println("   Was reset today: $wasReset")
        println("   Last opened date: ${habitPreferences.getLastOpenedDate()}")
        println()
        
        println("🎉 Demo completed successfully!")
        println("=".repeat(50))
    }
    

    fun demonstrateAdvancedOperations() {
        println("🔧 Advanced Operations Demo")
        println("-".repeat(30))
        

        val habits = habitPreferences.loadHabits()
        if (habits.isNotEmpty()) {
            val oldHabit = habits[0]
            val newHabit = Habit("${oldHabit.name} (Updated)", "10:00 AM", oldHabit.isCompleted)
            
            habitPreferences.updateHabit(oldHabit, newHabit)
            println("🔄 Updated habit: ${oldHabit.name} -> ${newHabit.name}")
        }
        

        val testJson = """[{"name":"Test Habit","time":"12:00 PM","isCompleted":false}]"""
        val importSuccess = habitPreferences.importHabitsFromJson(testJson)
        println("📥 Import test result: $importSuccess")
        
        println("✨ Advanced demo completed!")
    }
    

    fun cleanupDemo() {
        println("🧹 Cleaning up demo data...")
        habitPreferences.clearAllHabits()
        println("✅ Demo data cleared!")
    }
}