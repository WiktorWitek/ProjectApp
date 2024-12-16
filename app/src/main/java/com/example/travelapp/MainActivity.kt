package com.example.travelapp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.example.travelapp.app.AppNavigation
import com.example.travelapp.data.ThemePreferences
import com.example.travelapp.ui.theme.TravelAppTheme
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "api_key")
        }

        val themePreferences = ThemePreferences(applicationContext)

        setContent {
            val isDarkTheme = themePreferences.isDarkMode.collectAsState(initial = false)
            val scope = rememberCoroutineScope()

            App(
                isDarkTheme = isDarkTheme.value,
                onThemeToggle = { newTheme ->
                    scope.launch {
                        themePreferences.saveDarkModeSetting(newTheme)
                    }
                }
            )
        }
    }
}

@Composable
fun App(isDarkTheme: Boolean, onThemeToggle: (Boolean) -> Unit) {
    TravelAppTheme(darkTheme = isDarkTheme) {
        AppNavigation(
            onThemeToggle = { onThemeToggle(!isDarkTheme) },
            isDarkTheme = isDarkTheme,
        )
    }
}