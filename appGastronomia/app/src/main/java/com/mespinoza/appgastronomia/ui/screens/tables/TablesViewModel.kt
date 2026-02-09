package com.mespinoza.appgastronomia.ui.screens.tables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mespinoza.appgastronomia.data.model.Table
import com.mespinoza.appgastronomia.data.repository.GastronomiaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TablesUiState(val tables: List<Table> = emptyList())

@HiltViewModel
class TablesViewModel @Inject constructor(
    private val repository: GastronomiaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TablesUiState())
    val uiState: StateFlow<TablesUiState> = _uiState

    private val _eventTables = MutableStateFlow<List<com.mespinoza.appgastronomia.data.model.EventTable>>(emptyList())
    val eventTables: StateFlow<List<com.mespinoza.appgastronomia.data.model.EventTable>> = _eventTables
    private val _events = MutableStateFlow<List<com.mespinoza.appgastronomia.data.model.Event>>(emptyList())
    val events: StateFlow<List<com.mespinoza.appgastronomia.data.model.Event>> = _events

    // No cargar tablas globales automáticamente: usamos tablas por evento en el editor

    private fun loadTables() {
        viewModelScope.launch {
            val res = repository.getTables()
            if (res.isSuccess) {
                _uiState.value = TablesUiState(tables = res.getOrNull() ?: emptyList())
            }
        }
    }

    fun createTable(number: Int, seats: Int, location: String?) {
        viewModelScope.launch {
            val req = com.mespinoza.appgastronomia.data.model.CreateTableRequest(number = number, seats = seats, location = location)
            val res = repository.createTable(req)
            if (res.isSuccess) {
                loadTables()
            }
        }
    }

    fun generateTables(eventId: String, tablesCount: Int, capacity: Int) {
        viewModelScope.launch {
            val res = repository.generateTables(eventId, tablesCount, capacity)
            if (res.isSuccess) {
                // recargar las mesas del evento después de generar
                loadEventTables(eventId)
            }
        }
    }

    fun loadEventTables(eventId: String) {
        viewModelScope.launch {
            val res = repository.getEventTables(eventId)
            if (res.isSuccess) {
                _eventTables.value = res.getOrNull() ?: emptyList()
            }
        }
    }

    fun loadEvents() {
        viewModelScope.launch {
            val res = repository.getEvents()
            if (res.isSuccess) {
                _events.value = res.getOrNull() ?: emptyList()
            }
        }
    }

    fun updateEventTable(eventId: String, tableId: String, capacity: Int?, seatPrice: Double?, x: Int? = null, y: Int? = null, rotation: Int? = null) {
        viewModelScope.launch {
            val req = com.mespinoza.appgastronomia.data.remote.UpdateEventTableRequest(
                capacity = capacity,
                seatPrice = seatPrice,
                x = x,
                y = y,
                rotation = rotation
            )
            val res = repository.updateEventTable(eventId, tableId, req)
            if (res.isSuccess) {
                loadEventTables(eventId)
            }
        }
    }

    fun deleteEventTable(eventId: String, tableId: String) {
        viewModelScope.launch {
            val res = repository.deleteEventTable(eventId, tableId)
            if (res.isSuccess) {
                loadEventTables(eventId)
            }
        }
    }

    fun createEventTable(eventId: String, name: String, x: Int, y: Int, rotation: Int, capacity: Int, seatPrice: Double) {
        viewModelScope.launch {
            val req = com.mespinoza.appgastronomia.data.remote.CreateEventTableRequest(
                name = name,
                x = x,
                y = y,
                rotation = rotation,
                capacity = capacity,
                seatPrice = seatPrice
            )
            val res = repository.createEventTable(eventId, req)
            if (res.isSuccess) {
                loadEventTables(eventId)
            }
        }
    }

    // Synchronous suspend function to create a table and return success
    suspend fun createEventTableSuspend(eventId: String, name: String, x: Int, y: Int, rotation: Int, capacity: Int, seatPrice: Double): Boolean {
        val req = com.mespinoza.appgastronomia.data.remote.CreateEventTableRequest(
            name = name,
            x = x,
            y = y,
            rotation = rotation,
            capacity = capacity,
            seatPrice = seatPrice
        )
        val res = repository.createEventTable(eventId, req)
        return res.isSuccess
    }

    fun loadEventForEditor(eventId: String) {
        viewModelScope.launch {
            val res = repository.getEvent(eventId)
            if (res.isSuccess) {
                val event = res.getOrNull()
                if (event != null) {
                    // Convertir EventWithSeats a Event para agregarlo a _events
                    val simpleEvent = com.mespinoza.appgastronomia.data.model.Event(
                        id = event.id,
                        name = event.name,
                        description = event.description,
                        date = event.date,
                        venue = event.venue,
                        imageUrl = event.imageUrl,
                        ticketPrice = event.ticketPrice,
                        totalSeats = event.seats.size,
                        createdAt = "",
                        updatedAt = ""
                    )
                    // Agregar o reemplazar el evento en la lista
                    val currentEvents = _events.value.toMutableList()
                    val index = currentEvents.indexOfFirst { it.id == eventId }
                    if (index >= 0) {
                        currentEvents[index] = simpleEvent
                    } else {
                        currentEvents.add(simpleEvent)
                    }
                    _events.value = currentEvents
                }
            }
        }
    }

    fun generateTablesLocally(eventId: String, count: Int, capacity: Int) {
        val tables = mutableListOf<com.mespinoza.appgastronomia.data.model.EventTable>()
        val cols = kotlin.math.ceil(kotlin.math.sqrt(count.toDouble())).toInt()
        val spacing = 120 // px entre mesas

        for (i in 0 until count) {
            val row = i / cols
            val col = i % cols
            val x = col * spacing
            val y = row * spacing

            tables.add(
                com.mespinoza.appgastronomia.data.model.EventTable(
                    id = "temp_$i",
                    eventId = eventId,
                    name = "Mesa ${i + 1}",
                    x = x,
                    y = y,
                    rotation = 0,
                    capacity = capacity,
                    seatPrice = null,
                    seats = emptyList()
                )
            )
        }
        _eventTables.value = tables
    }

    fun saveTableStructure(
        eventId: String,
        tables: List<com.mespinoza.appgastronomia.data.model.EventTable>,
        positions: Map<String, androidx.compose.ui.geometry.Offset>,
        rotations: Map<String, Int>
    ) {
        viewModelScope.launch {
            if (tables.isEmpty()) return@launch

            val capacity = tables.first().capacity
            val count = tables.size

            // 1. Generar las mesas en el backend
            val genRes = repository.generateTables(eventId, count, capacity)
            if (!genRes.isSuccess) return@launch

            // 2. Recargar las mesas creadas
            val loadRes = repository.getEventTables(eventId)
            if (!loadRes.isSuccess) return@launch

            val createdTables = loadRes.getOrNull() ?: emptyList()
            if (createdTables.size != tables.size) return@launch

            // 3. Actualizar cada mesa con su posición y rotación
            for (i in tables.indices) {
                val localTable = tables[i]
                val createdTable = createdTables[i]
                val position = positions[localTable.id]
                val rotation = rotations[localTable.id] ?: 0

                if (position != null) {
                    val req = com.mespinoza.appgastronomia.data.remote.UpdateEventTableRequest(
                        capacity = capacity,
                        seatPrice = localTable.seatPrice,
                        x = position.x.toInt(),
                        y = position.y.toInt(),
                        rotation = rotation
                    )
                    repository.updateEventTable(eventId, createdTable.id, req)
                }
            }

            // 4. Recargar las mesas finales
            loadEventTables(eventId)
        }
    }
}
