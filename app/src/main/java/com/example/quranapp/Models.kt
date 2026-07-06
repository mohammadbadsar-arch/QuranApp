package com.example.quranapp

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user_id: Int? = null,
    val role: String? = null
)
