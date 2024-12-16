package com.example.travelapp.app

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.travelapp.screens.LoginScreen
import com.example.travelapp.screens.MainScreen
import com.example.travelapp.screens.NotificationScreen
import com.example.travelapp.screens.PlannerScreen
import com.example.travelapp.screens.ProfileScreen
import com.example.travelapp.screens.SignUpScreen
import com.example.travelapp.screens.TripDetailScreen
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AppNavigation(onThemeToggle: () -> Unit,
                  isDarkTheme: Boolean) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    val startDest: String = if (auth.currentUser != null) {
        "home"
    } else {
        "login"
    }


    NavHost(navController = navController, startDestination = startDest) {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("home") { MainScreen(navController) }
        composable("profile") {
            ProfileScreen(
                navController,
                isDarkTheme,
                onThemeToggle
            )
        }
        composable("tripDetails/{tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            TripDetailScreen(navController, tripId = tripId)
        }
        composable("notification") { NotificationScreen(navController) }
        composable("planner/{tripId}/{startDate}/{endDate}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            val startDate = backStackEntry.arguments?.getString("startDate")?.toLongOrNull()
            val endDate = backStackEntry.arguments?.getString("endDate")?.toLongOrNull()
            if (tripId != null && startDate != null && endDate != null) {
                PlannerScreen(
                    navController = navController,
                    tripId = tripId,
                    startDate = startDate,
                    endDate = endDate,
                    tasksByDay = mutableMapOf(),
                    onAddTask = { day, task -> Log.d("Planner", "Add task: $task for day $day") },
                    onDeleteTask = { day, task -> Log.d("Planner", "Delete task: $task for day $day") }
                )
            }
        }
    }
}