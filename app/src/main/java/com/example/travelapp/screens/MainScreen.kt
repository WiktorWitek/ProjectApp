package com.example.travelapp.screens


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.travelapp.app.TripNotificationWorker
import com.example.travelapp.components.BottomNavigationBar
import com.example.travelapp.components.FirstDialog
import com.example.travelapp.components.SecondDialog
import com.example.travelapp.components.TopToggleButtons
import com.example.travelapp.components.TripCard
import com.example.travelapp.components.formatDate
import com.example.travelapp.components.getCurrentLocation
import com.example.travelapp.data.FirebaseService.fetchTrips
import com.example.travelapp.data.TripData
import com.example.travelapp.ui.theme.roundFontFamily
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit


@Composable
fun MainScreen(navController: NavHostController) {
    val backgroundColor = MaterialTheme.colorScheme.background
    var showDialog by remember { mutableStateOf(false) }
    var searchedLocation by remember { mutableStateOf<LatLng?>(null) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var showNextDialog by remember { mutableStateOf(false) }

    // dane do bazy danych
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var placeId by remember { mutableStateOf("") }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }


    var trips by remember { mutableStateOf<List<TripData>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf("upcoming") } // Stan dla wybranego przycisku

    val context = LocalContext.current
    val activity = context as? Activity  // Bezpieczne rzutowanie na Activity
    var isLoading by remember { mutableStateOf(true) }

    // Aktualna data
    val currentDate = System.currentTimeMillis()
    val filteredTrips = when (selectedTab) {
        "upcoming" -> trips.filter { it.endDate != null && it.endDate > currentDate }
        "past" -> trips.filter { it.endDate != null && it.endDate <= currentDate }
        else -> emptyList()
    }

    var deleteDialog by remember { mutableStateOf(false) }
    var selectedTripId:String? by remember { mutableStateOf<String?>(null) }


    // funkcja do pobierania danych z firebase
    fun loadTrips() {
        isLoading = true
        fetchTrips(
            onSuccess = { fetchedTrips ->
                trips = fetchedTrips


                trips.forEach { trip ->
                    trip.startDate?.let { startDate ->
                        trip.city?.let { city ->
                            trip.country?.let { country ->
                                trip.id?.let { scheduleTripNotification(context, tripId = it, tripStartDate = trip.startDate, city = trip.city, country = trip.country) }
                            }
                        }
                    }
                }

                isLoading = false
            },
            onFailure = { exception ->
                errorMessage = exception.localizedMessage
                isLoading = false
            }
        )
    }


    LaunchedEffect(Unit) {
        activity?.let {
            // Asynchroniczne pobieranie lokalizacji tylko wtedy, gdy context to Activity
            getCurrentLocation(it) { location ->
                currentLocation = location
            }
        }
        loadTrips()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {

        TopToggleButtons(
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                selectedTab = tab
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .background(backgroundColor),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (isLoading) {
                item {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            } else {
                if (filteredTrips.isNotEmpty()) {
                    items(filteredTrips) { trip ->
                        trip.city?.let {
                            trip.country?.let { it1 ->
                                trip.placeId?.let { it2 ->
                                    TripCard(
                                        city = it,
                                        country = it1,
                                        startDate = trip.startDate?.let { formatDate(it) } ?: "N/A",
                                        endDate = trip.endDate?.let { formatDate(it) } ?: "N/A",
                                        placeId = it2,
                                        onClick = { navController.navigate("tripDetails/${trip.id}") },
                                        onLongPress = {
                                            selectedTripId = trip.id
                                            deleteDialog = true
                                                      },
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "No results found...",
                            fontFamily = roundFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 35.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally),
                        )
                    }
                }
            }
        }

    BottomNavigationBar(
        onProfileClick = { navController.navigate("profile") },
        onAddClick = { showDialog = true },
        onNotificationsClick = { navController.navigate("notification") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )

        if (deleteDialog) {
            AlertDialog(
                onDismissRequest = { deleteDialog = false },
                title = { Text(text = "Delete Trip") },
                text = { Text(text = "Are you sure you want to delete this trip?") },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedTripId?.let { tripId ->
                                deleteTrip(tripId,
                                    onSuccess = {
                                        deleteDialog = false
                                        loadTrips()
                                        Log.d("DeleteTrip", "Trip deleted successfully!")
                                    },
                                    onFailure = { e ->
                                        deleteDialog = false
                                        Log.e("DeleteTrip", "Error deleting trip: ${e.message}")
                                    })
                            }
                            deleteDialog = false
                        }
                    ) {
                        Text(text = "Delete")
                    }
                },
                dismissButton = {
                    Button(onClick = { deleteDialog = false }) {
                        Text(text = "Cancel")
                    }
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            )
        }


        if (showDialog) {
            FirstDialog(
                searchedLocation = searchedLocation,
                currentLocation = currentLocation,
                onLocationSelected = { location, fullAddress, selectedPlaceId ->
                    searchedLocation = location
                    val parts = fullAddress.split(", ")
                    city = parts.getOrNull(0)?: "Unknown"
                    country = parts.getOrNull(1)?: "Unknown"
                    placeId = selectedPlaceId
                    latitude = location?.latitude ?: 0.0
                    longitude = location?.longitude ?: 0.0

                },
                onClose = { showDialog = false },
                onNext = {
                    showDialog = false
                    showNextDialog = true
                }
                )
        }

        // Drugi dialog
        if (showNextDialog) {
            SecondDialog(onClose = { showNextDialog = false },
                city = city,
                country = country,
                longitude = longitude,
                latitude = latitude,
                placeID = placeId,
                onSaveComplete = {
                    loadTrips()
                }
            )
        }
    }
}

fun deleteTrip(tripId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("trips").document(tripId)
        .delete()
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}


@SuppressLint("MissingPermission")
fun getCurrentLocation(activity: Activity, onLocationReceived: (LatLng?) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        location?.let {
            onLocationReceived(LatLng(it.latitude, it.longitude))
        }
    }.addOnFailureListener { exception ->
        Log.e("LocationError", "Error fetching location: ${exception.message}")
        onLocationReceived(null)
    }
}


fun scheduleTripNotification(
    context: Context,
    tripId: String,
    tripStartDate: Long,
    city: String,
    country: String
) {
    val notificationId = tripStartDate.hashCode() // Unikalne ID na podstawie daty podróży

    // Oblicz czas powiadomienia: dzień przed `tripStartDate` o godzinie 9:00
    val calendar = java.util.Calendar.getInstance().apply {
        timeInMillis = tripStartDate
        add(java.util.Calendar.DATE, -1) // Dzień wcześniej
        set(java.util.Calendar.HOUR_OF_DAY, 9) // Godzina 9 rano
        set(java.util.Calendar.MINUTE, 0) // Minuta 0
        set(java.util.Calendar.SECOND, 0) // Sekunda 0
        set(java.util.Calendar.MILLISECOND, 0) // Milisekunda 0
    }
    val notificationTime = calendar.timeInMillis

    // Oblicz opóźnienie względem aktualnego czasu
    val currentTime = System.currentTimeMillis()
    val delay = notificationTime - currentTime

    if (delay > 0) {
        // Dane do przekazania do WorkManager
        val inputData = Data.Builder()
            .putString("tripId", tripId)
            .putString("city", city)
            .putString("country", country)
            .putInt("notificationId", notificationId)
            .build()

        // Utwórz zadanie WorkManager
        val notificationRequest = OneTimeWorkRequestBuilder<TripNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(notificationRequest)
        Log.d("Notification", "Scheduled notification for ${java.util.Date(notificationTime)}")
    } else {
        Log.w("Notification", "Notification time is in the past. Skipping...")
    }
}