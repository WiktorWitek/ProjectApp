package com.example.travelapp.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class BaggageItem(
    val name: String,
    val isChecked: MutableState<Boolean> = mutableStateOf(false)
)