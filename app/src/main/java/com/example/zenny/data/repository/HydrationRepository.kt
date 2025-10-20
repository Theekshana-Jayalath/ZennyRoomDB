package com.example.zenny.data.repository

import com.example.zenny.data.AppDatabase
import com.example.zenny.data.entity.HydrationStateEntity

class HydrationRepository(private val db: AppDatabase) {
    private val dao = db.hydrationDao()

    suspend fun get(): HydrationStateEntity = dao.get() ?: HydrationStateEntity().also { dao.upsert(it) }
    suspend fun save(state: HydrationStateEntity) = dao.upsert(state)
}
