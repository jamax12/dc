package com.example.eventflow.data

import com.example.eventflow.models.EventModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class EventRepository {
    private val database = FirebaseDatabase.getInstance().getReference("Events")
    private val _events = MutableStateFlow<List<EventModel>>(emptyList())
    val events: StateFlow<List<EventModel>> = _events

    // Fetch events for the current user
    fun fetchEvents(userId: String) {
        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventList = mutableListOf<EventModel>()
                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(EventModel::class.java)
                    event?.let { eventList.add(it) }
                }
                _events.value = eventList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error (e.g., log or notify)
            }
        })
    }

    // Create or update an event
    suspend fun saveEvent(userId: String, event: EventModel): Result<Unit> {
        return try {
            val eventId = event.eventId.ifEmpty { database.child(userId).push().key ?: "" }
            val updatedEvent = event.copy(eventId = eventId, userId = userId)
            database.child(userId).child(eventId).setValue(updatedEvent).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete an event
    suspend fun deleteEvent(userId: String, eventId: String): Result<Unit> {
        return try {
            database.child(userId).child(eventId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}