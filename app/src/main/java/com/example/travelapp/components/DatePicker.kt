package com.example.travelapp.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()

    val modifiedColorScheme = MaterialTheme.colorScheme.copy(
        onSurface = MaterialTheme.colorScheme.onBackground,
        primary = MaterialTheme.colorScheme.onBackground
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateRangeSelected(
                        Pair(
                            dateRangePickerState.selectedStartDateMillis,
                            dateRangePickerState.selectedEndDateMillis
                        )
                    )
                    onDismiss()
                },
            ) {
                Text("OK", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
    ) {
        MaterialTheme(colorScheme = modifiedColorScheme) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                DateRangePicker(
                    state = dateRangePickerState,
                    title = {
                        Text(
                            text = "Select date range",
                        )
                    },
                    showModeToggle = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                        .padding(16.dp)
                )
            }
        }
    }
}

// Funkcja pomocnicza do formatowania daty
fun formatDate(dateMillis: Long): String {
    val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(dateMillis))
}

fun formatDateRange(startDateMillis: Long?, endDateMillis: Long?): String {
    if (startDateMillis == null || endDateMillis == null) return "N/A"

    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val startDate = Date(startDateMillis)
    val endDate = Date(endDateMillis)

    return if (startDateMillis == endDateMillis) {
        dateFormat.format(startDate)
    } else {
        "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
    }
}