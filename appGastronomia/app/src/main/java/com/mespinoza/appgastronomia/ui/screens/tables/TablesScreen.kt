package com.mespinoza.appgastronomia.ui.screens.tables

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mespinoza.appgastronomia.data.model.Table
import com.mespinoza.appgastronomia.ui.theme.NearBlack

@Composable
fun TablesScreen(
    onNavigateToReservations: () -> Unit,
    viewModel: TablesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreate by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Mesas", modifier = Modifier.padding(bottom = 12.dp))

        // Admin quick actions: create single table or generate tables for an event
        Button(onClick = { showCreate = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Crear Mesa")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected event to configure
        var eventId by remember { mutableStateOf("") }
        var expandedEvents by remember { mutableStateOf(false) }
        // load events for admin selection
        LaunchedEffect(Unit) { viewModel.loadEvents() }
        val events = viewModel.events.collectAsState().value
        var tablesCount by remember { mutableStateOf(6) }
        var capacity by remember { mutableStateOf(4) }

        // Event selector
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = events.firstOrNull { it.id == eventId }?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Evento (admin)") },
                modifier = Modifier.fillMaxWidth()
                    .clickable { expandedEvents = true }
            )
            DropdownMenu(expanded = expandedEvents, onDismissRequest = { expandedEvents = false }) {
                if (events.isEmpty()) {
                    DropdownMenuItem(text = { Text("Cargando eventos...") }, onClick = { })
                } else {
                    events.forEach { ev ->
                        DropdownMenuItem(text = { Text(ev.name) }, onClick = {
                            eventId = ev.id
                            expandedEvents = false
                        })
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = tablesCount.toString(), onValueChange = { tablesCount = it.toIntOrNull() ?: 6 }, label = { Text("Cantidad mesas") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = capacity.toString(), onValueChange = { capacity = it.toIntOrNull() ?: 4 }, label = { Text("Capacidad") }, modifier = Modifier.weight(1f))
        }

        Button(onClick = {
            if (eventId.isNotBlank()) {
                viewModel.generateTables(eventId, tablesCount, capacity)
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Generar mesas para evento")
        }

        Spacer(modifier = Modifier.height(12.dp))

        var showEdit by remember { mutableStateOf(false) }

        Button(onClick = {
            if (eventId.isNotBlank()) {
                viewModel.loadEventTables(eventId)
                showEdit = true
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Editar mesas del evento")
        }

        if (showEdit) {
            EventTablesEditor(
                eventId = eventId,
                tables = viewModel.eventTables.collectAsState().value,
                onClose = { showEdit = false },
                onUpdate = { tableId, cap, price, x, y, rotation -> viewModel.updateEventTable(eventId, tableId, cap, price, x, y, rotation) },
                onDelete = { tableId -> viewModel.deleteEventTable(eventId, tableId) }
            )
        }

        if (showCreate) {
            CreateTableDialog(
                onDismiss = { showCreate = false },
                onCreate = { number, seats, location ->
                    viewModel.createTable(number, seats, location)
                    showCreate = false
                }
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(uiState.tables) { table ->
                TableRow(table = table, onClick = { onNavigateToReservations() })
            }
        }
    }
}

@Composable
fun TableRow(table: Table, onClick: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)
        .clickable { onClick() }) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Mesa ${table.number}", color = NearBlack)
            Text(text = "${table.seats} puestos", color = NearBlack)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTableDialog(onDismiss: () -> Unit, onCreate: (Int, Int, String?) -> Unit) {
    var numberText by remember { mutableStateOf(1) }
    var seatsText by remember { mutableStateOf(4) }
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Mesa") },
        text = {
            Column {
                OutlinedTextField(value = numberText.toString(), onValueChange = { numberText = it.toIntOrNull() ?: 1 }, label = { Text("Número") })
                OutlinedTextField(value = seatsText.toString(), onValueChange = { seatsText = it.toIntOrNull() ?: 4 }, label = { Text("Puestos") })
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Ubicación (opcional)") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(numberText, seatsText, if (location.isBlank()) null else location) }) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
