package com.jwmaila.appticketera.ui.screens.tickets

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jwmaila.appticketera.data.model.Ticket
import com.jwmaila.appticketera.data.model.TicketStatus
import com.jwmaila.appticketera.utils.formatDate
import com.jwmaila.appticketera.utils.formatPrice
import com.jwmaila.appticketera.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    onNavigateToTicketDetail: (String) -> Unit,
    viewModel: TicketsViewModel = hiltViewModel()
) {
    val ticketsState by viewModel.ticketsState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadMyTickets()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Mis Tickets",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MediumBlue,
                    titleContentColor = White
                )
            )
        }
    ) { paddingValues ->
        when (val state = ticketsState) {
            is TicketsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is TicketsState.Success -> {
                if (state.tickets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No tienes tickets aún",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.tickets) { ticket ->
                            TicketCard(
                                ticket = ticket,
                                onClick = { onNavigateToTicketDetail(ticket.id) }
                            )
                        }
                    }
                }
            }
            
            is TicketsState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Error al cargar tickets",
                            style = MaterialTheme.typography.titleMedium,
                            color = ErrorRed
                        )
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadMyTickets() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TicketCard(
    ticket: Ticket,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de estado
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (ticket.status) {
                    TicketStatus.PAID -> SuccessGreen
                    TicketStatus.PENDING -> WarningOrange
                    TicketStatus.CANCELLED -> ErrorRed
                    TicketStatus.USED -> Gray
                },
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (ticket.status) {
                            TicketStatus.PAID -> Icons.Default.CheckCircle
                            TicketStatus.PENDING -> Icons.Default.Schedule
                            TicketStatus.CANCELLED -> Icons.Default.Cancel
                            TicketStatus.USED -> Icons.Default.Done
                        },
                        contentDescription = null,
                        tint = White
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ticket.event?.name ?: "Evento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Asiento: Fila ${ticket.seat?.row}, N° ${ticket.seat?.column}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ticket.status.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (ticket.status) {
                        TicketStatus.PAID -> SuccessGreen
                        TicketStatus.PENDING -> WarningOrange
                        TicketStatus.CANCELLED -> ErrorRed
                        TicketStatus.USED -> Gray
                    }
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                ticket.event?.let { event ->
                    Text(
                        text = formatPrice(event.ticketPrice),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                }
                if (ticket.event?.date != null) {
                    Text(
                        text = formatDate(ticket.event.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray
                    )
                }
            }
        }
    }
}
