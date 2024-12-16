@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.travelapp.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person2
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.travelapp.R
import com.example.travelapp.ui.theme.roundFontFamily
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine


@Composable
fun NormalTextComponent(value: String) {
    Text(
        text = value,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal
        ), color = MaterialTheme.colorScheme.onPrimary,
        textAlign = TextAlign.Center
    )
}


@Composable
fun HeaderText(value: String) {
    Text(
        text = value,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(),
        style = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal
        ), color = MaterialTheme.colorScheme.onPrimary,
        textAlign = TextAlign.Center
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormalTextField(labelValue: String, painterValue: Painter, textState: MutableState<String>) {

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(30.dp)
            ),

        value = textState.value,
        onValueChange = { newText ->
            textState.value = newText
        },
        placeholder = { Text(labelValue) },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text
        ),
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary
        ),
        leadingIcon = {
            Icon(
                painter = painterValue,
                contentDescription = ""
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(labelValue: String, painterValue: Painter, passwordState: MutableState<String>) {
    val passwordVisible = remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(30.dp)
            ),

        value = passwordState.value,
        onValueChange = { newText ->
            passwordState.value = newText
        },
        placeholder = { Text(labelValue) },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password
        ),
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary
        ),
        leadingIcon = {
            Icon(
                painter = painterValue,
                contentDescription = ""
            )
        },
        trailingIcon = {
            val iconImage = if(passwordVisible.value) {
                Icons.Filled.Visibility
            } else {
                Icons.Filled.VisibilityOff
            }
            
            var description = if(passwordVisible.value) {
                stringResource(id = R.string.hide_password)
            } else {
                stringResource(id = R.string.show_password)
            }
            
            IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                Icon(imageVector = iconImage, contentDescription = description)
            }
            
        },

        visualTransformation = if(passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation()

    )
}


@Composable
fun ButtonComponent(value: String, onClick: () -> Unit) {
    Button(onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(48.dp),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(Color.Transparent)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .heightIn(48.dp)
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(50.dp),
            ),
        contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RoundButton(onClick: () -> Unit, modifier: Modifier) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.size(60.dp)
    ) {
        Text(text = "+", style = MaterialTheme.typography.headlineLarge)
    }
}


@Composable
fun DividerTextComponent() {
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
            color = MaterialTheme.colorScheme.onPrimary,
            thickness = 1.dp)
        
        Text(text = stringResource(id = R.string.or),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(8.dp))

        HorizontalDivider(modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
            color = MaterialTheme.colorScheme.onPrimary,
            thickness = 1.dp)
    }
}


@Composable
fun ClickableTextComponent(normal: String,
                           clickable: String,
                           navController: NavHostController,
                           onClickAction: (() -> Unit)? = null) {
    val annotatedText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp)) {
            append(normal)
        }
        withStyle(style = SpanStyle(color = Color(0xFF03DAC6), fontSize = 16.sp)) {
            pushStringAnnotation(tag = "login", annotation = "login")
            append(clickable)
            pop()
        }
    }
    ClickableText(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        style = TextStyle(
            fontSize = 21.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            textAlign = TextAlign.Center,
        ),
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "login", start = offset, end = offset)
                .firstOrNull()?.let {
                    onClickAction?.invoke()
                }
        }
    )
}


@Composable
fun FYPclickableText() {
    val annotatedText = buildAnnotatedString {
        withStyle(style = SpanStyle(Color(0xFF03DAC6), fontSize = 16.sp)) {
            pushStringAnnotation(tag = "login", annotation = "login")
            append("Forgot your password?")
            pop()
        }
    }
    ClickableText(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        style = TextStyle(
            fontSize = 21.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            textAlign = TextAlign.Center,
            textDecoration = TextDecoration.Underline
        ),
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "login", start = offset, end = offset)
                .firstOrNull()?.let {
                }
        }
    )
}

