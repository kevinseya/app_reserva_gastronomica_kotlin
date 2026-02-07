package com.jwmaila.appticketera.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.jwmaila.appticketera.data.model.Event
import com.jwmaila.appticketera.utils.formatDate
import com.jwmaila.appticketera.utils.formatPrice
import com.jwmaila.appticketera.utils.ImageUtils
import com.jwmaila.appticketera.ui.theme.*
import com.jwmaila.appticketera.ui.components.AppTopBar
import com.jwmaila.appticketera.data.local.UserPreferences
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsListScreen(
    onNavigateToEventDetail: (String) -> Unit,
    onNavigateToMyTickets: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToManageEvents: () -> Unit = {},
    onNavigateToManageUsers: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    viewModel: EventsViewModel = hiltViewModel()
) {
    val eventsState by viewModel.eventsState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    // Obtener preferencias de usuario para el header
    val profileViewModel: com.jwmaila.appticketera.ui.screens.profile.ProfileViewModel = hiltViewModel()
    val userPreferences = profileViewModel.userPreferences
    val userName = userPreferences.getUserName() ?: "Usuario"
    val userRole = userPreferences.getUserRole() ?: "CLIENT"

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadEvents()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Eventos",
                userName = userName,
                userRole = userRole,
                onNavigateToEvents = { },
                onNavigateToMyTickets = onNavigateToMyTickets,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToManageEvents = onNavigateToManageEvents,
                onNavigateToManageUsers = onNavigateToManageUsers,
                onNavigateToAdmin = onNavigateToAdmin
            )
        }
    ) { paddingValues ->
        when (val state = eventsState) {
            is EventsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is EventsState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Buscador
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Buscar eventos por nombre, lugar...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar", tint = MediumBlue)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = Gray)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MediumBlue,
                            unfocusedBorderColor = Gray.copy(alpha = 0.3f)
                        ),
                        singleLine = true
                    )
                    
                    // Lista de eventos o mensaje vacío
                    if (state.events.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                if (searchQuery.isEmpty()) Icons.Default.Event else Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                if (searchQuery.isEmpty()) "No hay eventos disponibles" else "No se encontraron eventos",
                                style = MaterialTheme.typography.titleMedium,
                                color = Gray
                            )
                            if (searchQuery.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Intenta con otra búsqueda",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.events) { event ->
                                EventCard(
                                    event = event,
                                    onClick = { onNavigateToEventDetail(event.id) }
                                )
                            }
                        }
                    }
                }
            }
            
            is EventsState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Error al cargar eventos",
                            style = MaterialTheme.typography.titleMedium,
                            color = ErrorRed
                        )
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadEvents() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Imagen del evento
            AsyncImage(
                model = ImageUtils.getFullImageUrl(event.imageUrl) ?: "https://via.placeholder.com/400x200",
                contentDescription = event.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Título
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Descripción
                event.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Fecha
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MediumBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDate(event.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Ubicación
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MediumBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.venue,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Precio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Desde",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray
                    )
                    Text(
                        text = formatPrice(event.ticketPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }
        }
    }
}
