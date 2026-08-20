package es.afinavila.feature.comunidad.data.remote

import es.afinavila.BuildConfig
import es.afinavila.feature.comunidad.domain.Archivo
import es.afinavila.feature.comunidad.domain.Comunidad
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.CertificatePinner
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class ComunidadApi(context: Context) {

    var comunidadNombre: String? = null

    private val cookieStore = mutableListOf<Cookie>()
    private val cookiePrefs = EncryptedSharedPreferences.create(
        "afinavila_cookies",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    private val cookiePreferenceKey = "cookies"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .certificatePinner(
            CertificatePinner.Builder()
                .add("www.afinavila.es", "sha256/mi16DWWn3FuJYkmx5MYTUYV7ZTzIqb2qcVQrWZzrGbQ=")
                .add("afinavila.es", "sha256/mi16DWWn3FuJYkmx5MYTUYV7ZTzIqb2qcVQrWZzrGbQ=")
                .build()
        )
        .cookieJar(object : CookieJar {
            override fun loadForRequest(url: HttpUrl): List<Cookie> = synchronized(cookieStore) {
                val persisted = cookiePrefs.getStringSet(cookiePreferenceKey, emptySet()).orEmpty()
                    .mapNotNull { Cookie.parse(url, it) }
                cookieStore.clear()
                cookieStore.addAll(persisted)
                cookieStore.filter { it.matches(url) && (it.secure || url.isHttps) }
            }
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                synchronized(cookieStore) {
                    cookies.forEach { newCookie ->
                        if (!url.isHttps || !newCookie.secure) return@forEach
                        cookieStore.removeAll { it.name == newCookie.name && it.domain == newCookie.domain && it.path == newCookie.path }
                        if (!newCookie.expiresAt.let { it < System.currentTimeMillis() }) {
                            cookieStore.add(newCookie)
                        }
                    }
                    cookiePrefs.edit()
                        .putStringSet(cookiePreferenceKey, cookieStore.map { it.toString() }.toSet())
                        .apply()
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
