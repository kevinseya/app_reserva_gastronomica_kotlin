package com.jwmaila.appticketera.data.remote

import com.jwmaila.appticketera.data.local.UserPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val userPreferences: UserPreferences
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // No agregar token a endpoints públicos
        val isPublicEndpoint = originalRequest.url.encodedPath.let { path ->
            path.contains("/auth/login") || 
            path.contains("/auth/register") ||
            (path.contains("/events") && originalRequest.method == "GET")
        }
        
        if (isPublicEndpoint) {
            return chain.proceed(originalRequest)
        }
        
        // Agregar token a endpoints protegidos
        val token = userPreferences.getAuthToken()
        
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(newRequest)
    }
}
