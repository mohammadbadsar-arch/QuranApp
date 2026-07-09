package com.example.quranapp

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded
    @POST("register.php")
    fun registerUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<AuthResponse>

    @FormUrlEncoded
    @POST("login.php")
    fun loginUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<AuthResponse>

    // دستورات جدید برای ارسال تیک‌ها و دریافت تایید معلم
    @FormUrlEncoded
    @POST("update_progress.php")
    fun updateProgress(
        @Field("student_id") studentId: String,
        @Field("verse_index") verseIndex: String
    ): Call<ProgressResponse>

    @FormUrlEncoded
    @POST("get_progress.php")
    fun getProgress(
        @Field("student_id") studentId: String
    ): Call<GetProgressResponse>
}
