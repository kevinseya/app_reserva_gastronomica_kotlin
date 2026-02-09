package com.mespinoza.appgastronomia.data.model

import kotlinx.serialization.Serializable

@Serializable
data class EventTable(
    val id: String,
    val eventId: String,
    val name: String? = null,
    val x: Int = 0,
    val y: Int = 0,
    val rotation: Int? = null,
    val capacity: Int = 4,
    val seatPrice: Double? = null,
    val seats: List<TableSeat> = emptyList()
)

@Serializable
data class TableSeat(
    val id: String,
    val tableId: String,
    val index: Int,
    val price: Double,
    val reservationId: String? = null
    ,
    val table: EventTable? = null
)
