package com.jwmaila.appticketera.ui.screens.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jwmaila.appticketera.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayuda y Soporte", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MediumBlue,
                    titleContentColor = White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Preguntas Frecuentes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            
            FAQItem(
                question = "¿Cómo compro un ticket?",
                answer = "1. Ve a la pantalla de Eventos\n2. Selecciona el evento que te interesa\n3. Elige tus asientos\n4. Presiona 'Comprar' y completa el pago con Stripe\n5. Recibirás tu ticket con código QR"
            )
            
            FAQItem(
                question = "¿Dónde puedo ver mis tickets?",
                answer = "Tus tickets comprados se encuentran en la sección 'Mis Tickets' en el menú inferior de navegación."
            )
            
            FAQItem(
                question = "¿Puedo cancelar mi compra?",
                answer = "Una vez confirmada la compra, no es posible cancelarla. Por favor, asegúrate de seleccionar correctamente tus asientos antes de pagar."
            )
            
            FAQItem(
                question = "¿Cómo funciona el código QR?",
                answer = "Cada ticket tiene un código QR único que se puede escanear en la entrada del evento para verificar su autenticidad."
            )
            
            FAQItem(
                question = "¿Qué métodos de pago aceptan?",
                answer = "Aceptamos pagos con tarjeta de crédito/débito a través de Stripe, una plataforma segura de procesamiento de pagos."
            )
            
            FAQItem(
                question = "¿Puedo buscar eventos?",
                answer = "Sí, en la pantalla de Eventos hay un buscador donde puedes filtrar por nombre, lugar o descripción del evento."
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VeryLightBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = MediumBlue
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "¿Necesitas más ayuda?",
                                fontWeight = FontWeight.SemiBold,
                                color = DarkBlue
                            )
                            Text(
                                "Contáctanos: soporte@ticketera.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FAQItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkBlue,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MediumBlue
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray
                )
            }
        }
    }
}
