package com.jwmaila.appticketera.utils

import java.text.NumberFormat
import java.util.Locale

fun formatDate(dateString: String): String {
    return try {
        dateString.split("T")[0]
    } catch (e: Exception) {
        dateString
    }
}

fun formatPrice(price: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
    return format.format(price)
}
