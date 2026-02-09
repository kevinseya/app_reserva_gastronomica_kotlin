package com.mespinoza.appgastronomia.ui.screens.reservations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mespinoza.appgastronomia.data.model.Reservation
import com.mespinoza.appgastronomia.ui.theme.NearBlack

@Composable
fun ReservationsScreen(
    viewModel: ReservationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreate by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Mis Reservas", modifier = Modifier.padding(bottom = 12.dp))

        Button(onClick = { showCreate = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Crear Reserva")
        }

        if (showCreate) {
            CreateReservationDialog(
                onDismiss = { showCreate = false },
                onCreate = { datetime, partySize, tableId ->
                    viewModel.createReservation(datetime, partySize, tableId)
                    showCreate = false
                }
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(uiState.reservations) { r ->
                ReservationRow(reservation = r)
            }
        }
    }
}

@Composable
fun ReservationRow(reservation: Reservation) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Fecha: ${reservation.datetime}", color = NearBlack)
            Text(text = "Personas: ${reservation.partySize}", color = NearBlack)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReservationDialog(onDismiss: () -> Unit, onCreate: (String, Int, String?) -> Unit) {
    var datetime by remember { mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) }
    var partySizeText by remember { mutableStateOf("1") }
    var tableId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Reserva") },
        text = {
            Column {
                OutlinedTextField(value = datetime, onValueChange = { datetime = it }, label = { Text("Fecha y hora (ISO)") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text))
                OutlinedTextField(value = partySizeText, onValueChange = { partySizeText = it }, label = { Text("Personas") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = tableId, onValueChange = { tableId = it }, label = { Text("ID Mesa (opcional)") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(datetime, partySizeText.toIntOrNull() ?: 1, if (tableId.isBlank()) null else tableId) }) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
