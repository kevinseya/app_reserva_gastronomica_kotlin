package com.jwmaila.appticketera.ui.screens.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jwmaila.appticketera.data.model.Ticket
import com.jwmaila.appticketera.data.repository.TicketeraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketDetailViewModel @Inject constructor(
    private val repository: TicketeraRepository
) : ViewModel() {

    private val _ticketState = MutableStateFlow<TicketDetailState>(TicketDetailState.Loading)
    val ticketState: StateFlow<TicketDetailState> = _ticketState.asStateFlow()

    fun loadTicket(ticketId: String) {
        viewModelScope.launch {
            _ticketState.value = TicketDetailState.Loading
            repository.getTicketById(ticketId)
                .onSuccess { ticket ->
                    _ticketState.value = TicketDetailState.Success(ticket)
                }
                .onFailure { error ->
                    _ticketState.value = TicketDetailState.Error(error.message ?: "Error al cargar ticket")
                }
        }
    }
}

sealed class TicketDetailState {
    data object Loading : TicketDetailState()
    data class Success(val ticket: Ticket) : TicketDetailState()
    data class Error(val message: String) : TicketDetailState()
}
