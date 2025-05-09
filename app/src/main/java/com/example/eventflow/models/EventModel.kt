package com.example.eventflow.models

data class EventModel(
    val eventId: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "", // Format: "YYYY-MM-DD"
    val time: String = "", // Format: "HH:MM"
    val location: String = "",
    val userId: String = "" // Links event to the user
)