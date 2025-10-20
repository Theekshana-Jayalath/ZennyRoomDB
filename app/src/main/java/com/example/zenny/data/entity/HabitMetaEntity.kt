package com.example.zenny.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_meta")
data class HabitMetaEntity(
    @PrimaryKey val id: Int = 1,
    val lastOpenedDate: String? = null
)
