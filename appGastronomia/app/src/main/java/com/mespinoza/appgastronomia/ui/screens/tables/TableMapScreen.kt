package com.mespinoza.appgastronomia.ui.screens.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mespinoza.appgastronomia.data.model.EventTable
import com.mespinoza.appgastronomia.ui.theme.MediumBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableMapScreen(
    eventId: String,
    onBack: () -> Unit,
    viewModel: TableMapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedSeatIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(eventId) { viewModel.loadTables(eventId) }

    val paymentState by viewModel.paymentState.collectAsState()

    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> {
                // confirm payment on backend
                if (paymentState is com.mespinoza.appgastronomia.ui.screens.tables.TableMapPaymentState.PaymentIntentCreated) {
                    val s = paymentState as com.mespinoza.appgastronomia.ui.screens.tables.TableMapPaymentState.PaymentIntentCreated
                    viewModel.confirmPayment(s.paymentIntentId, s.orderId, s.reservationId)
                }
            }
            is PaymentSheetResult.Canceled -> {
                // noop
            }
            is PaymentSheetResult.Failed -> {
                // noop
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Mapa de Mesas") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
        })
    }, bottomBar = {
        BottomAppBar {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${selectedSeatIds.size} asientos seleccionados")
                Button(onClick = { viewModel.checkout(eventId, selectedSeatIds.toList()) }, enabled = selectedSeatIds.isNotEmpty()) {
                    Text("Comprar")
                }
            }
        }
    }) { padding ->
        // Observe payment state and present sheet
        LaunchedEffect(paymentState) {
            when (val ps = paymentState) {
                is com.mespinoza.appgastronomia.ui.screens.tables.TableMapPaymentState.PaymentIntentCreated -> {
                    paymentSheet.presentWithPaymentIntent(ps.clientSecret, PaymentSheet.Configuration(merchantDisplayName = "Sabores"))
                }
                is com.mespinoza.appgastronomia.ui.screens.tables.TableMapPaymentState.Success -> {
                    // optionally navigate to tickets or show success
                }
                else -> {}
            }
        }
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
            when (val state = uiState) {
                is TableMapUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is TableMapUiState.Success -> {
                    // Simple grid: show tables in rows
                    val tables = state.tables
                    Column(Modifier.fillMaxSize()) {
                        tables.chunked(3).forEach { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                row.forEach { table -> TableView(table = table, onSeatToggle = { seatId, selected ->
                                    selectedSeatIds = if (selected) selectedSeatIds + seatId else selectedSeatIds - seatId
                                }) }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                is TableMapUiState.Error -> Text(text = state.message, color = Color.Red)
            }
        }
    }
}

@Composable
fun TableView(table: EventTable, onSeatToggle: (String, Boolean) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFF1B5E20)), contentAlignment = Alignment.Center) {
            Text(table.name ?: "Mesa", color = Color.White)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            table.seats.forEach { seat ->
                var selected by remember { mutableStateOf(false) }
                Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(if (selected) MediumBlue else if (seat.reservationId != null) Color.Gray else Color.Yellow).clickable {
                    if (seat.reservationId == null) {
                        selected = !selected
                        onSeatToggle(seat.id, selected)
                    }
                })
            }
        }
    }
}
