package com.example.travelapp.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplaneTicket
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.travelapp.components.BaggageDialog
import com.example.travelapp.components.MapComponent
import com.example.travelapp.components.PDFReader
import com.example.travelapp.components.formatDateRange
import com.example.travelapp.components.getPlacePhoto
import com.example.travelapp.components.loadTickets
import com.example.travelapp.data.FirebaseService.sendNotification
import com.example.travelapp.data.TripData
import com.example.travelapp.ui.theme.roundFontFamily
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.File


@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(navController: NavHostController, tripId: String?) {
    var trip by remember { mutableStateOf<TripData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }
    val formattedDateRange = formatDateRange(trip?.startDate, trip?.endDate)
    var isMapLoaded by remember { mutableStateOf(false) }
    var loc: LatLng

    // udostepnianie
    var shareDialog by remember { mutableStateOf(false) }
    var shareCode by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("Unknown User") }

    // bagaz
    var showBagDialog by remember { mutableStateOf(false) }

    // bilet
    var showTicketsDialog by remember { mutableStateOf(false) }
    var selectedTicketUri by remember { mutableStateOf<Uri?>(null) }
    var selectedTicketName by remember { mutableStateOf<String?>(null) }
    var showPDFDialog by remember { mutableStateOf(false) }


    // Obliczanie wysokości zdjęcia i panelu w procentach
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val imageHeight = screenHeight * 0.3f  // 40% wysokości ekranu
    val panelHeight = screenHeight * 0.7f  // 60% wysokości ekranu


    fun fetchUserDetails() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: "Unknown"
                        val lastName = document.getString("lastName") ?: "User"
                        userName = "$firstName $lastName"
                    } else {
                        userName = "User not found"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FETCH_USER", "Error fetching user details: ${exception.message}")
                    userName = "Error loading user"
                }
        } else {
            userName = "Not logged in"
        }
    }


    // Pobierz szczególy usera z firebase
    LaunchedEffect(Unit) {
        fetchUserDetails()
    }

    // Pobierz szczegóły podróży z Firebase
    LaunchedEffect(tripId) {
        if (tripId != null) {
            try {
                isLoading = true
                val db = FirebaseFirestore.getInstance()
                val document = db.collection("trips").document(tripId).get().await()
                val data = document.toObject(TripData::class.java)
                trip = data

                val placeId = trip?.placeId
                if (placeId != null) {
                    imageBitmap = getPlacePhoto(placeId, placesClient)
                } else {
                    errorMessage = "Place ID is null"
                }

            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        } else {
            errorMessage = "Invalid Trip ID"
            isLoading = false
        }
    }

    // Wyświetl szczegóły podróży
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text(
                text = errorMessage ?: "Error loading trip details",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        } else if (trip != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (imageBitmap != null) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    Image(
                        bitmap = imageBitmap!!.asImageBitmap(),
                        contentDescription = "City Landscape",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imageHeight + 20.dp)
                    )

                    // miasto, kraj
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imageHeight)
                            .padding(start = 16.dp, bottom = 16.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        IconButton(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                        Column {
                            trip!!.city?.let {
                                Text(
                                    text = it,
                                    color = Color.White,
                                    fontFamily = roundFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        shadow = Shadow(
                                            color = Color.Black.copy(alpha = 0.8f),
                                            offset = Offset(4f, 4f),
                                            blurRadius = 6f
                                        )
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Row {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "",
                                    tint = Color.White
                                )
                                Text(
                                    text = formattedDateRange,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        shadow = Shadow(
                                            color = Color.Black.copy(alpha = 0.8f),
                                            offset = Offset(4f, 4f),
                                            blurRadius = 6f
                                        )
                                    ),
                                    color = Color.White,
                                    fontFamily = roundFontFamily,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(top = imageHeight)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp
                                )
                            )
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 16.dp, vertical = 16.dp), // Marginesy wewnętrzne
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            trip?.notes?.let { notes ->
                                var expanded by remember { mutableStateOf(false) }
                                val notes = trip?.notes ?: ""
                                val descriptionText = notes.ifBlank { "No description yet..." }


                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 13.dp, top = 7.dp)
                                ) {
                                    Text(
                                        text = if (expanded) descriptionText else descriptionText.take(
                                            100
                                        ),
                                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )

                                    if (notes.length > 100 || notes.lines().size > 3) {
                                        TextButton(onClick = { expanded = !expanded }) {
                                            Text(
                                                text = if (expanded) "Show less" else "Read more",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    color = Color.Blue
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Sekcja z przyciskami
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { shareDialog = true },
                                    modifier = Modifier
                                        .height(90.dp)
                                        .weight(1f)
                                        .border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onSecondary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Share, contentDescription = "Share Icon")
                                        Text("Share")
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { showTicketsDialog = true },
                                    modifier = Modifier
                                        .height(90.dp)
                                        .weight(1f)
                                        .border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onSecondary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.AirplaneTicket,
                                            contentDescription = "Tickets Icon"
                                        )
                                        Text(
                                            "Tickets",
                                            fontFamily = roundFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { showBagDialog = true },
                                    modifier = Modifier
                                        .height(90.dp)
                                        .weight(1f)
                                        .border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onSecondary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Luggage,
                                            contentDescription = "Baggage Icon"
                                        )
                                        Text("Baggage")
                                    }
                                }
                            }
                        }

                        // Sekcja z Mapą
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                if (!isMapLoaded) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    LaunchedEffect(Unit) {
                                        kotlinx.coroutines.delay(200)
                                        isMapLoaded = true
                                    }
                                } else {
                                    loc = LatLng(trip!!.latitude!!, trip!!.longitude!!)
                                    MapComponent(
                                        searchedLocation = loc,
                                        currentLocation = loc,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(196.dp))
                        }
                    }
                    // PRZYCISK PLANNER
                    Button(
                        onClick = {
                            navController.navigate("planner/${trip!!.id}/${trip!!.startDate}/${trip!!.endDate}")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(196.dp)
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onSecondary)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Planner",
                                fontSize = 32.sp,
                                fontFamily = roundFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = "Arrow Forward",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                }
            }


            // OBSLUGA PRZYCISKU BAGGAGE
            if (showBagDialog) {
                BaggageDialog(
                    showDialog = { showBagDialog = it },
                    tripId = tripId,
                    context = context,
                )
            }


            // OBSLUGA PRZYCISKU TICKETS
            if (showTicketsDialog) {
                val tickets = remember { loadTickets(trip) }

                AlertDialog(
                    onDismissRequest = { showTicketsDialog = false },
                    title = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally),
                        ) {
                            Text(
                                "Tickets",
                                fontFamily = roundFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    text = {
                        if (tickets.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No tickets found...",
                                    fontFamily = roundFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                items(tickets) { ticketName ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        TextButton(
                                            onClick = {
                                                selectedTicketName = ticketName
                                                val ticketUriString = trip?.tickets?.get(ticketName)
                                                if (ticketUriString != null) {
                                                    selectedTicketUri = Uri.parse(ticketUriString)
                                                    showTicketsDialog = false
                                                    showPDFDialog = true
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "- ${ticketName}",
                                                fontFamily = roundFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontSize = 24.sp,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showTicketsDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }

            if (showPDFDialog && selectedTicketName != null) {
                val ticketFilePath = trip?.tickets?.get(selectedTicketName)
                val file = ticketFilePath?.let { File(it) }


                AlertDialog(
                    onDismissRequest = { showPDFDialog = false },
                    title = { Text("Ticket PDF") },
                    text = {
                        if (file != null && file.exists()) {
                            val fileUri = Uri.fromFile(File(ticketFilePath))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                PDFReader(pdfUri = fileUri)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "This ticket is not available. Please download it first.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Log.e("PDFReader", "Ticket file not found")
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showPDFDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }


            // OBSLUGA UDOSTEPNIANIA
            if (shareDialog) {
                AlertDialog(
                    onDismissRequest = { shareDialog = false },
                    title = {
                        Text(
                            text = "Share trip",
                            fontFamily = roundFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Enter the share code of the user you want to invite",
                                fontFamily = roundFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            TextField(
                                value = shareCode,
                                onValueChange = { shareCode = it },
                                placeholder = { Text(text = "Share Code") },
                                textStyle = TextStyle(MaterialTheme.colorScheme.onPrimary),
                                singleLine = true,
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    cursorColor = MaterialTheme.colorScheme.onPrimary,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                                    focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (tripId != null) {
                                    trip?.startDate?.let {
                                        trip?.endDate?.let { it1 ->
                                            trip?.city?.let { it2 ->
                                                trip?.country?.let { it3 ->
                                                    sendNotification(
                                                        tripId = tripId,
                                                        receiverId = shareCode,
                                                        startDate = it,
                                                        endDate = it1,
                                                        city = it2,
                                                        country = it3,
                                                        senderName = userName,
                                                        onSuccess = {
                                                            Log.d(
                                                                "SHARE",
                                                                "Notification sent successfully."
                                                            )
                                                            shareDialog = false
                                                        },
                                                        onFailure = { e ->
                                                            Log.e(
                                                                "SHARE",
                                                                "Error: ${e.message}"
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = Color.White
                            )
                        )
                        {
                            Text(text = "Share")
                        }
                    })
            }
        }
    }
}
