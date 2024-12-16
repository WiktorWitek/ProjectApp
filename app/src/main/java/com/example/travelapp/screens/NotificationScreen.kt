package com.example.travelapp.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.travelapp.components.NotificationCard
import com.example.travelapp.components.formatDate
import com.example.travelapp.data.FirebaseService.fetchNotifications
import com.example.travelapp.data.NotificationData
import com.example.travelapp.ui.theme.roundFontFamily
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun NotificationScreen(navController: NavHostController) {
    val context = LocalContext.current
    val activity = context as? Activity
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val backgroundColor = MaterialTheme.colorScheme.background

    var notifications by remember { mutableStateOf<List<NotificationData>>(emptyList()) }


    fun loadNotifications() {
        isLoading = true
        fetchNotifications(
            onSuccess = { fetchedNotifications ->
                notifications = fetchedNotifications
                isLoading = false
            },
            onFailure = { exception ->
                errorMessage = exception.localizedMessage
                isLoading = false
            }
        )
    }

    LaunchedEffect(Unit) {
        loadNotifications()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Text(
            "Notifications",
            fontFamily = roundFontFamily,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 42.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(3.dp))

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
                if (notifications.isNotEmpty()) {
                    items(notifications) { notify ->
                        notify.city?.let {
                            notify.country?.let { it1 ->
                                notify.startDate?.let { it2 ->
                                    notify.endDate?.let { it3 ->
                                        notify.senderName?.let { it4 ->
                                            NotificationCard(
                                                city = it,
                                                country = it1,
                                                startDate = notify.startDate.let { formatDate(it2) },
                                                endDate = notify.endDate.let { formatDate(it3) },
                                                sender = it4,
                                                onAccept = {
                                                    val db = FirebaseFirestore.getInstance()
                                                    db.collection("trips").document(notify.tripId!!)
                                                        .update("sharedWith", FieldValue.arrayUnion(notify.receiverId))
                                                        .addOnSuccessListener {
                                                            Log.d("Notification", "Trip accepted successfully.")
                                                            // Usuń powiadomienie z bazy danych po zaakceptowaniu
                                                            db.collection("notifications").document(notify.id!!)
                                                                .delete()
                                                                .addOnSuccessListener {
                                                                    Log.d("Notification", "Notification deleted after acceptance.")
                                                                }
                                                            loadNotifications()
                                                        }
                                                        .addOnFailureListener { exception ->
                                                            Log.e("Notification", "Error accepting trip: ${exception.message}")
                                                        }
                                                },
                                                onDecline = {
                                                    val db = FirebaseFirestore.getInstance()

                                                    db.collection("notifications").document(notify.id!!)
                                                        .delete()
                                                        .addOnSuccessListener {
                                                            Log.d("Notification", "Notification deleted after decline.")
                                                            loadNotifications()
                                                        }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}