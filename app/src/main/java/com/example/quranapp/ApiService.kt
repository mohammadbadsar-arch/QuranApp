import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("register.php")
    fun registerUser(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("role") role: String
    ): Call<AuthResponse>

    @FormUrlEncoded
    @POST("login.php")
    fun loginUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<AuthResponse>
}
