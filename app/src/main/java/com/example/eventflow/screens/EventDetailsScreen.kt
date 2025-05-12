package com.example.eventflow.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventflow.data.AuthRepository
import com.example.eventflow.models.EventModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(navController: NavController, eventId: String) {
    val authRepository = AuthRepository()
    val userId = authRepository.getCurrentUserId()
    var event by remember { mutableStateOf<EventModel?>(null) }
    var isInWishlist by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Custom back handler
    BackHandler(enabled = true) {
        println("EventDetailsScreen: Back button pressed, popping back stack")
        navController.popBackStack()
    }

    LaunchedEffect(key1 = eventId, key2 = userId) {
        println("EventDetailsScreen: Started with eventId=$eventId, userId=$userId")

        if (userId == null) {
            println("EventDetailsScreen: No user ID, showing error")
            errorMessage = "Please log in to view event details"
            isLoading = false
            return@LaunchedEffect
        }

        if (eventId.isBlank()) {
            println("EventDetailsScreen: Empty eventId")
            errorMessage = "Invalid event ID"
            isLoading = false
            return@LaunchedEffect
        }

        // Fetch event with timeout
        val eventResult = withTimeoutOrNull(5000L) {
            withContext(Dispatchers.IO) {
                println("EventDetailsScreen: Fetching event for eventId=$eventId")
                authRepository.getEventById(userId, eventId)
            }
        }

        when {
            eventResult == null -> {
                println("EventDetailsScreen: Event fetch timed out")
                errorMessage = "Request timed out. Please check your connection."
            }
            eventResult.isSuccess -> {
                event = eventResult.getOrNull()
                println("EventDetailsScreen: Event fetch success, event=${event?.title}")
                if (event == null) {
                    errorMessage = "Event not found"
                    println("EventDetailsScreen: Event not found for eventId=$eventId")
                }
            }
            eventResult.isFailure -> {
                errorMessage = eventResult.exceptionOrNull()?.message ?: "Failed to load event"
                println("EventDetailsScreen: Event fetch error: ${eventResult.exceptionOrNull()?.message}")
            }
        }

        // Check wishlist status
        try {
            authRepository.getWishlist(userId)
                .catch { e ->
                    println("EventDetailsScreen: Wishlist fetch error: ${e.message}")
                    errorMessage = errorMessage ?: "Unable to load wishlist: ${e.message}"
                }
                .collectLatest { wishlist ->
                    isInWishlist = wishlist.any { it.eventId == eventId }
                    println("EventDetailsScreen: Wishlist status updated, isInWishlist=$isInWishlist")
                }
        } catch (e: Exception) {
            println("EventDetailsScreen: Wishlist exception: ${e.message}")
            errorMessage = errorMessage ?: "Unable to load wishlist: ${e.message}"
        }

        isLoading = false
        println("EventDetailsScreen: Loading complete, isLoading=$isLoading, event=${event?.title}, errorMessage=$errorMessage")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(tween(500))
            ) {
                CircularProgressIndicator()
            }

            AnimatedVisibility(
                visible = !isLoading && errorMessage != null,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Go Back")
                    }
                }
            }

            AnimatedVisibility(
                visible = !isLoading && event != null && errorMessage == null,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500))
            ) {
                event?.let {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = it.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Date: ${it.date}", style = MaterialTheme.typography.bodyLarge)
                                Text(text = "Time: ${it.time}", style = MaterialTheme.typography.bodyLarge)
                                Text(text = "Location: ${it.location}", style = MaterialTheme.typography.bodyLarge)
                                Text(text = "Price: $${it.price}", style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = it.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val result = if (isInWishlist) {
                                            authRepository.removeFromWishlist(userId!!, it.eventId)
                                        } else {
                                            authRepository.addToWishlist(userId!!, it)
                                        }
                                        if (result.isSuccess) {
                                            isInWishlist = !isInWishlist
                                            snackbarHostState.showSnackbar(
                                                if (isInWishlist) "Added to wishlist" else "Removed from wishlist"
                                            )
                                        } else {
                                            snackbarHostState.showSnackbar("Operation failed: ${result.exceptionOrNull()?.message}")
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isInWishlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isInWishlist) "Remove from Wishlist" else "Add to Wishlist")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { navController.navigate("booking_confirmation/${it.eventId}") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Book Now")
                            }
                        }
                    }
                }
            }
        }
    }
}