package com.jwmaila.appticketera.ui.screens.qr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jwmaila.appticketera.data.model.VerifyTicketResponse
import com.jwmaila.appticketera.data.repository.TicketeraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanQrViewModel @Inject constructor(
    private val repository: TicketeraRepository
) : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    fun verify(qrCode: String) {
        viewModelScope.launch {
            _scanState.value = ScanState.Loading
            repository.verifyTicket(qrCode)
                .onSuccess { response ->
                    _scanState.value = ScanState.Success(response)
                }
                .onFailure { error ->
                    _scanState.value = ScanState.Error(error.message ?: "Error al verificar")
                }
        }
    }

    fun reset() {
        _scanState.value = ScanState.Idle
    }
}

sealed class ScanState {
    data object Idle : ScanState()
    data object Loading : ScanState()
    data class Success(val response: VerifyTicketResponse) : ScanState()
    data class Error(val message: String) : ScanState()
}
