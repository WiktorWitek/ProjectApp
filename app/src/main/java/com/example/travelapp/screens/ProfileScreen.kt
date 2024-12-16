package com.example.travelapp.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person3
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.travelapp.ui.theme.roundFontFamily
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController,
                  isDarkTheme: Boolean,
                  onThemeToggle: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current
    
    var settingDialog by remember { mutableStateOf(false) }

    // Zmienne do przechowywania danych użytkownika
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val email by remember { mutableStateOf(currentUser?.email ?: "") }
    var userID by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }


    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    // Pobierz dane użytkownika z Firestore
    if (currentUser == null) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo("profile") {
                    inclusive = true
                }
            }
        }
        return
    }
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        firstName = document.getString("firstName") ?: ""
                        lastName = document.getString("lastName") ?: ""
                        userID = currentUser.uid
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 30.dp, bottom = 56.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
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
            
            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { settingDialog = true },
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        if (settingDialog) {
            Dialog(
                onDismissRequest = { settingDialog = false },
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(text = "Settings",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    
                    Text(text = "Switch color mode",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineSmall
                        )
                    
                    Switch(checked = isDarkTheme,
                        onCheckedChange = {
                            isChecked ->
                            onThemeToggle()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.LightGray,
                            uncheckedThumbColor = Color.White,
                            checkedTrackColor = Color.Black.copy(alpha = 0.5f),
                            uncheckedTrackColor = Color.DarkGray.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        try {
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        } catch (e: Exception) {
                            Log.e("SignOut", "Error signing out: ${e.message}")
                        }
                    },
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(text = "Sign out")
                    }
                }
            }
        }

        Text(
            "My Profile",
            fontFamily = roundFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 42.sp,
            color = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Person3, contentDescription = "")
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Name", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary)
                Text("$firstName $lastName", fontFamily = roundFontFamily, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSecondary)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Email, contentDescription = "")
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Email", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary)
                Text(email, fontFamily = roundFontFamily, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSecondary)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = "")
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Share Code", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary)
                ClickableText(
                    text = AnnotatedString(currentUser!!.uid),
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSecondary,
                        fontFamily = roundFontFamily, fontWeight = FontWeight.Medium),
                    onClick = {
                        // Kopiowanie tekstu do schowka
                        clipboardManager.setText(AnnotatedString(currentUser.uid))
                        Toast.makeText(context, "User ID copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }


        Spacer(modifier = Modifier.weight(1f))

        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = { Text("Current Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
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

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
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

        OutlinedTextField(
            value = confirmNewPassword,
            onValueChange = { confirmNewPassword = it },
            label = { Text("Confirm New Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (currentPassword.isBlank()) {
                    Toast.makeText(context, "Current password cannot be empty", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (newPassword.isBlank() || confirmNewPassword.isBlank()) {
                    Toast.makeText(context, "New password cannot be empty", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (newPassword != confirmNewPassword) {
                    Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                currentUser?.reauthenticate(credential)
                    ?.addOnSuccessListener {
                        currentUser.updatePassword(newPassword)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                                auth.signOut()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to update password", Toast.LENGTH_SHORT).show()
                            }
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(context, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Change Password")
        }
    }
}
