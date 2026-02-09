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
class TicketDetailViewModel @Inject constructor(
    private val repository: GastronomiaRepository
) : ViewModel() {

    private val _ticketState = MutableStateFlow<TicketDetailState>(TicketDetailState.Loading)
    val ticketState: StateFlow<TicketDetailState> = _ticketState.asStateFlow()

    fun loadTicket(ticketId: String) {
        viewModelScope.launch {
            _ticketState.value = TicketDetailState.Loading
            
            // Cargamos el ticket individual y luego buscamos todos los tickets de esa misma compra (paymentIntentId)
            val ticketResult = repository.getTicketById(ticketId)
            val allTicketsResult = repository.getMyTickets()

            if (ticketResult.isSuccess && allTicketsResult.isSuccess) {
                val currentTicket = ticketResult.getOrNull()!!
                val allTickets = allTicketsResult.getOrNull()!!
                
                // Agrupamos por el ID de pago para mostrar la compra completa
                val relatedTickets = allTickets.filter { 
                    it.event?.id == currentTicket.event?.id && it.status == currentTicket.status
                }
                
                // Si por alguna razón no hay match (data antigua), mostramos al menos el actual
                val finalGroup = if (relatedTickets.isNotEmpty()) relatedTickets else listOf(currentTicket)
                
                _ticketState.value = TicketDetailState.Success(finalGroup)
            } else {
                _ticketState.value = TicketDetailState.Error("Error al cargar detalles de la compra")
            }
        }
    }
}

sealed class TicketDetailState {
    data object Loading : TicketDetailState()
    data class Success(val tickets: List<Ticket>) : TicketDetailState()
    data class Error(val message: String) : TicketDetailState()
}
