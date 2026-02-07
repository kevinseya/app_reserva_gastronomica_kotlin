package com.jwmaila.appticketera.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jwmaila.appticketera.data.repository.TicketeraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminStatsViewModel @Inject constructor(
    private val repository: TicketeraRepository
) : ViewModel() {
    
    private val _statsState = MutableStateFlow<AdminStatsState>(AdminStatsState.Loading)
    val statsState: StateFlow<AdminStatsState> = _statsState.asStateFlow()
    
    init {
        loadStats()
    }
    
    fun loadStats() {
        viewModelScope.launch {
            _statsState.value = AdminStatsState.Loading
            
            // Cargar eventos
            val eventsResult = repository.getEvents()
            val usersResult = repository.getUsers()
                val ticketsResult = repository.getAllTickets()
            
            if (eventsResult.isSuccess && usersResult.isSuccess && ticketsResult.isSuccess) {
                val events = eventsResult.getOrNull() ?: emptyList()
                val users = usersResult.getOrNull() ?: emptyList()
                val tickets = ticketsResult.getOrNull() ?: emptyList()
                
                val totalRevenue = tickets.sumOf { it.event?.ticketPrice ?: 0.0 }
                
                _statsState.value = AdminStatsState.Success(
                    totalEvents = events.size,
                    totalUsers = users.size,
                    totalTickets = tickets.size,
                    totalRevenue = totalRevenue
                )
            } else {
                _statsState.value = AdminStatsState.Error("Error al cargar estadísticas")
            }
        }
    }
}

sealed class AdminStatsState {
    data object Loading : AdminStatsState()
    data class Success(
        val totalEvents: Int,
        val totalUsers: Int,
        val totalTickets: Int,
        val totalRevenue: Double
    ) : AdminStatsState()
    data class Error(val message: String) : AdminStatsState()
}
