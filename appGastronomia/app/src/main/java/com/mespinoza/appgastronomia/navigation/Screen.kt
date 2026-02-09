package com.mespinoza.appgastronomia.navigation

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
    data object ManageFood : Screen("admin/food")
    data object CreateEvent : Screen("admin/events/create")
    data object EditEvent : Screen("admin/events/edit/{eventId}") {
        fun createRoute(eventId: String) = "admin/events/edit/$eventId"
    }
    data object EventTablesEditor : Screen("admin/events/{eventId}/tables/editor") {
        fun createRoute(eventId: String) = "admin/events/$eventId/tables/editor"
    }
    data object ScanQR : Screen("scan-qr")
    data object Tables : Screen("tables")
    data object Reservations : Screen("reservations")
    data object TableMap : Screen("events/{eventId}/tables/map") {
        fun createRoute(eventId: String) = "events/$eventId/tables/map"
    }
}
