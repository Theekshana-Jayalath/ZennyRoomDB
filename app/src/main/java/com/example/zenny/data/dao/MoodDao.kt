package com.example.zenny.data.dao

import androidx.room.*
import com.example.zenny.data.entity.MoodEntity

@Dao
interface MoodDao {
    @Query("SELECT * FROM moods ORDER BY timestamp DESC")
    suspend fun getAll(): List<MoodEntity>

    @Query("SELECT * FROM moods WHERE dateIso = :dateIso")
    suspend fun getForDate(dateIso: String): List<MoodEntity>

    @Query("SELECT * FROM moods WHERE dateIso BETWEEN :start AND :end ORDER BY dateIso ASC")
    suspend fun getForDateRange(start: String, end: String): List<MoodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(mood: MoodEntity)

    @Delete
    suspend fun delete(mood: MoodEntity)

    @Query("DELETE FROM moods")
    suspend fun clear()
}
