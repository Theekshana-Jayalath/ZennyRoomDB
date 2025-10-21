package com.example.zenny.data.repository

import com.example.zenny.MoodEntry
import com.example.zenny.data.AppDatabase
import com.example.zenny.data.entity.MoodEntity
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

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

    suspend fun getForDate(dateIso: String): List<MoodEntry> =
        dao.getForDate(dateIso).map { it.toDomain() }

    suspend fun getForDateRange(startIso: String, endIso: String): List<MoodEntry> =
        dao.getForDateRange(startIso, endIso).map { it.toDomain() }

    /**
     * Gets mood counts for the last 7 days (Mon..Sun index 0..6).
     */
    suspend fun getMoodDistributionForLast7Days(): Map<Int, Int> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE // YYYY-MM-DD
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(6)

        val moods = getForDateRange(startDate.format(formatter), endDate.format(formatter))

        // Initialize counts for all days of week (Mon..Sun -> 0..6)
        val counts = IntArray(7) { 0 }
        moods.forEach { entry ->
            try {
                val d = LocalDate.parse(entry.dateIso, formatter)
                val idx = (d.dayOfWeek.value - 1) // 1..7 => 0..6
                if (idx in 0..6) counts[idx]++
            } catch (_: Exception) { /* ignore parse errors */ }
        }
        return counts.mapIndexed { i, v -> i to v }.toMap()
    }

    private fun MoodEntity.toDomain() = MoodEntry(dateIso, emoji, note, timestamp, id)
    private fun MoodEntry.toEntity() = MoodEntity(dateIso, emoji, note, timestamp, id)
}
