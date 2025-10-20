package com.example.zenny.data.dao

import androidx.room.*
import com.example.zenny.data.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("UPDATE users SET darkMode = :dark WHERE id = 1")
    suspend fun setDarkMode(dark: Boolean)

    @Query("UPDATE users SET onboardingComplete = :done WHERE id = 1")
    suspend fun setOnboardingComplete(done: Boolean)
}
