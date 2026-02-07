package com.jwmaila.appticketera.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole
) {
    val fullName: String
        get() = "$firstName $lastName"
}

@Serializable
enum class UserRole {
    ADMIN,
    CLIENT
}

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

@Serializable
data class CreateUserRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole
)

@Serializable
data class UpdateUserRequest(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val role: UserRole? = null
)

@Serializable
data class UpdateProfileRequest(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val user: User
)
