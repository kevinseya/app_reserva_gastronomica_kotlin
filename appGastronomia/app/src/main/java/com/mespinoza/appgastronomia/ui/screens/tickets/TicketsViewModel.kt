package com.mespinoza.appgastronomia.ui.screens.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mespinoza.appgastronomia.data.model.Ticket
import com.mespinoza.appgastronomia.data.repository.GastronomiaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketsViewModel @Inject constructor(
    private val repository: GastronomiaRepository
) : ViewModel() {
    
    private val _ticketsState = MutableStateFlow<TicketsState>(TicketsState.Loading)
    val ticketsState: StateFlow<TicketsState> = _ticketsState.asStateFlow()
    
    init {
        loadMyTickets()
    }
    
    fun loadMyTickets() {
        viewModelScope.launch {
            _ticketsState.value = TicketsState.Loading
            repository.getMyTickets()
                .onSuccess { tickets ->
                    _ticketsState.value = TicketsState.Success(tickets)
                }
                .onFailure { error ->
                    _ticketsState.value = TicketsState.Error(error.message ?: "Error desconocido")
                }
        }
    }
}

sealed class TicketsState {
    data object Loading : TicketsState()
    data class Success(val tickets: List<Ticket>) : TicketsState()
    data class Error(val message: String) : TicketsState()
}
