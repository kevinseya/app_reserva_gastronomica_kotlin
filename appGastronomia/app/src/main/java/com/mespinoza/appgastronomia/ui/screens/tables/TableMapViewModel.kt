package com.mespinoza.appgastronomia.ui.screens.tables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mespinoza.appgastronomia.data.model.EventTable
import com.mespinoza.appgastronomia.data.remote.PaymentIntentResponse
import com.mespinoza.appgastronomia.data.repository.GastronomiaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TableMapViewModel @Inject constructor(private val repository: GastronomiaRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<TableMapUiState>(TableMapUiState.Loading)
    val uiState: StateFlow<TableMapUiState> = _uiState.asStateFlow()

    fun loadTables(eventId: String) {
        viewModelScope.launch {
            _uiState.value = TableMapUiState.Loading
            val res = repository.getEventTables(eventId)
            res.onSuccess { tables -> _uiState.value = TableMapUiState.Success(tables) }
                .onFailure { e -> _uiState.value = TableMapUiState.Error(e.message ?: "Error") }
        }
    }
    private val _paymentState = MutableStateFlow<TableMapPaymentState>(TableMapPaymentState.Idle)
    val paymentState: StateFlow<TableMapPaymentState> = _paymentState.asStateFlow()

    fun checkout(eventId: String, selectedSeatIds: List<String>) {
        viewModelScope.launch {
            _paymentState.value = TableMapPaymentState.Loading
            try {
                // 1) create reservation
                val res = repository.createReservation(com.mespinoza.appgastronomia.data.model.CreateReservationRequest(
                    eventId = eventId,
                    tableId = null,
                    seatIds = selectedSeatIds,
                    datetime = java.time.Instant.now().toString(),
                    partySize = selectedSeatIds.size
                ))
                if (res.isFailure) throw res.exceptionOrNull()!!
                val reservation = res.getOrNull()!!

                // 2) create order for reservation (no menu items for now)
                val orderRes = repository.createOrderForReservation(reservation.id, emptyList())
                if (orderRes.isFailure) throw orderRes.exceptionOrNull()!!
                val order = orderRes.getOrNull()!!

                // 3) request payment intent for order
                val payRes = repository.payOrder(order.id)
                if (payRes.isFailure) throw payRes.exceptionOrNull()!!
                val pi = payRes.getOrNull()!!

                _paymentState.value = TableMapPaymentState.PaymentIntentCreated(pi.clientSecret, pi.paymentIntentId, order.id, reservation.id)
            } catch (e: Exception) {
                _paymentState.value = TableMapPaymentState.Error(e.message ?: "Error")
            }
        }
    }

    fun confirmPayment(paymentIntentId: String, orderId: String, reservationId: String) {
        viewModelScope.launch {
            _paymentState.value = TableMapPaymentState.Loading
            try {
                val resp = repository.confirmOrder(orderId, paymentIntentId)
                if (resp.isFailure) throw resp.exceptionOrNull()!!
                _paymentState.value = TableMapPaymentState.Success("Payment confirmed")
            } catch (e: Exception) {
                _paymentState.value = TableMapPaymentState.Error(e.message ?: "Error confirming payment")
            }
        }
    }
}

sealed class TableMapUiState {
    object Loading : TableMapUiState()
    data class Success(val tables: List<EventTable>) : TableMapUiState()
    data class Error(val message: String) : TableMapUiState()
}

sealed class TableMapPaymentState {
    object Idle : TableMapPaymentState()
    object Loading : TableMapPaymentState()
    data class PaymentIntentCreated(val clientSecret: String, val paymentIntentId: String, val orderId: String, val reservationId: String) : TableMapPaymentState()
    data class Success(val message: String) : TableMapPaymentState()
    data class Error(val message: String) : TableMapPaymentState()
}
