package com.mespinoza.appgastronomia.data.remote

import com.mespinoza.appgastronomia.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface GastronomiaApi {
    // Auth endpoints
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @GET("auth/profile")
    suspend fun getProfile(): User
    
    // Events endpoints
    @GET("events")
    suspend fun getEvents(): List<Event>
    
    @GET("events/{id}")
    suspend fun getEvent(@Path("id") id: String): EventWithSeats

    @GET("events/{id}/tables")
    suspend fun getEventTables(@Path("id") id: String): List<EventTable>

    @POST("events/{id}/tables/auto")
    suspend fun generateTables(@Path("id") id: String, @Body request: GenerateTablesRequest): List<EventTable>

    @POST("events/{id}/tables")
    suspend fun createEventTable(@Path("id") id: String, @Body request: CreateEventTableRequest): EventTable

    @PATCH("events/{eventId}/tables/{tableId}")
    suspend fun updateEventTable(@Path("eventId") eventId: String, @Path("tableId") tableId: String, @Body request: UpdateEventTableRequest): EventTable

    @DELETE("events/{eventId}/tables/{tableId}")
    suspend fun deleteEventTable(@Path("eventId") eventId: String, @Path("tableId") tableId: String): GenericResponse
    
    @Multipart
    @POST("events")
    suspend fun createEvent(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("date") date: RequestBody,
        @Part("venue") venue: RequestBody,
        @Part("ticketPrice") ticketPrice: RequestBody,
        @Part("totalSeats") totalSeats: RequestBody,
        @Part image: MultipartBody.Part?
    ): Event
    
    @Multipart
    @PATCH("events/{id}")
    suspend fun updateEvent(
        @Path("id") id: String,
        @Part("name") name: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("date") date: RequestBody?,
        @Part("venue") venue: RequestBody?,
        @Part("ticketPrice") ticketPrice: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Event
    
    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: String)
    
    // Tickets endpoints
    @POST("tickets/create-payment-intent")
    suspend fun createPaymentIntent(@Body request: CreatePaymentIntentRequest): PaymentIntentResponse
    
    @POST("tickets/confirm-payment/{paymentIntentId}")
    suspend fun confirmPayment(
        @Path("paymentIntentId") paymentIntentId: String,
        @Body request: ConfirmPaymentRequest
    ): List<Ticket>
    
    @GET("tickets/my-tickets")
    suspend fun getMyTickets(): List<Ticket>

    @GET("tickets/{id}")
    suspend fun getTicketById(@Path("id") id: String): Ticket

    @GET("tickets")
    suspend fun getAllTickets(): List<Ticket>
    
    @POST("tickets/verify")
    suspend fun verifyTicket(@Body request: VerifyTicketRequest): VerifyTicketResponse
    
    // Users endpoints (admin only)
    @GET("users")
    suspend fun getUsers(): List<User>
    
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): User
    
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: String)

    @PATCH("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: UpdateUserRequest
    ): User

    @POST("users")
    suspend fun createUser(@Body request: CreateUserRequest): User

    @PATCH("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): User

    // Tables (mesas)
    @GET("tables")
    suspend fun getTables(): List<Table>

    @POST("tables")
    suspend fun createTable(@Body request: CreateTableRequest): Table

    @PATCH("tables/{id}")
    suspend fun updateTable(@Path("id") id: String, @Body request: UpdateTableRequest): Table

    // Reservations
    @POST("reservations")
    suspend fun createReservation(@Body request: CreateReservationRequest): Reservation

    @POST("reservations/{id}/order")
    suspend fun createOrderForReservation(@Path("id") id: String, @Body request: CreateOrderRequest): OrderResponse

    @GET("reservations/my")
    suspend fun getMyReservations(): List<Reservation>

    @GET("reservations/{id}")
    suspend fun getReservation(@Path("id") id: String): Reservation

    // Orders - pay and confirm
    @POST("orders/{id}/pay")
    suspend fun payOrder(@Path("id") id: String): PaymentIntentResponse

    @POST("orders/{id}/confirm")
    suspend fun confirmOrder(@Path("id") id: String, @Body request: ConfirmPaymentRequest): GenericResponse

    // Food Management
    @GET("food/menu")
    suspend fun getMenu(): List<FoodCategory>

    @POST("food/categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): FoodCategory

    @Multipart
    @POST("food/items")
    suspend fun createFoodItem(
        @Part("categoryId") categoryId: RequestBody,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("price") price: RequestBody,
        @Part image: MultipartBody.Part?
    ): FoodItem

    @Multipart
    @PATCH("food/items/{id}")
    suspend fun updateFoodItem(
        @Path("id") id: String,
        @Part("categoryId") categoryId: RequestBody?,
        @Part("name") name: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("price") price: RequestBody?,
        @Part image: MultipartBody.Part?
    ): FoodItem

    @DELETE("food/items/{id}")
    suspend fun deleteFoodItem(@Path("id") id: String): GenericResponse
}

@kotlinx.serialization.Serializable
data class CreateOrderRequest(val items: List<OrderItemRequest>)

@kotlinx.serialization.Serializable
data class OrderItemRequest(val menuItemId: String, val quantity: Int = 1)

@kotlinx.serialization.Serializable
data class OrderResponse(val id: String, val reservationId: String?, val total: Double, val status: String)

@kotlinx.serialization.Serializable
data class PaymentIntentResponse(val clientSecret: String, val paymentIntentId: String)

@kotlinx.serialization.Serializable
data class GenericResponse(val ok: Boolean)

@kotlinx.serialization.Serializable
data class GenerateTablesRequest(val tablesCount: Int, val capacity: Int)

@kotlinx.serialization.Serializable
data class CreateEventTableRequest(
    val name: String? = null,
    val x: Int? = null,
    val y: Int? = null,
    val rotation: Int? = null,
    val capacity: Int? = null,
    val seatPrice: Double? = null
)

@kotlinx.serialization.Serializable
data class UpdateEventTableRequest(
    val name: String? = null,
    val x: Int? = null,
    val y: Int? = null,
    val rotation: Int? = null,
    val capacity: Int? = null,
    val seatPrice: Double? = null
)
