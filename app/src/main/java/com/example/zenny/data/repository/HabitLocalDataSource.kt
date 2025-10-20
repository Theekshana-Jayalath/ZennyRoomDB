package com.example.zenny.data.repository

import com.example.zenny.Habit
import com.example.zenny.data.AppDatabase
import com.example.zenny.data.entity.HabitMetaEntity
import java.text.SimpleDateFormat
import java.util.*

class HabitLocalDataSource(private val db: AppDatabase) {
    private val habitRepo = HabitRepository(db)
    private val metaDao = db.habitMetaDao()

    suspend fun saveHabits(habits: List<Habit>) = habitRepo.saveHabits(habits)
    suspend fun loadHabits(): MutableList<Habit> = habitRepo.loadHabits()
    suspend fun addHabit(habit: Habit) = habitRepo.addHabit(habit)
    suspend fun updateHabit(oldHabit: Habit, newHabit: Habit) = habitRepo.updateHabit(oldHabit, newHabit)
    suspend fun removeHabit(habit: Habit) = habitRepo.removeHabit(habit)
    suspend fun updateHabitCompletion(habit: Habit, isCompleted: Boolean) = habitRepo.updateHabitCompletion(habit, isCompleted)
    suspend fun getProgressPercentage(): Int = habitRepo.getProgressPercentage()

    suspend fun checkAndResetDailyProgress(): Boolean {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val meta = metaDao.get()
        val lastOpened = meta?.lastOpenedDate
        return if (today != lastOpened) {
            val habits = habitRepo.loadHabits().map { it.copy(isCompleted = false) }
            habitRepo.saveHabits(habits)
            metaDao.upsert(HabitMetaEntity(id = 1, lastOpenedDate = today))
            true
        } else false
    }
}
