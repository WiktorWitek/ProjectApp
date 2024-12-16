package com.example.travelapp.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient


@Composable
fun AutoCompletePlacesComponent(onPlaceSelected: (LatLng?, String, String) -> Unit) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var predictions by remember { mutableStateOf(listOf<AutocompletePrediction>()) }
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }
    val focusRequester = remember { FocusRequester() }

    val keyboardController = LocalSoftwareKeyboardController.current


    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = searchQuery,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onPrimary
            ),
            singleLine = true,
            onValueChange = {
                searchQuery = it
                if (it.text.length > 2) {  // Uruchom wyszukiwanie po wpisaniu min. 3 znaków
                    searchPlaces(it.text, placesClient) { result ->
                        predictions = result
                    }
                } else {
                    predictions = emptyList()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(7.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(MaterialTheme.colorScheme.background)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(30.dp)
                )
                .focusRequester(focusRequester),
            placeholder = { Text("Enter location") },
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Wyświetlanie podpowiedzi (predykcji)
        if (predictions.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                predictions.forEach { prediction ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                keyboardController?.hide()

                                // miasto, kraj
                                val fullAddress = prediction.getFullText(null).toString()
                                val placeId = prediction.placeId

                                searchQuery = TextFieldValue(
                                    text = prediction.getFullText(null).toString(),
                                    selection = TextRange(prediction.getFullText(null).length)  // Kursor na końcu tekstu
                                )

                                // Wywołaj funkcję przekazującą lokalizację
                                val location = getLocationFromAddress(context, prediction.getFullText(null).toString())
                                onPlaceSelected(location, fullAddress, placeId)

                                // Ukryj predykcje
                                predictions = emptyList()

                                // Ustaw fokus na polu tekstowym
                                focusRequester.requestFocus()
                            }
                        ) {
                            Text(
                                text = prediction.getPrimaryText(null).toString(),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        } else if (searchQuery.text.isNotEmpty() && predictions.isEmpty() && !searchQuery.text.contains(",")) {
            Text(
                text = "No results found",
                style = TextStyle(MaterialTheme.colorScheme.onSecondary, fontSize = 16.sp),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


private fun searchPlaces(
    query: String,
    placesClient: PlacesClient,
    onResult: (List<AutocompletePrediction>) -> Unit
) {
    val request = FindAutocompletePredictionsRequest.builder()
        .setQuery(query)
        .setTypesFilter(listOf("locality", "administrative_area_level_3"))
        .build()

    placesClient.findAutocompletePredictions(request)
        .addOnSuccessListener { response ->
            onResult(response.autocompletePredictions)
        }
        .addOnFailureListener { exception ->
            Log.e("AutoComplete", "Place not found: ${exception.message}")
        }
}
