package com.example.travelapp.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.navigation.NavHostController
import com.example.travelapp.components.AddTaskDialog
import com.example.travelapp.data.Task
import com.example.travelapp.data.deleteTaskFromFirestore
import com.example.travelapp.data.loadTasksFromFirestore
import com.example.travelapp.data.saveTaskToFirestore
import com.example.travelapp.ui.theme.roundFontFamily
import java.time.Instant
import java.time.Period
import java.time.ZoneId

@SuppressLint("MutableCollectionMutableState")
@Composable
fun PlannerScreen(
    navController: NavHostController,
    tripId: String?,
    startDate: Long,
    endDate: Long,
    tasksByDay: MutableMap<Int, MutableList<Task>>,
    onAddTask: (Int, Task) -> Unit,
    onDeleteTask: (Int, Task) -> Unit
) {
    val context = LocalContext.current
    val numberOfDays = calculateDaysBetween(startDate, endDate)
    var selectedDay by remember { mutableStateOf(1) }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }


    var isDataLoaded by remember { mutableStateOf(false) }
    val tasksByDay = remember { mutableStateOf<MutableMap<Int, MutableList<Task>>>(mutableMapOf()) }


    LaunchedEffect(tripId) {
        tripId?.let {
            loadTasksFromFirestore(it, tasksByDay)
            isDataLoaded = true
        }
    }


    if (!isDataLoaded) {
        Text(
            text = "Loading...",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
        return
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {

            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(1.dp)
                    .padding(start = 4.dp, end = 4.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))

            // górny rząd przycisków
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items(numberOfDays) { day ->
                    Button(
                        onClick = { selectedDay = day + 1 },
                        colors = ButtonDefaults.buttonColors(
                            if (selectedDay == day + 1) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier
                            .height(56.dp)
                            .width(96.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        shape = RectangleShape
                    ) {
                        Text(
                            text = "Day ${day + 1}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = if (selectedDay == day + 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Separator
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                // Pojedyncza sekcja dla całego dnia
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24 * 80.dp) // Wysokość odpowiadająca 24 godzinom
                    ) {
                        // Oś czasu - LazyColumn automatycznie obsłuży przewijanie
                        (0..23).forEach { hour ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .offset(y = hour * 80.dp) // Pozycjonowanie w pionie
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(
                                        text = "${hour}:00",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier
                                            .width(50.dp) // Kolumna godzin
                                            .padding(end = 8.dp)
                                    )

                                    HorizontalDivider(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                        color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.weight(1f)) // Puste miejsce dla osi
                                }
                            }
                        }

                        // Zadania w danym dniu
                        tasksByDay.value[selectedDay]?.forEach { task ->
                            val startHourInt = task.startHour.split(":")[0].toInt()
                            val endHourInt = task.endHour.split(":")[0].toInt()
                            val taskHeight = (endHourInt - startHourInt) * 80.dp
                            val taskOffset = startHourInt * 80.dp

                            // Zadanie jako Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.835f)
                                    .offset(
                                        x = 59.dp,
                                        y = taskOffset + 16.dp
                                    ) // Pozycja początkowa zadania
                                    .height(taskHeight) // Wysokość zadania
                                    .background(
                                        MaterialTheme.colorScheme.onSecondary,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 4.dp,
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    .padding(8.dp)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                taskToDelete = task
                                                showDeleteDialog = true
                                            }
                                        )
                                    }

                            ) {
                                Text(
                                    text = "${task.startHour} - ${task.endHour}",
                                    fontFamily = roundFontFamily,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.align(Alignment.TopCenter)
                                )
                                Text(
                                    text = task.content,
                                    fontFamily = roundFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }



                Button(
                    onClick = { showAddTaskDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .align(Alignment.BottomCenter)
                        .padding(start = 4.dp, end = 4.dp),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary),
                    shape = RectangleShape
                ) {
                    Text(text = "Add Task", color = MaterialTheme.colorScheme.onPrimary)
                }

                // Dialog dodawania zadania
                if (showAddTaskDialog) {
                    AddTaskDialog(
                        onDismiss = { showAddTaskDialog = false },
                        onTaskAdded = { task ->
                            tasksByDay.value.getOrPut(selectedDay) { mutableListOf() }.add(task)
                            onAddTask(selectedDay, task)


                            // Dodanie do bazy danych
                            tripId?.let {
                                saveTaskToFirestore(
                                    tripId = it,
                                    day = selectedDay,
                                    task = task,
                                    context = context
                                )
                            }

                            // odswiezenie stanu taskByDay po dodaniu nowego
                            val updatedTasks = tasksByDay.value.toMutableMap()
                            val dayTasks = updatedTasks[selectedDay]?.toMutableList() ?: mutableListOf()
                            dayTasks.add(task)
                            updatedTasks[selectedDay] = dayTasks
                            tasksByDay.value = updatedTasks


                            showAddTaskDialog = false
                        }
                    )
                }

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text(text = "Delete Task", color = MaterialTheme.colorScheme.onPrimary) },
                            text = { Text(text = "Are you sure you want to delete this task?") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        taskToDelete?.let { task ->
                                            tripId?.let { id ->
                                                deleteTaskFromFirestore(
                                                    tripId = id,
                                                    day = selectedDay,
                                                    task = task,
                                                    onSuccess = {
                                                        tasksByDay.value[selectedDay]?.remove(task)
                                                        showDeleteDialog = false
                                                    },
                                                    onFailure = { e ->
                                                        showDeleteDialog = false
                                                        Log.e("DeleteTask", "Error deleting task: ${e.message}")
                                                    }
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Text(text = "Delete")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showDeleteDialog = false }) {
                                    Text(text = "Cancel")
                                }
                            }
                        )
                    }
    }
}



// Funkcja obliczająca liczbę dni między startDate i endDate
fun calculateDaysBetween(startDate: Long, endDate: Long): Int {
    val start = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate()
    val end = Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate()
    return Period.between(start, end).days + 1 // Dodajemy 1, aby uwzględnić oba dni
}

