package com.mespinoza.appgastronomia.ui.screens.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mespinoza.appgastronomia.data.model.Reservation
import com.mespinoza.appgastronomia.data.repository.GastronomiaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReservationsUiState(val reservations: List<Reservation> = emptyList())

@HiltViewModel
class ReservationsViewModel @Inject constructor(
    private val repository: GastronomiaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReservationsUiState())
    val uiState: StateFlow<ReservationsUiState> = _uiState

    init {
        loadReservations()
    }

    private fun loadReservations() {
        viewModelScope.launch {
            val res = repository.getMyReservations()
            if (res.isSuccess) {
                _uiState.value = ReservationsUiState(reservations = res.getOrNull() ?: emptyList())
            }
        }
    }

    fun createReservation(datetime: String, partySize: Int, tableId: String?) {
        viewModelScope.launch {
            val req = com.mespinoza.appgastronomia.data.model.CreateReservationRequest(
                tableId = tableId,
                datetime = datetime,
                partySize = partySize
            )
            val res = repository.createReservation(req)
            if (res.isSuccess) {
                loadReservations()
            }
        }
    }
}
