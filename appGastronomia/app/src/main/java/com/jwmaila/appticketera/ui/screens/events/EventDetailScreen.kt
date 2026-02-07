package com.jwmaila.appticketera.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jwmaila.appticketera.data.model.Seat
import com.jwmaila.appticketera.ui.screens.payment.PaymentViewModel
import com.jwmaila.appticketera.ui.screens.payment.PaymentState
import com.jwmaila.appticketera.utils.formatDate
import com.jwmaila.appticketera.utils.formatPrice
import com.jwmaila.appticketera.utils.ImageUtils
import com.jwmaila.appticketera.ui.theme.*
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    onPurchaseSuccess: () -> Unit,
    viewModel: EventsViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val eventDetailState by viewModel.eventDetailState.collectAsState()
    val paymentState by paymentViewModel.paymentState.collectAsState()
    var selectedSeats by remember { mutableStateOf(setOf<String>()) }
    var paymentIntentId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var paymentErrorMessage by remember { mutableStateOf<String?>(null) }
    
    // Configurar PaymentSheet de Stripe
    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> {
                // Pago exitoso, confirmar en el backend
                paymentIntentId?.let { id ->
                    paymentViewModel.confirmPayment(id, selectedSeats.toList())
                }
            }
            is PaymentSheetResult.Canceled -> {
                paymentViewModel.resetState()
                paymentErrorMessage = "Pago cancelado"
            }
            is PaymentSheetResult.Failed -> {
                paymentViewModel.resetState()
                paymentErrorMessage = result.error.localizedMessage ?: "Error al procesar el pago"
            }
        }
    }
    
    LaunchedEffect(eventId) {
        viewModel.loadEventDetail(eventId)
    }
    
    // Observar el estado del pago
    LaunchedEffect(paymentState) {
        when (val state = paymentState) {
            is PaymentState.PaymentIntentCreated -> {
                // Abrir Stripe PaymentSheet con el clientSecret
                paymentIntentId = state.paymentIntentId
                
                paymentSheet.presentWithPaymentIntent(
                    state.clientSecret,
                    PaymentSheet.Configuration(
                        merchantDisplayName = "Ticketera App"
                    )
                )
            }
            is PaymentState.Success -> {
                onPurchaseSuccess()
                paymentViewModel.resetState()
                selectedSeats = setOf()
            }
            is PaymentState.Error -> {
                paymentErrorMessage = state.message
            }
            else -> {}
        }
    }

    LaunchedEffect(paymentErrorMessage) {
        paymentErrorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            paymentErrorMessage = null
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Evento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBlue,
                    titleContentColor = LightBlueGray,
                    navigationIconContentColor = LightBlueGray
                )
            )
        },
        bottomBar = {
            if (eventDetailState is EventDetailState.Success) {
                BottomAppBar(
                    containerColor = DarkBlue,
                    contentColor = LightBlueGray
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${selectedSeats.size} asientos",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            val totalPrice = (eventDetailState as EventDetailState.Success).event.ticketPrice * selectedSeats.size
                            Text(
                                text = formatPrice(totalPrice),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = {
                                paymentViewModel.createPaymentIntent(
                                    eventId,
                                    selectedSeats.toList()
                                )
                            },
                            enabled = selectedSeats.isNotEmpty() && paymentState !is PaymentState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MediumBlue,
                                disabledContainerColor = LightGray
                            )
                        ) {
                            Text("Comprar")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when (val state = eventDetailState) {
            is EventDetailState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MediumBlue)
                }
            }
            
            is EventDetailState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Imagen
                    AsyncImage(
                        model = ImageUtils.getFullImageUrl(state.event.imageUrl) ?: "https://via.placeholder.com/800x400",
                        contentDescription = state.event.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                    
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Título
                        Text(
                            text = state.event.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Info básica
                        InfoRow(icon = Icons.Default.CalendarToday, text = formatDate(state.event.date))
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(icon = Icons.Default.LocationOn, text = state.event.venue)
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(icon = Icons.Default.AttachMoney, text = formatPrice(state.event.ticketPrice))
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Descripción
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        state.event.description?.let { desc ->
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Selección de asientos
                        Text(
                            text = "Selecciona tus asientos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Leyenda
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            LegendItem(color = LightGray, text = "Disponible")
                            LegendItem(color = Gray, text = "Ocupado")
                            LegendItem(color = MediumBlue, text = "Seleccionado")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Grid de asientos (10x10)
                        SeatGrid(
                            seats = state.event.seats,
                            selectedSeats = selectedSeats,
                            onSeatClick = { seatId ->
                                selectedSeats = if (selectedSeats.contains(seatId)) {
                                    selectedSeats - seatId
                                } else {
                                    selectedSeats + seatId
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(80.dp)) // Espacio para el bottom bar
                    }
                }
                
            }
            
            is EventDetailState.Error -> {
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
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadEventDetail(eventId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MediumBlue
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MediumBlue,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray
        )
    }
}

@Composable
private fun LegendItem(color: androidx.compose.ui.graphics.Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SeatGrid(
    seats: List<Seat>,
    selectedSeats: Set<String>,
    onSeatClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(10),
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(seats) { seat ->
            SeatItem(
                seat = seat,
                isSelected = selectedSeats.contains(seat.id),
                onClick = { if (!seat.isOccupied) onSeatClick(seat.id) }
            )
        }
    }
}

@Composable
private fun SeatItem(
    seat: Seat,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        seat.isOccupied -> Gray
        isSelected -> MediumBlue
        else -> LightGray
    }
    
    val seatNumber = "${('A' + seat.row)}${seat.column}"
    
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable(enabled = !seat.isOccupied) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!seat.isOccupied || isSelected) {
            Text(
                text = seatNumber,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) androidx.compose.ui.graphics.Color.White else DarkBlue
            )
        }
    }
}

