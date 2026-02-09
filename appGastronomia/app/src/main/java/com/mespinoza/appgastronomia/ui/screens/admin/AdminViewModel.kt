package com.mespinoza.appgastronomia.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mespinoza.appgastronomia.data.model.Event
import com.mespinoza.appgastronomia.data.repository.GastronomiaRepository
import kotlinx.coroutines.async
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: GastronomiaRepository
) : ViewModel() {
    
    private val _eventsState = MutableStateFlow<AdminEventsState>(AdminEventsState.Loading)
    val eventsState: StateFlow<AdminEventsState> = _eventsState.asStateFlow()
    
    init {
        loadEvents()
    }
    
    fun loadEvents() {
        viewModelScope.launch {
            _eventsState.value = AdminEventsState.Loading
            val res = repository.getEvents()
            if (res.isFailure) {
                _eventsState.value = AdminEventsState.Error(res.exceptionOrNull()?.message ?: "Error al cargar eventos")
                return@launch
            }

            val events = res.getOrNull() ?: emptyList()

            // Para cada evento, cargar sus mesas y sumar asientos
            val displays = events.map { event ->
                async {
                    val tablesRes = repository.getEventTables(event.id)
                    val seatsCount = if (tablesRes.isSuccess) {
                        tablesRes.getOrNull()?.sumOf { it.seats.size } ?: 0
                    } else 0
                    AdminEventDisplay(event, seatsCount)
                }
            }.map { it.await() }

            _eventsState.value = AdminEventsState.Success(displays)
        }
    }
    
    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
                .onSuccess {
                    loadEvents() // Recargar lista después de eliminar
                }
                .onFailure { error ->
                    _eventsState.value = AdminEventsState.Error(error.message ?: "Error al eliminar evento")
                }
        }
    }
}

sealed class AdminEventsState {
    data object Loading : AdminEventsState()
    data class Success(val events: List<AdminEventDisplay>) : AdminEventsState()
    data class Error(val message: String) : AdminEventsState()
}

data class AdminEventDisplay(
    val event: Event,
    val seatsCount: Int
)
