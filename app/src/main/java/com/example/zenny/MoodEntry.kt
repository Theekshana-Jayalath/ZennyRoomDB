package com.example.zenny

import org.json.JSONObject

data class MoodEntry(
    val id: String,
    val dateIso: String,
    val emoji: String,
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis()
){
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("dateIso", dateIso)
        obj.put("emoji", emoji)
        obj.put("note", note)
        obj.put("timestamp", timestamp)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): MoodEntry {
            return MoodEntry(
                id = obj.optString("id"),
                dateIso = obj.optString("dateIso"),
                emoji = obj.optString("emoji"),
                note = obj.optString("note", null),
                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }
}

