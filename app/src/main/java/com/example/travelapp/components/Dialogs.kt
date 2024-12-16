package com.example.travelapp.components

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AirplaneTicket
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.travelapp.data.BagDataStore
import com.example.travelapp.data.BaggageItem
import com.example.travelapp.data.FirebaseService.saveTrip
import com.example.travelapp.data.Task
import com.example.travelapp.ui.theme.roundFontFamily
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


@Composable
fun FirstDialog(searchedLocation: LatLng?,
                currentLocation: LatLng?,
                onLocationSelected: (LatLng?, String, String) -> Unit,
                onClose: () -> Unit,
                onNext: () -> Unit
) {
    Dialog(onDismissRequest = { onClose() }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 12.dp,
            modifier = Modifier.size(600.dp, 800.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 35.dp)
                ) {
                    Text(
                        text = "Search Location",
                        fontFamily = roundFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AutoCompletePlacesComponent { location, fullAddress, placeId ->
                        onLocationSelected(location, fullAddress, placeId)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))) {
                        MapComponent(
                            searchedLocation = searchedLocation,
                            currentLocation = currentLocation
                        )
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    // Dodany przycisk "Next" do otwarcia drugiego dialogu
                    Button(
                        onClick = {
                            if (searchedLocation != null) {
                                onNext()
                            }
                        },  // Zmiana stanu dla drugiego dialogu
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(text = "Next")
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun SecondDialog(
    latitude: Double,
    longitude: Double,
    city: String,
    country: String,
    placeID: String,
    onClose: () -> Unit,
    onSaveComplete: () -> Unit
) {
    var showBaggageDialog by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    // daty
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    // bagaz
    var baggageItem by remember { mutableStateOf("") }
    var baggageList by remember { mutableStateOf(mutableStateListOf<String>()) }

    // notatki
    var notes by remember { mutableStateOf("") }

    // bilet
    val ticket = remember { HashMap<String, String>() }

    var showTicketDialog by remember { mutableStateOf(false) }

    val saveResult = remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }


    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 8.dp,
            modifier = Modifier.size(600.dp, 800.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 30.dp, bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(25.dp)  // odstep miedzy elementami

            ) {
                Text(
                    text = "Search Location",
                    fontFamily = roundFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(2.dp))

                // WYBOR DATY
                Column {
                    Row {
                        Text(
                            text = "Choose a date range",
                            fontFamily = roundFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Add Baggage",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(
                        onClick = { showDateRangePicker = true },
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add Date",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                if (showDateRangePicker) {
                    DateRangePickerModal(
                        onDateRangeSelected = { range ->
                            startDate = range.first
                            endDate = range.second
                        },
                        onDismiss = { showDateRangePicker = false }
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .height(24.dp)
                        .fillMaxWidth()
                )
                // BAGAZ
                Column {
                    Row {
                        Text(
                            text = "Add baggage",
                            fontFamily = roundFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.Luggage,
                            contentDescription = "Add Baggage",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(
                        onClick = { showBaggageDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add Baggage",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                if (showBaggageDialog) {
                    Dialog(onDismissRequest = { showBaggageDialog = false }) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(600.dp, 400.dp),
                            color = MaterialTheme.colorScheme.background

                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Add baggage",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = baggageItem,
                                        onValueChange = { baggageItem = it },
                                        label = { Text("Add an element") },
                                        textStyle = TextStyle(
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontSize = 16.sp
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(7.dp)
                                            .clip(RoundedCornerShape(30.dp))
                                            .background(MaterialTheme.colorScheme.background)
                                            .border(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                shape = RoundedCornerShape(30.dp)
                                            ),
                                        singleLine = true,
                                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                            focusedBorderColor = Color.Transparent,
                                            cursorColor = MaterialTheme.colorScheme.onPrimary,
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                            unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary
                                        )
                                    )

                                    Spacer(Modifier.height(5.dp))

                                    IconButton(
                                        onClick = {
                                            if (baggageItem.isNotBlank()) {
                                                baggageList.add(baggageItem)
                                                baggageItem = ""
                                            }
                                        },
                                        modifier = Modifier.size(70.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }

                                Spacer(Modifier.height(5.dp))

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Start)
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    LazyColumn {
                                        items(baggageList) { item ->
                                            ClickableText(
                                                text = AnnotatedString("- $item"),
                                                modifier = Modifier
                                                    .padding(4.dp)
                                                    .fillMaxWidth(),
                                                style = TextStyle(
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                ),
                                                onClick = { baggageList.remove(item) }
                                            )
                                        }
                                    }
                                }
                                Button(
                                    onClick = { showBaggageDialog = false },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text(text = "OK")
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .height(24.dp)
                        .fillMaxWidth()
                )

                Column {
                    Row {
                        Text(
                            text = "Add tickets",
                            fontFamily = roundFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.AirplaneTicket,
                            contentDescription = "Add Baggage",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(
                        onClick = { showTicketDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add Date",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                if (showTicketDialog) {
                    AddTicketDialog(
                        onDismiss = { showTicketDialog = false },
                        onTicketAdded = { ticket.putAll(it)
                        }
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .height(24.dp)
                        .fillMaxWidth()
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        Text(
                            text = "Description",
                            fontFamily = roundFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Add Baggage",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Enter description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimary),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            cursorColor = MaterialTheme.colorScheme.onPrimary,
                            focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                            focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSecondary
                        ),
                    )
                }


                Spacer(modifier = Modifier.weight(1f))

                Button(onClick = {
                    if (startDate != null && endDate != null) {
                        saveTrip(
                            city,
                            country,
                            startDate,
                            endDate,
                            baggageList,
                            notes,
                            latitude,
                            longitude,
                            ticket,
                            placeID,
                            onSuccess = {
                                onSaveComplete()
                                onClose()
                                saveResult.value = "Data saved successfully"
                            },
                            onFailure = {saveResult.value = "Data saving failed"}
                        )
                    } else {
                        saveResult.value = "Choose dates"
                    }

                },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
                ) {
                    Text(text = "Save")
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState)
    }
    LaunchedEffect(saveResult.value) {
        if (saveResult.value.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = saveResult.value,
                duration = SnackbarDuration.Short
            )
            saveResult.value = ""
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTicketDialog(onDismiss: () -> Unit, onTicketAdded: (HashMap<String, String>) -> Unit) {
    var ticketName by remember { mutableStateOf("") }
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }

    // hashmapa biletow
    val ticketMap = remember { HashMap<String, String>() }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedPdfUri = uri
        }
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Ticket",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    )

                // TextField do wpisania nazwy biletu
                OutlinedTextField(
                    value = ticketName,
                    onValueChange = { ticketName = it },
                    label = { Text("Ticket Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(30.dp)
                        ),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSecondary
                    )
                )

                // Przycisk do wyboru pliku PDF
                Button(
                    onClick = { pdfPickerLauncher.launch("application/pdf") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
                ) {
                    Text(text = "Attach PDF")
                }

                // Przycisk "Add Ticket"
                val context = LocalContext.current
                Button(
                    onClick = {
                        if (ticketName.isNotBlank() && selectedPdfUri != null) {
                            val fileName = "$ticketName.pdf"
                            val filePath = saveFileToInternalStorage(context, selectedPdfUri!!, fileName)

                            if (filePath != null) {
                                ticketMap[ticketName] = filePath

                                onTicketAdded(ticketMap)

                                ticketName = ""
                                selectedPdfUri = null

                                onDismiss()
                            } else {
                                Log.e("AddTicketDialog", "Failed to save PDF to internal storage")
                            }
                        }
                    },
                    enabled = ticketName.isNotBlank() && selectedPdfUri != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
                ) {
                    Text(text = "Add Ticket", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}



@Composable
fun BaggageDialog(
    showDialog: (Boolean) -> Unit,
    tripId: String?,
    context: Context
) {
    val baggageList = remember { mutableStateListOf<BaggageItem>() }
    val scope = rememberCoroutineScope()
    val dataStore = BagDataStore(context)
    var showEditDialog by remember { mutableStateOf(false) }


    LaunchedEffect(tripId) {
        if (tripId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("trips").document(tripId)
                .get()
                .addOnSuccessListener { document ->
                    val baggage = document.get("baggage") as? List<String>
                    baggage?.let {
                        baggageList.clear()
                        baggageList.addAll(it.map { item -> BaggageItem(item, mutableStateOf(false)) })

                        scope.launch {
                            dataStore.loadBaggageState(context, baggageList)
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("BaggageDialog", "Error fetching baggage")
                }
        }
    }

    AlertDialog(
        onDismissRequest = { showDialog(false) },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Baggage", fontFamily = roundFontFamily, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimary, fontSize = 24.sp)
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Baggage", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
        text = {
            if (baggageList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Baggage is empty...",
                        fontFamily = roundFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary
                        )
                }
            } else {
            Column {
                baggageList.forEach { baggageItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        scope.launch {
                                            baggageList.remove(baggageItem)
                                            updateBaggageInDatabase(
                                                tripId,
                                                baggageList.map { it.name })
                                        }
                                    }
                                )

                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = baggageItem.name, fontFamily = roundFontFamily, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimary)
                        Checkbox(
                            checked = baggageItem.isChecked.value,
                            onCheckedChange = { isChecked ->
                                baggageItem.isChecked.value = isChecked
                                scope.launch {
                                    dataStore.saveBaggageState(context, baggageList)
                                }
                            }
                        )
                    }
                }
                }
            }
        },
        confirmButton = {
            Button(onClick = { showDialog(false) }) {
                Text("Done")
            }
        }
    )
    if (showEditDialog) {
        EditBaggageDialog(
            onDismiss = { showEditDialog = false },
            onBaggageAdded = { newBaggage ->
                // Scal istniejące i nowe dane
                val mergedBaggage = baggageList.map { it.name } + newBaggage
                val db = FirebaseFirestore.getInstance()

                tripId?.let {
                    db.collection("trips").document(it)
                        .update("baggage", mergedBaggage)
                        .addOnSuccessListener {
                            Log.d("SecondDialog", "Baggage updated successfully")
                            // Zaktualizuj stan listy w pierwszym dialogu
                            baggageList.clear()
                            baggageList.addAll(mergedBaggage.map { item -> BaggageItem(item, mutableStateOf(false)) })
                        }
                        .addOnFailureListener { e ->
                            Log.e("SecondDialog", "Error updating baggage: ${e.message}")
                        }
                }
            }
        )
    }
}

// Funkcja aktualizująca bagaż w bazie danych
private fun updateBaggageInDatabase(tripId: String?, updatedBaggage: List<String>) {
    if (tripId != null) {
        val db = FirebaseFirestore.getInstance()
        db.collection("trips").document(tripId)
            .update("baggage", updatedBaggage)
            .addOnSuccessListener {
                Log.d("BaggageDialog", "Baggage updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("BaggageDialog", "Error updating baggage: ${e.message}")
            }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBaggageDialog(onDismiss: () -> Unit, onBaggageAdded: (List<String>) -> Unit) {
    var newItem by remember { mutableStateOf("") }
    val newItemsList = remember { mutableStateListOf<String>() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Add Baggage Items", fontFamily = roundFontFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)

                // Pole tekstowe do dodawania nowych elementów
                OutlinedTextField(
                    value = newItem,
                    onValueChange = { newItem = it },
                    label = { Text("Baggage Item") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = MaterialTheme.colorScheme.onPrimary,
                        focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                        focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSecondary
                    )
                )

                // Przycisk dodawania do listy
                TextButton(onClick = {
                    if (newItem.isNotBlank()) {
                        newItemsList.add(newItem)
                        newItem = ""
                    }
                }) {
                    Text("Add to List", color = MaterialTheme.colorScheme.onPrimary)
                }

                // Wyświetl nowo dodane elementy
                LazyColumn {
                    items(newItemsList) { item ->
                        Text(text = item, fontFamily = roundFontFamily, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                // Przyciski zapisu i anulowania
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp).background(MaterialTheme.colorScheme.background))
                    TextButton(onClick = {
                        onBaggageAdded(newItemsList)
                        onDismiss()
                    }) {
                        Text("Save", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}


// PLANNERY
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onTaskAdded: (Task) -> Unit
) {
    var startHour by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(1) }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = {
                if (startHour < endHour) {
                    val task = Task(
                        startHour = "$startHour:00",
                        endHour = "$endHour:00",
                        content = content
                    )
                    onTaskAdded(task)
                    onDismiss()
                }
            }) {
                Text("Add Task", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        text = {
            Column {
                Text(
                    text = "Add New Task",
                    fontSize = 24.sp,
                    fontFamily = roundFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                DropdownMenuComponent(
                    label = "Start Hour",
                    value = startHour,
                    onValueChange = { startHour = it }
                )
                DropdownMenuComponent(
                    label = "End Hour",
                    value = endHour,
                    onValueChange = { endHour = it }
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Task Content") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = MaterialTheme.colorScheme.onPrimary,
                        focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                        focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSecondary
                    )
                )
            }
        }
    )
}


@Composable
fun DropdownMenuComponent(label: String, value: Int, onValueChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Select ${label}: ${value}:00 ", fontFamily = roundFontFamily, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        TextButton(onClick = { expanded = true }) {
            Text(text = "Select", color = MaterialTheme.colorScheme.onPrimary)
        }


        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (0..23).forEach { hour ->
                DropdownMenuItem(
                    text = { Text("$hour:00", color = MaterialTheme.colorScheme.onPrimary) },
                    onClick = {
                        onValueChange(hour)
                        expanded = false
                    }
                )
            }
        }
    }
}