package com.jwmaila.appticketera.ui.components

import androidx.compose.foundation.layout.*
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
fun AppTopBar(
    title: String,
    userName: String,
    userRole: String,
    onNavigateToEvents: () -> Unit,
    onNavigateToMyTickets: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToManageEvents: () -> Unit,
    onNavigateToManageUsers: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    TopAppBar(
        title = { 
            Text(
                title,
                fontWeight = FontWeight.Bold
            ) 
        },
        actions = {
            // Mostrar rol
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (userRole == "ADMIN") PastelBlue else VeryLightBlue,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = if (userRole == "ADMIN") "Admin" else "Cliente",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = DarkBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // Menú de opciones
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Menú",
                    tint = White
                )
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                // Opciones comunes
                DropdownMenuItem(
                    text = { Text("🎫 Eventos") },
                    onClick = {
                        showMenu = false
                        onNavigateToEvents()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Event, contentDescription = null, tint = MediumBlue)
                    }
                )
                
                if (userRole == "CLIENT") {
                    // Opciones solo para clientes
                    DropdownMenuItem(
                        text = { Text("🎟️ Mis Tickets") },
                        onClick = {
                            showMenu = false
                            onNavigateToMyTickets()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = MediumBlue)
                        }
                    )
                }
                
                if (userRole == "ADMIN") {
                    // Opciones solo para admin
                    Divider()
                    
                    DropdownMenuItem(
                        text = { Text("⚙️ Gestionar Eventos") },
                        onClick = {
                            showMenu = false
                            onNavigateToManageEvents()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = MediumBlue)
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("👥 Gestionar Usuarios") },
                        onClick = {
                            showMenu = false
                            onNavigateToManageUsers()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.People, contentDescription = null, tint = MediumBlue)
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("📊 Panel Admin") },
                        onClick = {
                            showMenu = false
                            onNavigateToAdmin()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Dashboard, contentDescription = null, tint = MediumBlue)
                        }
                    )
                }
                
                Divider()
                
                DropdownMenuItem(
                    text = { Text("👤 Perfil") },
                    onClick = {
                        showMenu = false
                        onNavigateToProfile()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MediumBlue)
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MediumBlue,
            titleContentColor = White
        )
    )
}
