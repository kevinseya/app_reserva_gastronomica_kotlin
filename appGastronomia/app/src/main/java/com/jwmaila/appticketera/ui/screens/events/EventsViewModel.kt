package com.jwmaila.appticketera.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jwmaila.appticketera.data.model.Event
import com.jwmaila.appticketera.data.model.EventWithSeats
import com.jwmaila.appticketera.data.repository.TicketeraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repository: TicketeraRepository
) : ViewModel() {
    
    private val _eventsState = MutableStateFlow<EventsState>(EventsState.Loading)
    val eventsState: StateFlow<EventsState> = _eventsState.asStateFlow()
    
    private val _eventDetailState = MutableStateFlow<EventDetailState>(EventDetailState.Loading)
    val eventDetailState: StateFlow<EventDetailState> = _eventDetailState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private var allEvents: List<Event> = emptyList()
    
    init {
        loadEvents()
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterEvents(query)
    }
    
    private fun filterEvents(query: String) {
        if (query.isBlank()) {
            _eventsState.value = EventsState.Success(allEvents)
        } else {
            val filtered = allEvents.filter { event ->
                event.name.contains(query, ignoreCase = true) ||
                event.venue.contains(query, ignoreCase = true) ||
                event.description?.contains(query, ignoreCase = true) == true
            }
            _eventsState.value = EventsState.Success(filtered)
        }
    }
    
    fun loadEvents() {
        viewModelScope.launch {
            _eventsState.value = EventsState.Loading
            repository.getEvents()
                .onSuccess { events ->
                    allEvents = events
                    filterEvents(_searchQuery.value)
                }
                .onFailure { error ->
                    _eventsState.value = EventsState.Error(error.message ?: "Error desconocido")
                }
        }
    }
    
    fun loadEventDetail(eventId: String) {
        viewModelScope.launch {
            _eventDetailState.value = EventDetailState.Loading
            repository.getEvent(eventId)
                .onSuccess { event ->
                    _eventDetailState.value = EventDetailState.Success(event)
                }
                .onFailure { error ->
                    _eventDetailState.value = EventDetailState.Error(error.message ?: "Error desconocido")
                }
        }
    }
}

sealed class EventsState {
    data object Loading : EventsState()
    data class Success(val events: List<Event>) : EventsState()
    data class Error(val message: String) : EventsState()
}

sealed class EventDetailState {
    data object Loading : EventDetailState()
    data class Success(val event: EventWithSeats) : EventDetailState()
    data class Error(val message: String) : EventDetailState()
}
