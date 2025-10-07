package com.example.zenny.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Base class for managing SharedPreferences operations in ZENNY app.
 * Provides common functionality for saving and retrieving data from SharedPreferences.
 */
abstract class PreferencesManager(
    private val context: Context,
    private val preferenceName: String
) {
    
    protected val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
    }
    
    protected val gson: Gson by lazy { Gson() }
    
    /**SharedPreferences*/
    protected fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    
    /**
     * Get a string value from SharedPreferences
     */
    protected fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }
    
    /**
     * Save an integer value to SharedPreferences
     */
    protected fun saveInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    
    /**
     * Get an integer value from SharedPreferences
     */
    protected fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
    
    /**
     * Save a boolean value to SharedPreferences
     */
    protected fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    /**
     * Get a boolean value from SharedPreferences
     */
    protected fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    
    /**
     * Save a long value to SharedPreferences
     */
    protected fun saveLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }
    
    /**
     * Get a long value from SharedPreferences
     */
    protected fun getLong(key: String, defaultValue: Long = 0L): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }
    
    /**
     * Save any object as JSON string to SharedPreferences
     */
    protected fun <T> saveObject(key: String, obj: T) {
        val json = gson.toJson(obj)
        saveString(key, json)
    }
    
    /**
     * Get an object from JSON string in SharedPreferences
     */
    protected inline fun <reified T> getObject(key: String, defaultValue: T? = null): T? {
        val json = getString(key) ?: return defaultValue
        return try {
            gson.fromJson(json, T::class.java)
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    /**
     * Save a list of objects as JSON string to SharedPreferences
     */
    protected fun <T> saveList(key: String, list: List<T>) {
        val json = gson.toJson(list)
        saveString(key, json)
    }
    
    /**
     * Get a list of objects from JSON string in SharedPreferences
     */
    protected inline fun <reified T> getList(key: String): MutableList<T> {
        val json = getString(key) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<T>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }
    
    /**
     * Remove a specific key from SharedPreferences
     */
    protected fun removeKey(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
    
    /**
     * Clear all data from SharedPreferences
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
    
    /**
     * Check if a key exists in SharedPreferences
     */
    protected fun containsKey(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}