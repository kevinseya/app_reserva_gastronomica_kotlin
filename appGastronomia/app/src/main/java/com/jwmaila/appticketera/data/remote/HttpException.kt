package com.jwmaila.appticketera.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Serializable
data class ErrorResponse(
    val message: String? = null,
    val statusCode: Int? = null,
    val error: String? = null
)

fun HttpException.getErrorMessage(): String {
    return try {
        val errorBody = response()?.errorBody()?.string()
        if (errorBody != null) {
            val json = Json { ignoreUnknownKeys = true }
            val errorResponse = json.decodeFromString<ErrorResponse>(errorBody)
            errorResponse.message ?: errorResponse.error ?: "Error desconocido"
        } else {
            when (code()) {
                400 -> "Solicitud inválida"
                401 -> "Credenciales inválidas"
                403 -> "Acceso denegado"
                404 -> "No encontrado"
                409 -> "El email ya está registrado"
                500 -> "Error del servidor"
                else -> "Error: ${code()}"
            }
        }
    } catch (e: Exception) {
        when (code()) {
            400 -> "Solicitud inválida"
            401 -> "Credenciales inválidas"
            403 -> "Acceso denegado"
            404 -> "No encontrado"
            409 -> "El email ya está registrado"
            500 -> "Error del servidor"
            else -> "Error de conexión"
        }
    }
}

fun Throwable.getUserMessage(): String {
    return when (this) {
        is HttpException -> this.getErrorMessage()
        is java.net.UnknownHostException -> "No se pudo conectar al servidor"
        is java.net.SocketTimeoutException -> "Tiempo de espera agotado"
        is java.io.IOException -> "Error de conexión"
        else -> message ?: "Error desconocido"
    }
}
