package es.afinavila.feature.comunidad.data

import es.afinavila.feature.comunidad.data.remote.ComunidadApi
import es.afinavila.feature.comunidad.data.remote.LoginRequest
import es.afinavila.feature.comunidad.domain.Archivo
import es.afinavila.feature.comunidad.domain.Comunidad
import es.afinavila.feature.comunidad.domain.ComunidadRepository
import android.util.Log
import es.afinavila.BuildConfig

class ComunidadDataRepository(private val comunidadApi: ComunidadApi) : ComunidadRepository {

    private var cachedNombre: String? = null

    override suspend fun login(codigoAcceso: String): Result<Comunidad> {
        return try {
            val response = comunidadApi.service.login(LoginRequest(codigoAcceso))
            if (response.isSuccessful) {
                val comunidad = response.body() ?: return Result.failure(Exception("Respuesta vacía del servidor"))
                comunidadApi.comunidadNombre = comunidad.nombre
                cachedNombre = comunidad.nombre
                Result.success(comunidad)
            } else {
                val errorCode = response.code()
                if (BuildConfig.DEBUG) Log.e("Repo", "Login failed: $errorCode")
                val errorMessage = when (errorCode) {
                    401, 403 -> "Código incorrecto"
                    500 -> "Error interno del servidor (500)"
                    else -> "Error del servidor ($errorCode)"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e("Repo", "Login error", e)
            Result.failure(Exception("Error de red: ${e.localizedMessage ?: e.message ?: "Desconocido"}"))
        }
    }

    override suspend fun getMe(): Result<Comunidad> {
        return try {
            val response = comunidadApi.service.getMe()
            if (response.isSuccessful) {
                Result.success(response.body() ?: return Result.failure(Exception("Respuesta vacía")))
            } else {
                if (BuildConfig.DEBUG) Log.e("Repo", "getMe failed: ${response.code()}")
                Result.failure(Exception("No autenticado"))
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e("Repo", "getMe error", e)
            Result.failure(e)
        }
    }

    override suspend fun getArchivosSession(): Result<List<Archivo>> {
        return try {
            val response = comunidadApi.service.getArchivosSession()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                if (BuildConfig.DEBUG) Log.e("Repo", "getArchivosSession failed: ${response.code()}")
                Result.failure(Exception("Error al cargar documentos"))
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e("Repo", "getArchivosSession error", e)
            Result.failure(e)
        }
    }

    override fun getCachedNombre(): String? = cachedNombre

    override suspend fun getPdfSession(archivoId: Int): Result<ByteArray> {
        return try {
            val response = comunidadApi.service.getPdfSession(archivoId)
            if (response.isSuccessful) {
                Result.success((response.body() ?: return Result.failure(Exception("Respuesta vacía"))).bytes())
            } else {
                Result.failure(Exception("Archivo no encontrado"))
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e("Repo", "getPdfSession error", e)
            Result.failure(e)
        }
    }
}
