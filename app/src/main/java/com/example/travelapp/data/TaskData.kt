package com.example.travelapp.data

import androidx.compose.ui.graphics.Color


data class Task(
    val startHour: String,
    val endHour: String,
    val content: String,
    val color: Color = Color.Blue
)