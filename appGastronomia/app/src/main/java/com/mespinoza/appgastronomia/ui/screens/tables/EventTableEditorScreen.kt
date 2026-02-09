package com.mespinoza.appgastronomia.ui.screens.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mespinoza.appgastronomia.data.model.EventTable
import com.mespinoza.appgastronomia.utils.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTableEditorScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    viewModel: TablesViewModel = hiltViewModel()
) {
    var loading by remember { mutableStateOf(true) }
    var eventName by remember { mutableStateOf("") }
    var localTables by remember { mutableStateOf<List<EventTable>>(emptyList()) }
    var selectedTable by remember { mutableStateOf<EventTable?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        viewModel.loadEvents()
        loading = false
    }

    val events by viewModel.events.collectAsState()
    LaunchedEffect(events) {
        events.firstOrNull { it.id == eventId }?.let { eventName = it.name }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar mesas: ${eventName.ifBlank { "..." }}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Text("Volver") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(12.dp)) {
            // Controles: Filas, Columnas, Asientos
            var rowsStr by remember { mutableStateOf("3") }
            var colsStr by remember { mutableStateOf("3") }
            var capacityStr by remember { mutableStateOf("4") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = rowsStr,
                    onValueChange = { rowsStr = it },
                    label = { Text("Filas") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = colsStr,
                    onValueChange = { colsStr = it },
                    label = { Text("Columnas") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = capacityStr,
                    onValueChange = { capacityStr = it },
                    label = { Text("Asientos") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Button(
                    onClick = {
                        val rows = rowsStr.toIntOrNull() ?: 3
                        val cols = colsStr.toIntOrNull() ?: 3
                        val capacity = capacityStr.toIntOrNull() ?: 4
                        
                        // Generar mesas LOCALMENTE con espaciado amplio
                        val generated = mutableListOf<EventTable>()
                        var idx = 1
                        val spacing = 200 // Espaciado amplio para que se vean bien
                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                generated.add(
                                    EventTable(
                                        id = "local-${System.currentTimeMillis()}-$idx",
                                        eventId = eventId,
                                        name = "Mesa $idx",
                                        x = c * spacing + 30,
                                        y = r * spacing + 30,
                                        rotation = 0,
                                        capacity = capacity,
                                        seatPrice = 0.0, // Precio por defecto (decimal)
                                        seats = emptyList()
                                    )
                                )
                                idx++
                            }
                        }
                        localTables = generated
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("Generar")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas visual para colocar mesas con scroll
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Ocupa el espacio disponible
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (loading) {
                    Text("Cargando...", modifier = Modifier.align(Alignment.Center))
                } else if (localTables.isEmpty()) {
                    Text(
                        "Genera las mesas para comenzar",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // Posiciones y snap to grid
                    val positions = remember { mutableStateMapOf<String, Offset>() }
                    val gridSize = 20

                    fun snapToGrid(value: Float): Float {
                        return (value / gridSize).toInt() * gridSize.toFloat()
                    }

                    // Inicializar posiciones desde coordenadas de la tabla
                    LaunchedEffect(localTables) {
                        localTables.forEach { t ->
                            if (!positions.containsKey(t.id)) {
                                positions[t.id] = Offset(t.x.toFloat(), t.y.toFloat())
                            }
                        }
                    }

                    // Calcular el tamaño necesario del canvas
                    val maxX = (localTables.maxOfOrNull { positions[it.id]?.x ?: 0f } ?: 0f) + 200
                    val maxY = (localTables.maxOfOrNull { positions[it.id]?.y ?: 0f } ?: 0f) + 200

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState())
                            .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
                                .size(
                                    width = maxX.dp.coerceAtLeast(400.dp),
                                    height = maxY.dp.coerceAtLeast(500.dp)
                                )
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                        // Renderizar cada mesa
                        localTables.forEach { table ->
                            val pos = positions[table.id] ?: Offset.Zero
                            var offset by remember { mutableStateOf(pos) }
                            var isDragging by remember { mutableStateOf(false) }

                            LaunchedEffect(positions[table.id]) {
                                offset = positions[table.id] ?: offset
                            }

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
                                    .size(102.dp) // Otro 20% más pequeñas (128 * 0.8 = 102)
                                    .pointerInput(table.id) {
                                        detectDragGestures(
                                            onDragStart = {
                                                isDragging = true
                                            },
                                            onDragEnd = {
                                                isDragging = false
                                                // Aplicar snap al soltar
                                                offset = Offset(
                                                    snapToGrid(offset.x),
                                                    snapToGrid(offset.y)
                                                )
                                                positions[table.id] = offset
                                            },
                                            onDragCancel = {
                                                isDragging = false
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                // Actualizar posición fluidamente durante el drag
                                                offset = Offset(
                                                    (offset.x + dragAmount.x).coerceAtLeast(0f),
                                                    (offset.y + dragAmount.y).coerceAtLeast(0f)
                                                )
                                            }
                                        )
                                    }
                                    .pointerInput(table.id) {
                                        // Detectar tap (click) solo si no está arrastrando
                                        detectTapGestures {
                                            if (!isDragging) {
                                                selectedTable = table
                                                showEditDialog = true
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // Mesa circular con asientos alrededor
                                androidx.compose.foundation.Canvas(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    val cx = size.width / 2
                                    val cy = size.height / 2
                                    val radius = kotlin.math.min(size.width, size.height) * 0.25f // Mesa más grande

                                    // Círculo de la mesa
                                    drawCircle(
                                        color = androidx.compose.ui.graphics.Color(0xFFFF9800),
                                        radius = radius,
                                        center = androidx.compose.ui.geometry.Offset(cx, cy)
                                    )

                                    // Borde de la mesa para más profundidad
                                    drawCircle(
                                        color = androidx.compose.ui.graphics.Color(0xFFF57C00),
                                        radius = radius,
                                        center = androidx.compose.ui.geometry.Offset(cx, cy),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                                    )

                                    // Asientos alrededor - MÁS GRANDES Y VISIBLES
                                    val angleStep = 360f / kotlin.math.max(1, table.capacity)
                                    val seatDistance = radius + (size.minDimension * 0.10f) // Mucho más pegados (antes 0.15f)
                                    for (i in 0 until table.capacity) {
                                        val angle = Math.toRadians((i * angleStep).toDouble())
                                        val seatX = cx + seatDistance * kotlin.math.cos(angle).toFloat()
                                        val seatY = cy + seatDistance * kotlin.math.sin(angle).toFloat()
                                        
                                        // Asiento con sombra
                                        drawCircle(
                                            color = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                            radius = 14f, // Asientos mucho más grandes
                                            center = androidx.compose.ui.geometry.Offset(seatX, seatY)
                                        )
                                        // Borde del asiento
                                        drawCircle(
                                            color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                            radius = 12f,
                                            center = androidx.compose.ui.geometry.Offset(seatX, seatY)
                                        )
                                    }
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = table.name ?: "M",
                                        style = MaterialTheme.typography.titleMedium, // Más grande
                                        color = MaterialTheme.colorScheme.surface
                                    )
                                    Text(
                                        text = formatPrice(table.seatPrice ?: 0.0),
                                        style = MaterialTheme.typography.bodyMedium, // Más grande
                                        color = MaterialTheme.colorScheme.surface
                                    )
                                }
                            }
                        }
                        }
                    }
                    
                    // Botón para GUARDAR estructura - FUERA del scroll, siempre visible
                    val scope = rememberCoroutineScope()
                    var saving by remember { mutableStateOf(false) }
                    val context = androidx.compose.ui.platform.LocalContext.current

                    Button(
                        onClick = {
                            if (saving) return@Button
                            saving = true
                            scope.launch {
                                var okAll = true
                                for (t in localTables) {
                                    val p = positions[t.id] ?: Offset.Zero
                                    val x = p.x.toInt()
                                    val y = p.y.toInt()
                                        val created = viewModel.createEventTableSuspend(
                                        eventId = eventId,
                                        name = t.name ?: "Mesa",
                                        x = x,
                                        y = y,
                                        rotation = 0,
                                        capacity = t.capacity,
                                            seatPrice = t.seatPrice ?: 0.0
                                    )
                                    if (!created) okAll = false
                                }
                                // Recargar mesas y mostrar resultado
                                viewModel.loadEventTables(eventId)
                                saving = false
                                if (okAll) {
                                    android.widget.Toast.makeText(context, "Mesas guardadas correctamente", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Error guardando algunas mesas", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !saving,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        if (saving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (saving) "Guardando..." else "Guardar estructura")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Información de mesas generadas
            if (localTables.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Mesas generadas: ${localTables.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Toca una mesa para editar precio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Diálogo para editar mesa individual
    if (showEditDialog && selectedTable != null) {
        var editName by remember { mutableStateOf(selectedTable?.name ?: "") }
        var editPrice by remember { mutableStateOf((selectedTable?.seatPrice ?: 0.0).toString()) }
        var editCapacity by remember { mutableStateOf((selectedTable?.capacity ?: 4).toString()) }

        AlertDialog(
            onDismissRequest = { 
                showEditDialog = false
                selectedTable = null
            },
            title = { Text("Editar Mesa") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = editCapacity,
                        onValueChange = { editCapacity = it },
                        label = { Text("Capacidad (asientos)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it },
                        label = { Text("Precio por asiento") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        prefix = { Text("$") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Actualizar la mesa en la lista local
                        val updatedTables = localTables.map { table ->
                            if (table.id == selectedTable?.id) {
                                table.copy(
                                    name = editName.ifBlank { table.name },
                                    capacity = editCapacity.toIntOrNull() ?: table.capacity,
                                    seatPrice = editPrice.toDoubleOrNull() ?: table.seatPrice
                                )
                            } else {
                                table
                            }
                        }
                        localTables = updatedTables
                        showEditDialog = false
                        selectedTable = null
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        selectedTable = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}