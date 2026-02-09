package com.mespinoza.appgastronomia.ui.screens.tickets

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.mespinoza.appgastronomia.data.model.TicketStatus
import com.mespinoza.appgastronomia.utils.formatDate
import com.mespinoza.appgastronomia.utils.formatPrice
import com.mespinoza.appgastronomia.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticketId: String,
    onNavigateBack: () -> Unit,
    viewModel: TicketDetailViewModel = hiltViewModel()
) {
    val ticketState by viewModel.ticketState.collectAsState()

    LaunchedEffect(ticketId) {
        viewModel.loadTicket(ticketId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ticket") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MediumBlue,
                    titleContentColor = White,
                    navigationIconContentColor = White
                )
            )
        }
    ) { paddingValues ->
        when (val state = ticketState) {
            is TicketDetailState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MediumBlue)
                }
            }
            is TicketDetailState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = ErrorRed)
                }
            }
            is TicketDetailState.Success -> {
                val tickets = state.tickets
                val mainTicket = tickets.first() // Usamos el primero para datos generales (evento, fecha, QR)
                val qrBitmap = remember(mainTicket.qrCode) { generateQrBitmap(mainTicket.qrCode, 600) }
                
                // Calcular totales de TODO el grupo de tickets
                val totalPrice = remember(tickets) {
                    tickets.sumOf { ticket ->
                        val eventPrice = ticket.event?.ticketPrice ?: 0.0
                        val seatPrice = ticket.tableSeat?.price ?: 0.0
                        val foodPrice = ticket.foodItems.sumOf { it.quantity * it.foodItem.price }
                        eventPrice + seatPrice + foodPrice
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // QR Code
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(White),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "QR",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Presenta este código en la entrada",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Información del ticket
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Detalles del Ticket",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = DarkBlue
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))

                            DetailRow(
                                "Estado",
                                mainTicket.status.name,
                                if (mainTicket.status == TicketStatus.PAID) SuccessGreen else DarkBlue
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailRow("Evento", mainTicket.event?.name ?: "Evento")
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailRow("Fecha", mainTicket.event?.date?.let { formatDate(it) } ?: "-")
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailRow("Ubicación", mainTicket.event?.venue ?: "-")
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Divider()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Asientos Comprados (${tickets.size})", fontWeight = FontWeight.Bold, color = DarkBlue)
                            Spacer(modifier = Modifier.height(8.dp))

                            tickets.forEach { ticket ->
                                val seatLabel = if (ticket.tableSeat != null) {
                                    "${ticket.tableSeat.table?.name ?: "Mesa"} - Asiento ${ticket.tableSeat.index + 1}"
                                } else {
                                    "Fila ${ticket.seat?.row}, N° ${ticket.seat?.column}"
                                }
                                val eventVal = ticket.event?.ticketPrice ?: 0.0
                                val seatVal = ticket.tableSeat?.price ?: 0.0

                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text(seatLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("  Entrada Evento:", style = MaterialTheme.typography.bodySmall, color = Gray)
                                        Text(formatPrice(eventVal), style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (seatVal > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("  Valor Asiento:", style = MaterialTheme.typography.bodySmall, color = Gray)
                                            Text(formatPrice(seatVal), style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                                Divider(color = LightGray.copy(alpha = 0.5f))
                            }
                            
                            // Sección de Comida
                            val allFoodItems = tickets.flatMap { it.foodItems }
                            if (allFoodItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Comida Ordenada", fontWeight = FontWeight.Bold, color = DarkBlue)
                                Spacer(modifier = Modifier.height(8.dp))
                                allFoodItems.forEach { tf ->
                                    val itemTotal = tf.quantity * tf.foodItem.price
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${tf.quantity}x ${tf.foodItem.name}", style = MaterialTheme.typography.bodyMedium)
                                            Text("${formatPrice(tf.foodItem.price)} c/u", style = MaterialTheme.typography.bodySmall, color = Gray)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(formatPrice(itemTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            val statusColor = if(tf.status == "SERVED") SuccessGreen else WarningOrange
                                            Text(tf.status, color = statusColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            DetailRow(
                                "Total Pagado",
                                formatPrice(totalPrice),
                                DarkBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun generateQrBitmap(content: String, size: Int): Bitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        size,
        size
    )
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
        }
    }
    return bitmap
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}
