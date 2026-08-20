package es.afinavila.feature.comunidad.data.remote

import es.afinavila.BuildConfig
import es.afinavila.feature.comunidad.domain.Archivo
import es.afinavila.feature.comunidad.domain.Comunidad
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit

class ComunidadApi {

    var comunidadNombre: String? = null

    private val cookieStore = mutableListOf<Cookie>()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .cookieJar(object : CookieJar {
            override fun loadForRequest(url: HttpUrl): List<Cookie> = synchronized(cookieStore) {
                cookieStore.filter { it.matches(url) }
            }
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                synchronized(cookieStore) {
                    cookies.forEach { newCookie ->
                        cookieStore.removeAll { it.name == newCookie.name && it.domain == newCookie.domain && it.path == newCookie.path }
                        if (!newCookie.expiresAt.let { it < System.currentTimeMillis() }) {
                            cookieStore.add(newCookie)
                        }
                    }
                }
            }
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL)
        .client(okHttpClient)
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().setLenient().create()
            )
        )
        .build()

    val service: ApiService = retrofit.create(ApiService::class.java)
}

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<Comunidad>

    @GET("auth/me")
    suspend fun getMe(): Response<Comunidad>

    @GET("archivos/session")
    suspend fun getArchivosSession(): Response<List<Archivo>>

    @GET("archivo/pdf/session/{id}")
    suspend fun getPdfSession(@Path("id") archivoId: Int): Response<ResponseBody>
}

data class LoginRequest(val codigoAcceso: String)
