package com.jwmaila.appticketera.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object EventsList : Screen("events")
    data object EventDetail : Screen("events/{eventId}") {
        fun createRoute(eventId: String) = "events/$eventId"
    }
    data object MyTickets : Screen("my-tickets")
    data object TicketDetail : Screen("tickets/{ticketId}") {
        fun createRoute(ticketId: String) = "tickets/$ticketId"
    }
    data object Profile : Screen("profile")
    data object Help : Screen("help")
    data object About : Screen("about")
    data object Admin : Screen("admin")
    data object ManageEvents : Screen("admin/events")
    data object ManageUsers : Screen("admin/users")
    data object CreateEvent : Screen("admin/events/create")
    data object EditEvent : Screen("admin/events/edit/{eventId}") {
        fun createRoute(eventId: String) = "admin/events/edit/$eventId"
    }
    data object ScanQR : Screen("scan-qr")
}
