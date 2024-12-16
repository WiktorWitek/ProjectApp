package com.example.travelapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.travelapp.R
import com.example.travelapp.components.ButtonComponent
import com.example.travelapp.components.ClickableTextComponent
import com.example.travelapp.components.DividerTextComponent
import com.example.travelapp.components.HeaderText
import com.example.travelapp.components.NormalTextComponent
import com.example.travelapp.components.NormalTextField
import com.example.travelapp.components.PasswordTextField
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavHostController) {
    val backgroundColor = MaterialTheme.colorScheme.background

    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val auth = FirebaseAuth.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .imePadding()
            .padding(top = 40.dp)
    ) {
        Surface(
            color = backgroundColor,
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                NormalTextComponent(value = stringResource(id = R.string.hey_there))
                HeaderText(value = stringResource(id = R.string.login))
                Spacer(modifier = Modifier.height(30.dp))

                // Email input field
                NormalTextField(
                    labelValue = stringResource(id = R.string.email),
                    painterValue = painterResource(id = R.drawable.mail),
                    textState = emailState
                )

                // Password input field
                PasswordTextField(
                    labelValue = stringResource(id = R.string.passowrd),
                    painterValue = painterResource(id = R.drawable.lock),
                    passwordState = passwordState
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Error message display (if exists)
                errorMessage?.let {
                    Text(text = it, color = androidx.compose.ui.graphics.Color.Red)
                }

                //FYPclickableText()
                Spacer(modifier = Modifier.height(200.dp))

                // Login button
                ButtonComponent(value = stringResource(id = R.string.login), onClick = {
                    coroutineScope.launch {
                        loginUser(
                            auth = auth,
                            email = emailState.value,
                            password = passwordState.value,
                            navController = navController
                        ) { error ->
                            errorMessage = error
                        }
                    }
                })

                Spacer(modifier = Modifier.height(10.dp))
                DividerTextComponent()

                // Sign up navigation
                ClickableTextComponent(
                    normal = "Don't have an account yet? ",
                    clickable = stringResource(id = R.string.register),
                    navController = navController,
                    onClickAction = { navController.navigate("signup") }
                )
            }
        }
    }
}



fun loginUser(
    auth: FirebaseAuth,
    email: String,
    password: String,
    navController: NavHostController,
    onError: (String?) -> Unit
) {
    if (email.isEmpty() || password.isEmpty()) {
        onError("Email and password cannot be empty.")
        return
    }

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                onError(task.exception?.localizedMessage)
            }
        }
}