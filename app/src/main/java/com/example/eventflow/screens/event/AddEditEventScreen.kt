package com.example.eventflow.screens.event

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventflow.data.AuthRepository
import com.example.eventflow.models.EventModel
import com.example.eventflow.viewmodel.EventViewModel

@Composable
fun AddEditEventScreen(navController: NavController, eventViewModel: EventViewModel) {
    val context = LocalContext.current
    val authRepository = AuthRepository()
    val userId = authRepository.getCurrentUserId() ?: return

    // State for event fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var eventId by remember { mutableStateOf("") }

    // Loading state
    var isLoading by remember { mutableStateOf(false) }

    // Get eventId from navigation arguments
    val navBackStackEntry = navController.currentBackStackEntry
    val eventIdArg = navBackStackEntry?.arguments?.getString("eventId")

    // Load event data if editing
    LaunchedEffect(eventIdArg) {
        if (!eventIdArg.isNullOrEmpty()) {
            isLoading = true
            eventViewModel.events.collect { events ->
                val event = events.find { it.eventId == eventIdArg }
                event?.let {
                    title = it.title
                    description = it.description
                    date = it.date
                    time = it.time
                    location = it.location
                    eventId = it.eventId
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = if (eventId.isEmpty()) "Create New Event" else "Edit Event",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator()
        }

        // Event title field
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Event Title") },
            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Date field
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date (YYYY-MM-DD)") },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Time field
        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time (HH:MM)") },
            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Location field
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Description field
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Save button
        Button(
            onClick = {
                isLoading = true
                val event = EventModel(
                    eventId = eventId,
                    title = title,
                    description = description,
                    date = date,
                    time = time,
                    location = location,
                    userId = userId
                )
                eventViewModel.saveEvent(event, userId, navController, context)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = title.isNotBlank() && date.isNotBlank() && time.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (eventId.isEmpty()) "Create Event" else "Update Event",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}