package com.mespinoza.appgastronomia.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Ticket(
    val id: String,
    val userId: String,
    val eventId: String,
    val seatId: String?,
    val status: TicketStatus,
    val qrCode: String,
    val stripePaymentId: String?,
    val tableSeatId: String? = null,
    val purchaseDate: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val event: Event? = null,
    val seat: Seat? = null,
    val tableSeat: com.mespinoza.appgastronomia.data.model.TableSeat? = null,
    val foodItems: List<TicketFood> = emptyList()
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
    val seatIds: List<String>,
    val foodOrders: List<SeatFoodOrder> = emptyList()
)

@Serializable
data class SeatFoodOrder(
    val seatId: String,
    val foodItems: List<FoodOrderItem>
)

@Serializable
data class FoodOrderItem(
    val foodId: String,
    val quantity: Int
)

@Serializable
data class TicketFood(
    val id: String,
    val foodItem: FoodItem,
    val quantity: Int,
    val status: String // PENDING, COOKING, SERVED
)

@Serializable
data class FoodItem(
    val id: String,
    val name: String,
    val description: String?,
    val price: Double,
    val imageUrl: String?,
    val categoryId: String,
    val category: FoodCategory? = null
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

@Serializable
data class FoodCategory(
    val id: String,
    val name: String,
    val items: List<FoodItem> = emptyList()
)

@Serializable
data class CreateCategoryRequest(val name: String)

@Serializable
data class CreateFoodItemRequest(
    val categoryId: String,
    val name: String,
    val description: String?,
    val price: Double,
    val imageUrl: String? = null
)
