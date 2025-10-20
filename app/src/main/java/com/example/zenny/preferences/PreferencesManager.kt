package com.example.zenny.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


abstract class PreferencesManager(
    private val context: Context,
    private val preferenceName: String
) {
    
    protected val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
    }
    
    protected val gson: Gson by lazy { Gson() }
    

    protected fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    

    protected fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }
    

    protected fun saveInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    

    protected fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
    

    protected fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    

    protected fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    

    protected fun saveLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }
    

    protected fun getLong(key: String, defaultValue: Long = 0L): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }
    

    protected fun <T> saveObject(key: String, obj: T) {
        val json = gson.toJson(obj)
        saveString(key, json)
    }
    

    protected inline fun <reified T> getObject(key: String, defaultValue: T? = null): T? {
        val json = getString(key) ?: return defaultValue
        return try {
            gson.fromJson(json, T::class.java)
        } catch (e: Exception) {
            defaultValue
        }
    }
    

    protected fun <T> saveList(key: String, list: List<T>) {
        val json = gson.toJson(list)
        saveString(key, json)
    }
    

    protected inline fun <reified T> getList(key: String): MutableList<T> {
        val json = getString(key) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<T>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }
    

    protected fun removeKey(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
    

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
    

    protected fun containsKey(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}