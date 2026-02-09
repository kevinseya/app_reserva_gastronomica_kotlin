package com.mespinoza.appgastronomia.ui.screens.tables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mespinoza.appgastronomia.data.model.EventTable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTablesEditor(
    eventId: String,
    tables: List<EventTable>,
    onClose: () -> Unit,
    onUpdate: (tableId: String, capacity: Int?, seatPrice: Int?, x: Int?, y: Int?, rotation: Int?) -> Unit,
    onDelete: (tableId: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Editar mesas para evento $eventId") },
        text = {
            if (tables.isEmpty()) {
                Text("No hay mesas para este evento.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    items(tables) { table ->
                        EventTableEditorRow(table = table, onUpdate = { id, cap, price, x, y, rotation -> onUpdate(id, cap, price, x, y, rotation) }, onDelete = onDelete)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) { Text("Cerrar") }
        }
    )
}

@Composable
fun EventTableEditorRow(table: EventTable, onUpdate: (String, Int?, Int?, Int?, Int?, Int?) -> Unit, onDelete: (String) -> Unit) {
    var capacityText by remember { mutableStateOf(table.capacity.toString()) }
    var priceText by remember { mutableStateOf((table.seatPrice ?: 0).toString()) }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = table.name ?: "Mesa ${table.id}")
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = capacityText, onValueChange = { capacityText = it }, label = { Text("Capacidad") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Precio (centavos)") }, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val cap = capacityText.toIntOrNull()
                    val price = priceText.toIntOrNull()
                    onUpdate(table.id, cap, price, null, null, null)
                }) { Text("Guardar") }
                OutlinedButton(onClick = { onDelete(table.id) }) { Text("Eliminar") }
            }
        }
    }
}
