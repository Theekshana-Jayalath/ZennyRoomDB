package com.example.zenny.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zenny.data.entity.HabitMetaEntity

@Dao
interface HabitMetaDao {
    @Query("SELECT * FROM habit_meta WHERE id = 1")
    suspend fun get(): HabitMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(meta: HabitMetaEntity)
}
