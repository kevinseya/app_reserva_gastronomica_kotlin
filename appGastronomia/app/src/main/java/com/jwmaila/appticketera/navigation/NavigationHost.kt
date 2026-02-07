package com.jwmaila.appticketera.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jwmaila.appticketera.data.local.UserPreferences
import com.jwmaila.appticketera.ui.screens.admin.AdminScreen
import com.jwmaila.appticketera.ui.screens.admin.ManageEventsScreen
import com.jwmaila.appticketera.ui.screens.admin.ManageUsersScreen
import com.jwmaila.appticketera.ui.screens.admin.CreateEventScreen
import com.jwmaila.appticketera.ui.screens.auth.LoginScreen
import com.jwmaila.appticketera.ui.screens.auth.RegisterScreen
import com.jwmaila.appticketera.ui.screens.events.EventDetailScreen
import com.jwmaila.appticketera.ui.screens.events.EventsListScreen
import com.jwmaila.appticketera.ui.screens.profile.ProfileScreen
import com.jwmaila.appticketera.ui.screens.help.HelpScreen
import com.jwmaila.appticketera.ui.screens.about.AboutScreen
import com.jwmaila.appticketera.ui.screens.qr.ScanQRScreen
import com.jwmaila.appticketera.ui.screens.tickets.MyTicketsScreen
import com.jwmaila.appticketera.ui.screens.tickets.TicketDetailScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationHost(
    userPreferences: UserPreferences
) {
    val navController = rememberNavController()
    
    // Hacer que isLoggedIn sea reactivo
    var isLoggedIn by remember { mutableStateOf(userPreferences.isLoggedIn()) }
    val startDestination = if (isLoggedIn) Screen.EventsList.route else Screen.Login.route
    
    // Usar remember para que se actualice cuando cambia el rol
    var userRole by remember { mutableStateOf(userPreferences.getUserRole()) }
    var isAdmin by remember { mutableStateOf(userRole == "ADMIN") }
    
    // Actualizar el estado cuando navega
    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, _, _ ->
            val currentLoggedIn = userPreferences.isLoggedIn()
            if (currentLoggedIn != isLoggedIn) {
                isLoggedIn = currentLoggedIn
            }
            
            val currentRole = userPreferences.getUserRole()
            if (currentRole != userRole) {
                userRole = currentRole
                isAdmin = currentRole == "ADMIN"
            }
        }
    }
    
    Scaffold(
        bottomBar = {
            if (isLoggedIn) {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    
                    val items = buildList {
                        add(
                            BottomNavItem(
                                icon = Icons.Default.Event,
                                label = "Eventos",
                                screen = Screen.EventsList
                            )
                        )
                        add(
                            BottomNavItem(
                                icon = Icons.Default.ConfirmationNumber,
                                label = "Mis Tickets",
                                screen = Screen.MyTickets
                            )
                        )
                        if (isAdmin) {
                            add(
                                BottomNavItem(
                                    icon = Icons.Default.AdminPanelSettings,
                                    label = "Admin",
                                    screen = Screen.Admin
                                )
                            )
                            add(
                                BottomNavItem(
                                    icon = Icons.Default.QrCodeScanner,
                                    label = "Escanear",
                                    screen = Screen.ScanQR
                                )
                            )
                        }
                        add(
                            BottomNavItem(
                                icon = Icons.Default.Person,
                                label = "Perfil",
                                screen = Screen.Profile
                            )
                        )
                    }
                    
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(Screen.EventsList.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(Screen.EventsList.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate(Screen.EventsList.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            
            // Events
            composable(Screen.EventsList.route) {
                EventsListScreen(
                    onNavigateToEventDetail = { eventId ->
                        navController.navigate(Screen.EventDetail.createRoute(eventId))
                    },
                    onNavigateToMyTickets = {
                        navController.navigate(Screen.MyTickets.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToManageEvents = {
                        navController.navigate(Screen.ManageEvents.route)
                    },
                    onNavigateToManageUsers = {
                        navController.navigate(Screen.ManageUsers.route)
                    },
                    onNavigateToAdmin = {
                        navController.navigate(Screen.Admin.route)
                    }
                )
            }
            
            composable(
                route = Screen.EventDetail.route,
                arguments = listOf(
                    navArgument("eventId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                EventDetailScreen(
                    eventId = eventId,
                    onNavigateBack = { navController.popBackStack() },
                    onPurchaseSuccess = {
                        navController.navigate(Screen.MyTickets.route)
                    }
                )
            }
            
            // Tickets
            composable(Screen.MyTickets.route) {
                MyTicketsScreen(
                    onNavigateToTicketDetail = { ticketId ->
                        navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                    }
                )
            }
            
            composable(
                route = Screen.TicketDetail.route,
                arguments = listOf(
                    navArgument("ticketId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: return@composable
                TicketDetailScreen(
                    ticketId = ticketId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Profile
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToHelp = {
                        navController.navigate(Screen.Help.route)
                    },
                    onNavigateToAbout = {
                        navController.navigate(Screen.About.route)
                    }
                )
            }
            
            // Help
            composable(Screen.Help.route) {
                HelpScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // About
            composable(Screen.About.route) {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Admin
            if (isAdmin) {
                composable(Screen.Admin.route) {
                    AdminScreen(
                        onNavigateToManageEvents = {
                            navController.navigate(Screen.ManageEvents.route)
                        },
                        onNavigateToManageUsers = {
                            navController.navigate(Screen.ManageUsers.route)
                        }
                    )
                }
                
                composable(Screen.ManageEvents.route) {
                    ManageEventsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToCreateEvent = {
                            navController.navigate(Screen.CreateEvent.route)
                        },
                        onNavigateToEditEvent = { eventId ->
                            navController.navigate(Screen.EditEvent.createRoute(eventId))
                        }
                    )
                }
                
                composable(Screen.ManageUsers.route) {
                    ManageUsersScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(Screen.CreateEvent.route) {
                    CreateEventScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(
                    route = Screen.EditEvent.route,
                    arguments = listOf(
                        navArgument("eventId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId")
                    CreateEventScreen(
                        eventId = eventId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(Screen.ScanQR.route) {
                    ScanQRScreen()
                }
            }
        }
    }
}

data class BottomNavItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val screen: Screen
)
