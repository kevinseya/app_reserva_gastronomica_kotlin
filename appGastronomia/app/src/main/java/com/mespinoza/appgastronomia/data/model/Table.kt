package com.mespinoza.appgastronomia.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Table(
    val id: String,
    val number: Int,
    val seats: Int,
    val status: String,
    val location: String?
)

@Serializable
data class CreateTableRequest(
    val number: Int,
    val seats: Int = 4,
    val location: String? = null
)

@Serializable
data class UpdateTableRequest(
    val number: Int? = null,
    val seats: Int? = null,
    val status: String? = null,
    val location: String? = null
)
