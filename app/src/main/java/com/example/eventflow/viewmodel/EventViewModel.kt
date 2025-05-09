package com.example.eventflow.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.eventflow.data.EventRepository
import com.example.eventflow.models.EventModel
import com.example.eventflow.navigation.ROUTE_EVENT_LIST
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {
    private val repository = EventRepository()
    val events: StateFlow<List<EventModel>> = repository.events

    fun fetchEvents(userId: String) {
        repository.fetchEvents(userId)
    }

    fun saveEvent(
        event: EventModel,
        userId: String,
        navController: NavController,
        context: Context
    ) {
        if (event.title.isBlank() || event.date.isBlank() || event.time.isBlank()) {
            Toast.makeText(context, "Please fill in title, date, and time", Toast.LENGTH_LONG).show()
            return
        }

        viewModelScope.launch {
            val result = repository.saveEvent(userId, event)
            if (result.isSuccess) {
                Toast.makeText(context, "Event saved successfully", Toast.LENGTH_LONG).show()
                navController.navigate(ROUTE_EVENT_LIST)
            } else {
                Toast.makeText(context, "Failed to save event", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun deleteEvent(
        userId: String,
        eventId: String,
        context: Context
    ) {
        viewModelScope.launch {
            val result = repository.deleteEvent(userId, eventId)
            if (result.isSuccess) {
                Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to delete event", Toast.LENGTH_LONG).show()
            }
        }
    }
}