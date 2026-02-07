package com.jwmaila.appticketera.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: String,
    val name: String,
    val description: String?,
    val date: String,
    val venue: String,
    val imageUrl: String?,
    val ticketPrice: Double,
    val totalSeats: Int = 100,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class EventWithSeats(
    val id: String,
    val name: String,
    val description: String?,
    val date: String,
    val venue: String,
    val imageUrl: String?,
    val ticketPrice: Double,
    val seats: List<Seat>
)

@Serializable
data class CreateEventRequest(
    val name: String,
    val description: String?,
    val date: String,
    val venue: String,
    val imageUrl: String?,
    val ticketPrice: Double
)

@Serializable
data class UpdateEventRequest(
    val name: String?,
    val description: String?,
    val date: String?,
    val venue: String?,
    val imageUrl: String?,
    val ticketPrice: Double?
)
