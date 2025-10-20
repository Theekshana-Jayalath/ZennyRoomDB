package com.example.zenny.data.repository

import com.example.zenny.MoodEntry
import com.example.zenny.data.AppDatabase
import com.example.zenny.data.entity.MoodEntity

class MoodRepository(private val db: AppDatabase) {
    private val dao = db.moodDao()

    suspend fun getAll(): List<MoodEntry> = dao.getAll().map { it.toDomain() }

    suspend fun addOrUpdate(entry: MoodEntry) {
        dao.upsert(entry.toEntity())
    }

    suspend fun delete(id: String) {
        val existing = dao.getAll().firstOrNull { it.id == id } ?: return
        dao.delete(existing)
    }

    suspend fun getForDate(dateIso: String): List<MoodEntry> = dao.getForDate(dateIso).map { it.toDomain() }

    suspend fun getForDateRange(startIso: String, endIso: String): List<MoodEntry> =
        dao.getForDateRange(startIso, endIso).map { it.toDomain() }

    private fun MoodEntity.toDomain() = MoodEntry(id, dateIso, emoji, note, timestamp)
    private fun MoodEntry.toEntity() = MoodEntity(id, dateIso, emoji, note, timestamp)
}
