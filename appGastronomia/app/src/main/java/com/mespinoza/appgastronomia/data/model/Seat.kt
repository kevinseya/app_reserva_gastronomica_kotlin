package com.mespinoza.appgastronomia.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Seat(
    val id: String,
    val eventId: String,
    val row: Int,
    val column: Int,
    val isOccupied: Boolean,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
