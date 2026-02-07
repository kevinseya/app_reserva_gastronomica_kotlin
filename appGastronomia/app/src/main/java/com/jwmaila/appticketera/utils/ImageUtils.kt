package com.jwmaila.appticketera.utils

import android.os.Build
import android.util.Log

object ImageUtils {
    private const val TAG = "ImageUtils"
    
    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for x86") ||
            Build.MANUFACTURER.contains("Genymotion")
    }

    private fun getBaseUrl(): String {
        return if (isEmulator()) {
            "http://10.0.2.2:3000"
        } else {
            "http://172.20.10.9:3000"
        }
    }

    /**
     * Convierte una ruta relativa de imagen en una URL completa
     */
    fun getFullImageUrl(imageUrl: String?): String? {
        if (imageUrl.isNullOrBlank()) {
            Log.d(TAG, "getFullImageUrl: imageUrl es null o vacío")
            return null
        }
        
        // Si ya es una URL completa, devolverla tal cual
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            Log.d(TAG, "getFullImageUrl: URL completa detectada: $imageUrl")
            return imageUrl
        }
        
        // Si es una ruta relativa, construir la URL completa
        val path = if (imageUrl.startsWith("/")) imageUrl else "/$imageUrl"
        val fullUrl = "${getBaseUrl()}$path"
        Log.d(TAG, "getFullImageUrl: Convirtiendo '$imageUrl' a '$fullUrl'")
        return fullUrl
    }
}
