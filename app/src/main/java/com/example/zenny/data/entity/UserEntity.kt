package com.example.zenny.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Long = 1, // single-user app
    val name: String? = null,
    val displayedName: String? = null,
    val email: String? = null,
    val profileImagePath: String? = null,
    val darkMode: Boolean = false,
    val onboardingComplete: Boolean = false
)
