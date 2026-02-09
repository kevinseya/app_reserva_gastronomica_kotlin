package com.mespinoza.appgastronomia.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.mespinoza.appgastronomia.data.local.UserPreferences
import com.mespinoza.appgastronomia.data.model.*
import com.mespinoza.appgastronomia.data.remote.GastronomiaApi
import com.mespinoza.appgastronomia.data.remote.getUserMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GastronomiaRepository @Inject constructor(
    private val api: GastronomiaApi,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) {
    // Auth
    suspend fun register(email: String, password: String, firstName: String, lastName: String): Result<AuthResponse> {
        return try {
            val response = api.register(RegisterRequest(email, password, firstName, lastName))
            userPreferences.saveAuthToken(response.accessToken)
            userPreferences.saveUserData(
                response.user.id,
                response.user.fullName,
                response.user.email,
                response.user.role.name
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }
    
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            userPreferences.saveAuthToken(response.accessToken)
            userPreferences.saveUserData(
                response.user.id,
                response.user.fullName,
                response.user.email,
                response.user.role.name
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }
    
    fun logout() {
        userPreferences.clear()
    }
    
    fun isLoggedIn(): Boolean = userPreferences.isLoggedIn()
    
    fun getUserRole(): String? = userPreferences.getUserRole()
    
    suspend fun getProfile(): Result<User> {
        return try {
            val user = api.getProfile()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }
    
    // Events
    suspend fun getEvents(): Result<List<Event>> {
        return try {
            val events = api.getEvents()
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getEvent(id: String): Result<EventWithSeats> {
        return try {
            val event = api.getEvent(id)
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }
    
    suspend fun createEvent(request: CreateEventRequest, totalSeats: Int, imageUri: Uri? = null): Result<Event> {
        return try {
            val namePart = request.name.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionPart = request.description?.toRequestBody("text/plain".toMediaTypeOrNull())
            val datePart = request.date.toRequestBody("text/plain".toMediaTypeOrNull())
            val venuePart = request.venue.toRequestBody("text/plain".toMediaTypeOrNull())
            val pricePart = request.ticketPrice.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val seatsPart = totalSeats.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            
            val imagePart = imageUri?.let { uri ->
                val file = getFileFromUri(uri)
                val mimeType = getMimeType(uri)
                val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", file.name, requestFile)
            }
            
            val event = api.createEvent(namePart, descriptionPart, datePart, venuePart, pricePart, seatsPart, imagePart)
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }
    
    suspend fun updateEvent(id: String, request: UpdateEventRequest, imageUri: Uri? = null): Result<Event> {
        return try {
            val namePart = request.name?.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionPart = request.description?.toRequestBody("text/plain".toMediaTypeOrNull())
            val datePart = request.date?.toRequestBody("text/plain".toMediaTypeOrNull())
            val venuePart = request.venue?.toRequestBody("text/plain".toMediaTypeOrNull())
            val pricePart = request.ticketPrice?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val imagePart = imageUri?.let { uri ->
                val file = getFileFromUri(uri)
                val mimeType = getMimeType(uri)
                val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", file.name, requestFile)
            }
            
            val event = api.updateEvent(id, namePart, descriptionPart, datePart, venuePart, pricePart, imagePart)
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }
    
    private fun getFileFromUri(uri: Uri): File {
        return if (uri.scheme == "content") {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            tempFile.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            tempFile
        } else {
            uri.toFile()
        }
    }
    
    private fun getMimeType(uri: Uri): String {
        return if (uri.scheme == "content") {
            context.contentResolver.getType(uri) ?: "image/jpeg"
        } else {
            val extension = uri.path?.substringAfterLast('.', "")
            when (extension?.lowercase()) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                else -> "image/jpeg"
            }
        }
    }
    
    suspend fun deleteEvent(id: String): Result<Unit> {
        return try {
            api.deleteEvent(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }
    
    // Tickets
    suspend fun createPaymentIntent(eventId: String, seatIds: List<String>): Result<com.mespinoza.appgastronomia.data.remote.PaymentIntentResponse> {
        return try {
            val response = api.createPaymentIntent(CreatePaymentIntentRequest(eventId, seatIds))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }
    
    suspend fun confirmPayment(paymentIntentId: String, seatIds: List<String>): Result<List<Ticket>> {
        return try {
            val tickets = api.confirmPayment(paymentIntentId, ConfirmPaymentRequest(paymentIntentId, seatIds))
            Result.success(tickets)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }
    
    suspend fun getMyTickets(): Result<List<Ticket>> {
        return try {
            val tickets = api.getMyTickets()
            Result.success(tickets)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    // Tables
    suspend fun getTables(): Result<List<Table>> {
        return try {
            val tables = api.getTables()
            Result.success(tables)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun createTable(request: CreateTableRequest): Result<Table> {
        return try {
            val table = api.createTable(request)
            Result.success(table)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun updateTable(id: String, request: UpdateTableRequest): Result<Table> {
        return try {
            val table = api.updateTable(id, request)
            Result.success(table)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    // Reservations
    suspend fun createReservation(request: CreateReservationRequest): Result<Reservation> {
        return try {
            val reservation = api.createReservation(request)
            Result.success(reservation)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun getMyReservations(): Result<List<Reservation>> {
        return try {
            val reservations = api.getMyReservations()
            Result.success(reservations)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun getReservation(id: String): Result<Reservation> {
        return try {
            val reservation = api.getReservation(id)
            Result.success(reservation)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun getEventTables(eventId: String): Result<List<com.mespinoza.appgastronomia.data.model.EventTable>> {
        return try {
            val tables = api.getEventTables(eventId)
            Result.success(tables)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun createEventTable(eventId: String, request: com.mespinoza.appgastronomia.data.remote.CreateEventTableRequest): Result<com.mespinoza.appgastronomia.data.model.EventTable> {
        return try {
            val table = api.createEventTable(eventId, request)
                android.util.Log.d("GastronomiaRepo", "createEventTable success: " + table.id)
            Result.success(table)
        } catch (e: Exception) {
                android.util.Log.e("GastronomiaRepo", "createEventTable error: " + (e.message ?: "?"), e)
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun generateTables(eventId: String, tablesCount: Int, capacity: Int): Result<List<com.mespinoza.appgastronomia.data.model.EventTable>> {
        return try {
            val tables = api.generateTables(eventId, com.mespinoza.appgastronomia.data.remote.GenerateTablesRequest(tablesCount, capacity))
            Result.success(tables)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun updateEventTable(eventId: String, tableId: String, request: com.mespinoza.appgastronomia.data.remote.UpdateEventTableRequest): Result<com.mespinoza.appgastronomia.data.model.EventTable> {
        return try {
            val table = api.updateEventTable(eventId, tableId, request)
            Result.success(table)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun deleteEventTable(eventId: String, tableId: String): Result<com.mespinoza.appgastronomia.data.remote.GenericResponse> {
        return try {
            val resp = api.deleteEventTable(eventId, tableId)
            Result.success(resp)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun createOrderForReservation(reservationId: String, items: List<com.mespinoza.appgastronomia.data.remote.OrderItemRequest>): Result<com.mespinoza.appgastronomia.data.remote.OrderResponse> {
        return try {
            val order = api.createOrderForReservation(reservationId, com.mespinoza.appgastronomia.data.remote.CreateOrderRequest(items))
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun payOrder(orderId: String): Result<com.mespinoza.appgastronomia.data.remote.PaymentIntentResponse> {
        return try {
            val resp = api.payOrder(orderId)
            Result.success(resp)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun confirmOrder(orderId: String, paymentIntentId: String): Result<com.mespinoza.appgastronomia.data.remote.GenericResponse> {
        return try {
            val resp = api.confirmOrder(orderId, com.mespinoza.appgastronomia.data.model.ConfirmPaymentRequest(paymentIntentId, emptyList<String>()))
            Result.success(resp)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun getTicketById(id: String): Result<Ticket> {
        return try {
            val ticket = api.getTicketById(id)
            Result.success(ticket)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun getAllTickets(): Result<List<Ticket>> {
        return try {
            val tickets = api.getAllTickets()
            Result.success(tickets)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }
    
    suspend fun verifyTicket(qrCode: String): Result<VerifyTicketResponse> {
        return try {
            val response = api.verifyTicket(VerifyTicketRequest(qrCode))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }
    
    // Users (admin only)
    suspend fun getUsers(): Result<List<User>> {
        return try {
            val users = api.getUsers()
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun createUser(request: CreateUserRequest): Result<User> {
        return try {
            val user = api.createUser(request)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun updateUser(id: String, request: UpdateUserRequest): Result<User> {
        return try {
            val user = api.updateUser(id, request)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun deleteUser(id: String): Result<Unit> {
        return try {
            api.deleteUser(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Result<User> {
        return try {
            val user = api.updateProfile(request)
            userPreferences.saveUserData(user.id, user.fullName, user.email, user.role.name)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(e.getUserMessage()))
        }
    }
}
