package com.jwmaila.appticketera.ui.screens.admin

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jwmaila.appticketera.data.model.Event
import com.jwmaila.appticketera.data.model.CreateEventRequest
import com.jwmaila.appticketera.data.model.UpdateEventRequest
import com.jwmaila.appticketera.data.repository.TicketeraRepository
import com.jwmaila.appticketera.utils.formatDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val repository: TicketeraRepository
) : ViewModel() {
    
    private val _createState = MutableStateFlow<CreateEventState>(CreateEventState.Idle)
    val createState: StateFlow<CreateEventState> = _createState.asStateFlow()
    
    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _createState.value = CreateEventState.Loading
            repository.getEvent(eventId)
                .onSuccess { eventWithSeats ->
                    _createState.value = CreateEventState.EventLoaded(
                        Event(
                            id = eventWithSeats.id,
                            name = eventWithSeats.name,
                            description = eventWithSeats.description,
                            date = formatDate(eventWithSeats.date),
                            venue = eventWithSeats.venue,
                            imageUrl = eventWithSeats.imageUrl,
                            ticketPrice = eventWithSeats.ticketPrice,
                            totalSeats = eventWithSeats.seats.size,
                            createdAt = "",
                            updatedAt = ""
                        )
                    )
                }
                .onFailure { error ->
                    _createState.value = CreateEventState.Error(error.message ?: "Error al cargar evento")
                }
        }
    }
    
    fun createEvent(
        name: String,
        description: String?,
        date: String,
        venue: String,
        ticketPrice: Double,
        totalSeats: Int,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _createState.value = CreateEventState.Loading
            val normalizedDate = normalizeDate(date)
            repository.createEvent(
                CreateEventRequest(
                    name = name,
                    description = description,
                    date = normalizedDate,
                    venue = venue,
                    ticketPrice = ticketPrice,
                    imageUrl = null
                ),
                totalSeats = totalSeats,
                imageUri = imageUri
            )
                .onSuccess {
                    _createState.value = CreateEventState.Success
                }
                .onFailure { error ->
                    _createState.value = CreateEventState.Error(error.message ?: "Error al crear evento")
                }
        }
    }
    
    fun updateEvent(
        eventId: String,
        name: String,
        description: String?,
        date: String,
        venue: String,
        ticketPrice: Double,
        totalSeats: Int,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _createState.value = CreateEventState.Loading
            val normalizedDate = normalizeDate(date)
            repository.updateEvent(
                id = eventId,
                request = UpdateEventRequest(
                    name = name,
                    description = description,
                    date = normalizedDate,
                    venue = venue,
                    ticketPrice = ticketPrice,
                    imageUrl = null
                ),
                imageUri = imageUri
            )
                .onSuccess {
                    _createState.value = CreateEventState.Success
                }
                .onFailure { error ->
                    _createState.value = CreateEventState.Error(error.message ?: "Error al actualizar evento")
                }
        }
    }
}

private fun normalizeDate(input: String): String {
    return if (input.contains("T")) {
        input
    } else {
        "${input}T00:00:00Z"
    }
}

sealed class CreateEventState {
    data object Idle : CreateEventState()
    data object Loading : CreateEventState()
    data class EventLoaded(val event: Event) : CreateEventState()
    data object Success : CreateEventState()
    data class Error(val message: String) : CreateEventState()
}
