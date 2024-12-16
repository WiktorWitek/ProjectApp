package com.example.travelapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.travelapp.R
import com.example.travelapp.components.ButtonComponent
import com.example.travelapp.components.ClickableTextComponent
import com.example.travelapp.components.HeaderText
import com.example.travelapp.components.NormalTextComponent
import com.example.travelapp.components.NormalTextField
import com.example.travelapp.components.PasswordTextField
import com.example.travelapp.data.FirebaseService

@Composable
fun SignUpScreen(navController: NavHostController) {
    val backgroundColor = MaterialTheme.colorScheme.background

    val firstNameState = remember { mutableStateOf("") }
    val lastNameState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val confirmPasswordState = remember { mutableStateOf("") }
    val registrationResult = remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(top = 40.dp)
    ) {
        Surface(
            color = backgroundColor,
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                NormalTextComponent(value = stringResource(id = R.string.hey_there))
                HeaderText(value = stringResource(id = R.string.create_an_account))
                Spacer(modifier = Modifier.height(30.dp))

                NormalTextField(
                    labelValue = stringResource(id = R.string.first_name),
                    painterValue = painterResource(id = R.drawable.profile),
                    textState = firstNameState
                )
                NormalTextField(
                    labelValue = stringResource(id = R.string.last_name),
                    painterValue = painterResource(id = R.drawable.profile),
                    textState = lastNameState
                )
                NormalTextField(
                    labelValue = stringResource(id = R.string.email),
                    painterValue = painterResource(id = R.drawable.mail),
                    textState = emailState
                )
                PasswordTextField(
                    labelValue = stringResource(id = R.string.passowrd),
                    painterValue = painterResource(id = R.drawable.lock),
                    passwordState = passwordState
                )
                PasswordTextField(
                    labelValue = stringResource(R.string.confirm_password),
                    painterValue = painterResource(id = R.drawable.lock),
                    passwordState = confirmPasswordState
                )

                Spacer(modifier = Modifier.height(45.dp))

                // Obsługa rejestracji
                ButtonComponent(value = stringResource(id = R.string.register)) {
                    if (passwordState.value != confirmPasswordState.value) {
                        registrationResult.value = "Passwords do not match"
                    } else if (passwordState.value.isEmpty() ||
                        confirmPasswordState.value.isEmpty() ||
                        firstNameState.value.isEmpty() ||
                        lastNameState.value.isEmpty() ||
                        emailState.value.isEmpty()) {
                        registrationResult.value = "Fields cannot be empty"
                    }
                    else {
                        FirebaseService.registerUser(
                            emailState.value,
                            passwordState.value,
                            firstNameState.value,
                            lastNameState.value
                        ) { success, errorMessage ->
                            if (success) {
                                registrationResult.value = "Registration successful"
                                navController.navigate("login")
                            } else {
                                registrationResult.value = errorMessage ?: "Registration failed"
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                ClickableTextComponent(
                    normal = stringResource(id = R.string.already_have_an_account) + " ",
                    clickable = stringResource(id = R.string.login),
                    navController = navController,
                    onClickAction = { navController.navigate("login") }
                )
            }
        }
        SnackbarHost(hostState = snackbarHostState)
    }

    // Wyświetlanie komunikatów
    LaunchedEffect(registrationResult.value) {
        if (registrationResult.value.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = registrationResult.value,
                duration = SnackbarDuration.Short
            )
            registrationResult.value = ""
        }
    }
}
