package com.example.zenny

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import com.example.zenny.MoodEntry
import java.util.*

class MoodPreference(private val context: Context) {
    private val prefs = context.getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
    private val KEY = "moods_json"

    fun getAll(): List<MoodEntry> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<MoodEntry>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(MoodEntry.fromJson(obj))
            }
            list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveAll(list: List<MoodEntry>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    fun addOrUpdate(entry: MoodEntry) {
        val list = getAll().toMutableList()
        // Try replace by id
        val idxById = list.indexOfFirst { it.id == entry.id }
        if (idxById >= 0) {
            list[idxById] = entry
            saveAll(list)
            return
        }

        val idxByDate = list.indexOfFirst { it.dateIso == entry.dateIso }
        if (idxByDate >= 0) {
            list[idxByDate] = entry
        } else {
            list.add(entry)
        }
        saveAll(list)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun delete(id: String) {
        val list = getAll().filterNot { it.id == id }
        saveAll(list)
    }


    fun getForDate(dateIso: String): List<MoodEntry> {
        return getAll().filter { it.dateIso == dateIso }
    }


    fun getForMonth(year: Int, monthZeroBased: Int): List<MoodEntry> {
        val monthStr = if (monthZeroBased + 1 < 10) "0${monthZeroBased + 1}" else "${monthZeroBased + 1}"
        val prefix = "%04d-%s".format(year, monthStr)
        return getAll().filter { it.dateIso.startsWith(prefix) }
    }



    fun getForDateRange(start: org.threeten.bp.LocalDate, end: org.threeten.bp.LocalDate): List<MoodEntry> {
        val allMoods = getAll()
        val formatter = org.threeten.bp.format.DateTimeFormatter.ISO_LOCAL_DATE

        return allMoods.filter {
            try {
                val moodDate = org.threeten.bp.LocalDate.parse(it.dateIso, formatter)
                !moodDate.isBefore(start) && !moodDate.isAfter(end)
            } catch (e: Exception) {

                false
            }
        }
    }


}
