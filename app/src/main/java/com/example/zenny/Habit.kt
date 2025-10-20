package com.example.zenny

data class Habit(
    var name: String, 
    var time: String, 
    var isCompleted: Boolean = false)