@Composable
fun TripCard(
    city: String,
    country: String,
    startDate: String,
    endDate: String,
    placeId: String,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }


    LaunchedEffect(placeId) {
        imageBitmap = getPlacePhoto(placeId = placeId, placesClient = placesClient)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onLongPress()
                    },
                    onTap = {
                        onClick()
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .height(200.dp)
                .background(MaterialTheme.colorScheme.onSecondary)
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!.asImageBitmap(),
                    contentDescription = "City Landscape",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Warstwa półprzezroczystego gradientu, aby tekst był bardziej czytelny
            Box(
                modifier = Modifier
                    .fillMaxSize()
            )

            // Tekst z informacjami o podróży
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "$city, $country",
                    color = Color.White,
                    fontFamily = roundFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge.copy(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.8f),
                            offset = Offset(4f, 4f),
                            blurRadius = 6f
                        )
                    )
                )
                Text(
                    text = "Start: $startDate",
                    color = Color.White,
                    fontFamily = roundFontFamily,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.labelLarge.copy(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.8f),
                            offset = Offset(4f, 4f),
                            blurRadius = 6f
                        )
                    )
                )
                Text(
                    text = "End: $endDate",
                    color = Color.White,
                    fontFamily = roundFontFamily,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.labelLarge.copy(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.8f),
                            offset = Offset(4f, 4f),
                            blurRadius = 6f
                        )
                    )
                )
            }
        }
    }
}


@Composable
fun NotificationCard(
    city: String,
    country: String,
    startDate: Comparable<*>,
    endDate: String,
    sender: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Trip to $city, $country",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Sent by: $sender",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Dates: $startDate - $endDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // ikony akcept i decline
            Row(
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accept",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                IconButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.error,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Decline",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(onProfileClick: () -> Unit, onAddClick: () -> Unit, onNotificationsClick: () -> Unit, modifier: Modifier) {
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Przycisk Profil
            IconButton(onClick = { onProfileClick() }) {
                Icon(
                    imageVector = Icons.Default.Person2,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Pionowa kreska

            VerticalDivider(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .height(24.dp)
                    .width(3.dp)
            )

            // Przycisk Dodaj
            IconButton(onClick = { onAddClick() }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            VerticalDivider(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .height(24.dp)
                    .width(3.dp)
            )

            // Przycisk Powiadomienia
            IconButton(onClick = { onNotificationsClick() }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}


@SuppressLint("RememberReturnType")
@Composable
fun TopToggleButtons(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Przycisk "Upcoming"
        val upcomingColor by animateColorAsState(
            if (selectedTab == "upcoming") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
        )
        Text(
            text = "Upcoming",
            color = upcomingColor,
            fontFamily = roundFontFamily,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clickable(
                    indication = null, // Usuwa domyślne podświetlenie
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onTabSelected("upcoming")
                }
        )

        VerticalDivider(
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier
                .height(24.dp)
                .width(1.dp)
        )

        // Przycisk "Past"
        val pastColor by animateColorAsState(
            if (selectedTab == "past") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
        )
        Text(
            text = "  Past  ",
            color = pastColor,
            fontFamily = roundFontFamily,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onTabSelected("past")
                }
        )
    }
}


@OptIn(ExperimentalCoroutinesApi::class)
suspend fun getPlacePhoto(placeId: String, placesClient: PlacesClient): Bitmap? = suspendCancellableCoroutine { continuation ->
    val placeFields = listOf(Place.Field.PHOTO_METADATAS)
    val request = FetchPlaceRequest.newInstance(placeId, placeFields)

    placesClient.fetchPlace(request)
        .addOnSuccessListener { response ->
            val place = response.place
            val photoMetadata = place.photoMetadatas?.firstOrNull()

            if (photoMetadata != null) {
                val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(500)
                    .build()

                placesClient.fetchPhoto(photoRequest)
                    .addOnSuccessListener { photoResponse ->
                        continuation.resume(photoResponse.bitmap, null)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("PlacesAPI", "Photo not found: ${exception.message}")
                        continuation.resume(null, null)
                    }
            } else {
                continuation.resume(null, null)
            }
        }
        .addOnFailureListener { exception ->
            Log.e("PlacesAPI", "Place details not found: ${exception.message}")
            continuation.resume(null, null)
        }
}

