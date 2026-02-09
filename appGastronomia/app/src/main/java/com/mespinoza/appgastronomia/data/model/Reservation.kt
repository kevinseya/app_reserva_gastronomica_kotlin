package com.mespinoza.appgastronomia.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Reservation(
    val id: String,
    val userId: String,
    val tableId: String?,
    val datetime: String,
    val partySize: Int,
    val status: String,
    val notes: String?
)

@Serializable
data class CreateReservationRequest(
    val eventId: String? = null,
    val tableId: String? = null,
    val seatIds: List<String> = emptyList(),
    val datetime: String,
    val partySize: Int = 1,
    val notes: String? = null
)
