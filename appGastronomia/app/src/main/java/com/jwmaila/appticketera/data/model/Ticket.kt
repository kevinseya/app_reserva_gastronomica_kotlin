package com.jwmaila.appticketera.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Ticket(
    val id: String,
    val userId: String,
    val eventId: String,
    val seatId: String,
    val status: TicketStatus,
    val qrCode: String,
    val stripePaymentId: String?,
    val purchaseDate: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val event: Event? = null,
    val seat: Seat? = null
)

@Serializable
enum class TicketStatus {
    PENDING,
    PAID,
    CANCELLED,
    USED
}

@Serializable
data class PaymentIntentResponse(
    val clientSecret: String,
    val paymentIntentId: String
)

@Serializable
data class CreatePaymentIntentRequest(
    val eventId: String,
    val seatIds: List<String>
)

@Serializable
data class ConfirmPaymentRequest(
    val paymentIntentId: String,
    val seatIds: List<String>
)

@Serializable
data class VerifyTicketRequest(
    val qrCode: String
)

@Serializable
data class VerifyTicketResponse(
    val valid: Boolean,
    val message: String,
    val ticket: Ticket?
)
