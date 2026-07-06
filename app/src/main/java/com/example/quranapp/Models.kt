data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user_id: Int?,
    val role: String?
)
