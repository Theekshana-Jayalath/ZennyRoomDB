package com.example.zenny.data.repository

import com.example.zenny.Habit
import com.example.zenny.data.AppDatabase
import com.example.zenny.data.entity.HabitEntity

class HabitRepository(private val db: AppDatabase) {
    private val dao = db.habitDao()

    suspend fun loadHabits(): MutableList<Habit> = dao.getAll().map { it.toDomain() }.toMutableList()

    suspend fun saveHabits(habits: List<Habit>) {
        dao.clear()
        dao.insertAll(habits.map { it.toEntity() })
    }

    suspend fun addHabit(habit: Habit) {
        dao.insert(habit.toEntity())
    }

    suspend fun updateHabit(oldHabit: Habit, newHabit: Habit) {
        // naive: delete old and insert new based on name+time match
        val current = dao.getAll()
        val match = current.firstOrNull { it.name == oldHabit.name && it.time == oldHabit.time }
        if (match != null) {
            dao.update(match.copy(name = newHabit.name, time = newHabit.time, isCompleted = newHabit.isCompleted))
        }
    }

    suspend fun removeHabit(habit: Habit) {
        val current = dao.getAll()
        val match = current.firstOrNull { it.name == habit.name && it.time == habit.time }
        if (match != null) dao.delete(match)
    }

    suspend fun updateHabitCompletion(habit: Habit, isCompleted: Boolean) {
        val current = dao.getAll()
        val match = current.firstOrNull { it.name == habit.name && it.time == habit.time }
        if (match != null) dao.update(match.copy(isCompleted = isCompleted))
    }

    suspend fun getProgressPercentage(): Int {
        val list = dao.getAll()
        if (list.isEmpty()) return 0
        val completed = list.count { it.isCompleted }
        return (completed * 100) / list.size
    }

    private fun HabitEntity.toDomain() = Habit(name = name, time = time, isCompleted = isCompleted)
    private fun Habit.toEntity() = HabitEntity(name = name, time = time, isCompleted = isCompleted)
}
