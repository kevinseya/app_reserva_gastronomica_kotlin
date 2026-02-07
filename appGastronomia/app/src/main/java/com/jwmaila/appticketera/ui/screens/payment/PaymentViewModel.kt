package com.jwmaila.appticketera.ui.screens.payment

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
class PaymentViewModel @Inject constructor(
    private val repository: TicketeraRepository
) : ViewModel() {
    
    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()
    
    fun createPaymentIntent(eventId: String, seatIds: List<String>) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            repository.createPaymentIntent(eventId, seatIds)
                .onSuccess { response ->
                    _paymentState.value = PaymentState.PaymentIntentCreated(
                        response.clientSecret,
                        response.paymentIntentId
                    )
                }
                .onFailure { error ->
                    _paymentState.value = PaymentState.Error(error.message ?: "Error al crear intención de pago")
                }
        }
    }
    
    fun confirmPayment(paymentIntentId: String, seatIds: List<String>) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            repository.confirmPayment(paymentIntentId, seatIds)
                .onSuccess { tickets ->
                    _paymentState.value = PaymentState.Success(tickets.size)
                }
                .onFailure { error ->
                    _paymentState.value = PaymentState.Error(error.message ?: "Error al confirmar pago")
                }
        }
    }
    
    fun resetState() {
        _paymentState.value = PaymentState.Idle
    }
}

sealed class PaymentState {
    data object Idle : PaymentState()
    data object Loading : PaymentState()
    data class PaymentIntentCreated(
        val clientSecret: String,
        val paymentIntentId: String
    ) : PaymentState()
    data class Success(val ticketCount: Int) : PaymentState()
    data class Error(val message: String) : PaymentState()
}
