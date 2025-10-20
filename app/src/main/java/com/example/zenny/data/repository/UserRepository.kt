package com.example.zenny.data.repository

import com.example.zenny.data.AppDatabase
import com.example.zenny.data.entity.UserEntity

class UserRepository(private val db: AppDatabase) {
    private val dao = db.userDao()

    private suspend fun ensureUser(): UserEntity {
        val u = dao.getUser()
        return if (u == null) {
            val new = UserEntity()
            dao.upsert(new)
            new
        } else u
    }

    suspend fun saveName(name: String) { val u = ensureUser(); dao.upsert(u.copy(name = name)) }
    suspend fun getName(): String? = dao.getUser()?.name

    suspend fun saveDisplayedName(displayedName: String) { val u = ensureUser(); dao.upsert(u.copy(displayedName = displayedName)) }
    suspend fun getDisplayedName(): String? = dao.getUser()?.displayedName

    suspend fun saveEmail(email: String) { val u = ensureUser(); dao.upsert(u.copy(email = email)) }
    suspend fun getEmail(): String? = dao.getUser()?.email

    suspend fun saveProfileImagePath(path: String) { val u = ensureUser(); dao.upsert(u.copy(profileImagePath = path)) }
    suspend fun getProfileImagePath(): String? = dao.getUser()?.profileImagePath

    suspend fun saveDarkMode(isDarkMode: Boolean) { val u = ensureUser(); dao.upsert(u.copy(darkMode = isDarkMode)) }
    suspend fun isDarkMode(): Boolean = dao.getUser()?.darkMode ?: false

    suspend fun saveOnboardingComplete(isComplete: Boolean) { val u = ensureUser(); dao.upsert(u.copy(onboardingComplete = isComplete)) }
    suspend fun isOnboardingComplete(): Boolean = dao.getUser()?.onboardingComplete ?: false
}
