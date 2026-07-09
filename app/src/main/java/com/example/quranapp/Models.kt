package com.example.quranapp

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user_id: Int? = null,
    val role: String? = null
)

// مدل‌های جدید برای دریافت وضعیت پیشرفت از سرور
data class ProgressResponse(
    val error: Boolean,
    val message: String
)

data class ProgressItem(
    val verse_index: String,
    val status: String
)

data class GetProgressResponse(
    val error: Boolean,
    val message: String?,
    val progress: List<ProgressItem>?
)
