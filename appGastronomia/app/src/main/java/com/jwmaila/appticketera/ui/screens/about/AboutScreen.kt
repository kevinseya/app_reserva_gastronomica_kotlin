package com.jwmaila.appticketera.ui.screens.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jwmaila.appticketera.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acerca de", fontWeight = FontWeight.Bold) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo y nombre
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MediumBlue,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
            
            Text(
                text = "Ticketera App",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            
            Text(
                text = "Versión 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Descripción
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VeryLightBlue)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Sobre la aplicación",
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Ticketera es tu plataforma de confianza para la compra de tickets de eventos universitarios. Ofrecemos una experiencia segura, rápida y fácil para que no te pierdas ningún evento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray
                    )
                }
            }
            
            // Características
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Características principales",
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    FeatureItem(
                        icon = Icons.Default.Event,
                        title = "Eventos universitarios",
                        description = "Amplio catálogo de eventos"
                    )
                    FeatureItem(
                        icon = Icons.Default.Search,
                        title = "Búsqueda rápida",
                        description = "Encuentra eventos fácilmente"
                    )
                    FeatureItem(
                        icon = Icons.Default.EventSeat,
                        title = "Selección de asientos",
                        description = "Elige tus asientos favoritos"
                    )
                    FeatureItem(
                        icon = Icons.Default.Payment,
                        title = "Pago seguro",
                        description = "Procesamiento con Stripe"
                    )
                    FeatureItem(
                        icon = Icons.Default.QrCode,
                        title = "Tickets digitales",
                        description = "Código QR para acceso rápido"
                    )
                }
            }
            
            // Información legal
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "© 2026 Ticketera App",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray
                )
                Text(
                    "Todos los derechos reservados",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray
                )
            }
            
            // Tecnologías
            Text(
                "Desarrollado con ❤️ usando Kotlin & Jetpack Compose",
                style = MaterialTheme.typography.bodySmall,
                color = Gray
            )
        }
    }
}

@Composable
fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MediumBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                color = DarkBlue
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = Gray
            )
        }
    }
}
