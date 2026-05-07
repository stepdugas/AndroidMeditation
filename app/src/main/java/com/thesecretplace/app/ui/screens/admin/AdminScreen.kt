package com.thesecretplace.app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.theme.*

@Composable
fun AdminScreen(viewModel: AdminViewModel = hiltViewModel()) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val loginError by viewModel.loginError.collectAsState()
    val uploadResult by viewModel.uploadResult.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()

    ThemedBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            SectionHeader("Admin Panel")
            Spacer(Modifier.height(6.dp))

            if (!isLoggedIn) {
                // Login panel
                Text("The Secret Place", fontFamily = SerifDisplay, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
                Text("Sign in to publish new meditations", fontSize = 14.sp, color = Color.White.copy(0.45f))

                Spacer(Modifier.height(28.dp))

                PremiumCard {
                    Column(modifier = Modifier.padding(24.dp)) {
                        var email by remember { mutableStateOf("") }
                        var password by remember { mutableStateOf("") }

                        Text("EMAIL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.4f), letterSpacing = 1.6.sp)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("cassia@example.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Surface,
                                unfocusedContainerColor = Surface,
                                cursorColor = Accent
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        Text("PASSWORD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.4f), letterSpacing = 1.6.sp)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("••••••••") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Surface,
                                unfocusedContainerColor = Surface,
                                cursorColor = Accent
                            ),
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )

                        loginError?.let {
                            Spacer(Modifier.height(12.dp))
                            Text(it, fontSize = 12.sp, color = ErrorRed.copy(0.8f))
                        }

                        Spacer(Modifier.height(16.dp))

                        GoldButton("Sign In") {
                            viewModel.signIn(email, password)
                        }
                    }
                }
            } else {
                // Upload panel
                Text("Upload Meditation", fontFamily = SerifDisplay, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
                Text("Signed in as ${viewModel.currentUserEmail}", fontSize = 12.sp, color = Color.White.copy(0.35f))

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = { viewModel.signOut() }) {
                    Text("Sign Out", color = Accent, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(16.dp))

                PremiumCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        var title by remember { mutableStateOf("") }
                        var description by remember { mutableStateOf("") }
                        var duration by remember { mutableStateOf("") }

                        Text("TITLE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.4f))
                        OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Surface, unfocusedContainerColor = Surface, cursorColor = Accent))

                        Spacer(Modifier.height(12.dp))

                        Text("DESCRIPTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.4f))
                        OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth(), maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Surface, unfocusedContainerColor = Surface, cursorColor = Accent))

                        Spacer(Modifier.height(12.dp))

                        Text("DURATION (e.g. 8 min)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.4f))
                        OutlinedTextField(value = duration, onValueChange = { duration = it }, modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Surface, unfocusedContainerColor = Surface, cursorColor = Accent))
                    }
                }

                uploadResult?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(it, fontSize = 14.sp, color = if (it.startsWith("✓")) Accent else ErrorRed.copy(0.8f))
                }

                Spacer(Modifier.height(16.dp))

                GoldButton(if (isUploading) "Uploading…" else "Publish Meditation") {
                    // Upload logic will use viewModel
                }
            }
        }
    }
}
