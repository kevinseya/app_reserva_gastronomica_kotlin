package com.mespinoza.appgastronomia.ui.screens.events

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mespinoza.appgastronomia.data.model.EventTable
import com.mespinoza.appgastronomia.data.model.TableSeat
import com.mespinoza.appgastronomia.ui.screens.payment.PaymentViewModel
import com.mespinoza.appgastronomia.ui.screens.payment.PaymentState
import com.mespinoza.appgastronomia.ui.screens.tables.TablesViewModel
import com.mespinoza.appgastronomia.utils.formatDate
import com.mespinoza.appgastronomia.utils.formatPrice
import com.mespinoza.appgastronomia.utils.ImageUtils
import com.mespinoza.appgastronomia.ui.theme.*
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    onPurchaseSuccess: () -> Unit,
    onNavigateToTableMap: (String) -> Unit,
    viewModel: EventsViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    tablesViewModel: TablesViewModel = hiltViewModel()
) {
    val eventDetailState by viewModel.eventDetailState.collectAsState()
    val paymentState by paymentViewModel.paymentState.collectAsState()
    val eventTables by tablesViewModel.eventTables.collectAsState()
    
    var selectedSeats by remember { mutableStateOf(setOf<String>()) }
    var paymentIntentId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var paymentErrorMessage by remember { mutableStateOf<String?>(null) }
    
    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> {
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
        tablesViewModel.loadEventTables(eventId)
    }
    
    LaunchedEffect(paymentState) {
        when (val state = paymentState) {
            is PaymentState.PaymentIntentCreated -> {
                paymentIntentId = state.paymentIntentId
                paymentSheet.presentWithPaymentIntent(
                    state.clientSecret,
                    PaymentSheet.Configuration(merchantDisplayName = "Ticketera App")
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
    
    val totalPrice = remember(selectedSeats, eventTables) {
        eventTables.flatMap { it.seats }
            .filter { selectedSeats.contains(it.id) }
            .sumOf { it.price }
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
                BottomAppBar(containerColor = DarkBlue, contentColor = LightBlueGray) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${selectedSeats.size} asientos", style = MaterialTheme.typography.bodyMedium)
                            Text(formatPrice(totalPrice.toDouble()), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { paymentViewModel.createPaymentIntent(eventId, selectedSeats.toList()) },
                            enabled = selectedSeats.isNotEmpty() && paymentState !is PaymentState.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = MediumBlue, disabledContainerColor = LightGray)
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
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MediumBlue)
                }
            }
            is EventDetailState.Success -> {
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())) {
                    AsyncImage(
                        model = ImageUtils.getFullImageUrl(state.event.imageUrl) ?: "https://via.placeholder.com/800x400",
                        contentDescription = state.event.name,
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        contentScale = ContentScale.Crop
                    )
                    
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(state.event.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = DarkBlue)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(icon = Icons.Default.CalendarToday, text = formatDate(state.event.date))
                        InfoRow(icon = Icons.Default.LocationOn, text = state.event.venue)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Selecciona tus asientos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            LegendItem(color = Color(0xFF4CAF50), text = "Libre")
                            LegendItem(color = Color(0xFFFF6B6B), text = "Ocupado")
                            LegendItem(color = MediumBlue, text = "Mío")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (eventTables.isEmpty()) {
                            Text("Cargando mapa de mesas...", modifier = Modifier.padding(16.dp))
                        } else {
                            TableMapView(
                                tables = eventTables,
                                selectedSeats = selectedSeats,
                                onSeatClick = { seatId ->
                                    selectedSeats = if (selectedSeats.contains(seatId)) selectedSeats - seatId else selectedSeats + seatId
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
            is EventDetailState.Error -> { /* Reintentar UI */ }
        }
    }
}

@Composable
private fun TableMapView(
    tables: List<EventTable>,
    selectedSeats: Set<String>,
    onSeatClick: (String) -> Unit
) {
    // Calculamos límites para el scroll
    val maxX = (tables.maxOfOrNull { it.x } ?: 0) + 150
    val maxY = (tables.maxOfOrNull { it.y } ?: 0) + 150

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEEEEEE))
    ) {
        Box(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.size(width = maxX.dp, height = maxY.dp)) {
                tables.forEach { table ->
                    TableComponent(
                        table = table,
                        selectedSeats = selectedSeats,
                        onSeatClick = onSeatClick,
                        modifier = Modifier.offset(x = table.x.dp, y = table.y.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TableComponent(
    table: EventTable,
    selectedSeats: Set<String>,
    onSeatClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(110.dp), // Contenedor de mesa + asientos
        contentAlignment = Alignment.Center
    ) {
        // La Mesa (Centro)
        Canvas(modifier = Modifier.size(50.dp)) {
            drawCircle(color = Color(0xFFFF9800))
            drawCircle(color = Color(0xFFE65100), style = Stroke(width = 3f))
        }
        Text(table.name ?: "T", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)

        // Los Asientos (Distribución Circular)
        table.seats.forEachIndexed { index, seat ->
            // Algoritmo: Distribuir uniformemente en 360 grados
            val angle = (2 * Math.PI * index / table.capacity) - (Math.PI / 2)
            val radius = 42.dp // Distancia desde el centro de la mesa
            
            val offsetX = (radius.value * cos(angle)).dp
            val offsetY = (radius.value * sin(angle)).dp

            SeatCircle(
                seat = seat,
                isSelected = selectedSeats.contains(seat.id),
                onClick = { onSeatClick(seat.id) },
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .size(26.dp)
            )
        }
    }
}

@Composable
private fun SeatCircle(
    seat: TableSeat,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOccupied = seat.reservationId != null
    val color = when {
        isOccupied -> Color(0xFFFF6B6B)
        isSelected -> MediumBlue
        else -> Color(0xFF4CAF50)
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .clickable(enabled = !isOccupied) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!isOccupied) {
            Text("${seat.index}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        } else {
            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, null, tint = MediumBlue, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Gray)
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}