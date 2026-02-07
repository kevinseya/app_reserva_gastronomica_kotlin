package com.jwmaila.appticketera.data.remote

import com.jwmaila.appticketera.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface TicketeraApi {
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
}
