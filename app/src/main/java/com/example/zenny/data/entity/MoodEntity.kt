package com.example.zenny.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moods")
data class MoodEntity(
    @PrimaryKey val dateIso: String,
    val emoji: String,
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val id: String
)
