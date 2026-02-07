package com.jwmaila.appticketera.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jwmaila.appticketera.data.model.Event
import com.jwmaila.appticketera.data.repository.TicketeraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: TicketeraRepository
) : ViewModel() {
    
    private val _eventsState = MutableStateFlow<AdminEventsState>(AdminEventsState.Loading)
    val eventsState: StateFlow<AdminEventsState> = _eventsState.asStateFlow()
    
    init {
        loadEvents()
    }
    
    fun loadEvents() {
        viewModelScope.launch {
            _eventsState.value = AdminEventsState.Loading
            repository.getEvents()
                .onSuccess { events ->
                    _eventsState.value = AdminEventsState.Success(events)
                }
                .onFailure { error ->
                    _eventsState.value = AdminEventsState.Error(error.message ?: "Error al cargar eventos")
                }
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
    data class Success(val events: List<Event>) : AdminEventsState()
    data class Error(val message: String) : AdminEventsState()
}
