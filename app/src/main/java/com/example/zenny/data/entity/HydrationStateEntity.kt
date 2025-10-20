package com.example.zenny.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hydration_state")
data class HydrationStateEntity(
    @PrimaryKey val id: Int = 1,
    val intake: Int = 0,
    val goal: Int = 0,
    val glassSize: Int = 0,
    val wakingHours: Int = 8,
    val date: String = "",
    val remindersEnabled: Boolean = false,
    val reminderInterval: Long = 0,
    val reminderPos: Int = 0,
    val reminderEndTime: Long = 0
)
