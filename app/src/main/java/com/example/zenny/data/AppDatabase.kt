package com.example.zenny.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.zenny.data.dao.HabitDao
import com.example.zenny.data.dao.HabitMetaDao
import com.example.zenny.data.dao.HydrationDao
import com.example.zenny.data.dao.MoodDao
import com.example.zenny.data.dao.UserDao
import com.example.zenny.data.entity.HabitEntity
import com.example.zenny.data.entity.HabitMetaEntity
import com.example.zenny.data.entity.HydrationStateEntity
import com.example.zenny.data.entity.MoodEntity
import com.example.zenny.data.entity.UserEntity

@Database(
    entities = [HabitEntity::class, HabitMetaEntity::class, HydrationStateEntity::class, MoodEntity::class, UserEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitMetaDao(): HabitMetaDao
    abstract fun hydrationDao(): HydrationDao
    abstract fun moodDao(): MoodDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zenny.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
