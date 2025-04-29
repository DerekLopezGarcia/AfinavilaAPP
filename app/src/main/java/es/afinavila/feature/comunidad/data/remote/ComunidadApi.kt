package es.afinavila.feature.comunidad.data.remote

import es.afinavila.feature.comunidad.domain.Archivo
import es.afinavila.feature.comunidad.domain.Comunidad
import org.koin.core.annotation.Single
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import com.google.gson.GsonBuilder
import okhttp3.ResponseBody

@Single
class ComunidadApi {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.12:8081") // Corregir la URL base
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().setLenient().create()
            )
        )
        .build()

    val service: ApiService = retrofit.create(ApiService::class.java)
}

interface ApiService {
    @GET("/comunidades")
    suspend fun getComunidades(): Response<List<Comunidad>>

    @GET("/archivos/{comunidadId}")
    suspend fun getArchivosByComunidadId(@Path("comunidadId") comunidadId: Int): Response<List<Archivo>> // Corregido para usar @Path

    @GET("/archivo/{archivoId}")
    suspend fun getArchivoById(@Path("archivoId") archivoId: Int): Response<UrlResponse>
    @GET("/archivo/pdf/{id}")
    suspend fun getPdfArchivoById(@Path("id") archivoId: Int): Response<ResponseBody>
}

data class UrlResponse(
    val url: String
)

