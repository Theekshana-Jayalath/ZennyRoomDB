package com.example.zenny.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.zenny.data.entity.HydrationStateEntity

@Dao
interface HydrationDao {
    @Query("SELECT * FROM hydration_state WHERE id = 1")
    suspend fun get(): HydrationStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: HydrationStateEntity)

    @Update
    suspend fun update(state: HydrationStateEntity)
}
