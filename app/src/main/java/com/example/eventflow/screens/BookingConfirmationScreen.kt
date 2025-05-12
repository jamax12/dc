package com.example.eventflow.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventflow.data.AuthRepository
import com.example.eventflow.models.EventModel
import com.example.eventflow.models.UserModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmationScreen(navController: NavController, eventId: String) {
    val authRepository = AuthRepository()
    val userId = authRepository.getCurrentUserId() ?: return
    var event by remember { mutableStateOf<EventModel?>(null) }
    var user by remember { mutableStateOf<UserModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(eventId) {
        // Fetch event
        val eventResult = authRepository.getEventById(userId, eventId)
        if (eventResult.isSuccess) {
            event = eventResult.getOrNull()
        } else {
            errorMessage = eventResult.exceptionOrNull()?.message ?: "Failed to load event"
        }
        // Fetch user
        authRepository.getUserData(userId).collectLatest { userData ->
            user = userData
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Booking") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            event != null && user != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Booking Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Event: ${event!!.title}", style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Date: ${event!!.date}", style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Price: $${event!!.price}", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "User: ${user!!.name}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Email: ${user!!.email}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Button(
                        onClick = { showPaymentDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Pay Now")
                    }
                }

                if (showPaymentDialog) {
                    AlertDialog(
                        onDismissRequest = { showPaymentDialog = false },
                        title = { Text("Confirm Payment") },
                        text = { Text("Proceed to pay $${event!!.price} for ${event!!.title}?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val result = authRepository.createBooking(userId, event!!)
                                        showPaymentDialog = false
                                        if (result.isSuccess) {
                                            snackbarHostState.showSnackbar("Booking confirmed!")
                                            navController.navigate("wishlist") {
                                                popUpTo(navController.graph.startDestinationId)
                                            }
                                        } else {
                                            snackbarHostState.showSnackbar("Booking failed")
                                        }
                                    }
                                }
                            ) {
                                Text("Confirm")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPaymentDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}