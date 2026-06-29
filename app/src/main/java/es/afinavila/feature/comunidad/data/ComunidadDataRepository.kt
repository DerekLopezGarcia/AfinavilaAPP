package es.afinavila.feature.comunidad.data

import es.afinavila.feature.comunidad.data.remote.ComunidadApi
import es.afinavila.feature.comunidad.data.remote.LoginRequest
import es.afinavila.feature.comunidad.domain.Archivo
import es.afinavila.feature.comunidad.domain.Comunidad
import es.afinavila.feature.comunidad.domain.ComunidadRepository
import android.util.Log

class ComunidadDataRepository(private val comunidadApi: ComunidadApi) : ComunidadRepository {

    private var cachedNombre: String? = null

    override suspend fun login(codigoAcceso: String): Result<Comunidad> {
        return try {
            val response = comunidadApi.service.login(LoginRequest(codigoAcceso))
            if (response.isSuccessful) {
                val comunidad = response.body() ?: return Result.failure(Exception("Respuesta vacía"))
                comunidadApi.comunidadNombre = comunidad.nombre
                cachedNombre = comunidad.nombre
                Result.success(comunidad)
            } else {
                Log.e("Repo", "Login failed: ${response.code()}")
                Result.failure(Exception("Código incorrecto"))
            }
        } catch (e: Exception) {
            Log.e("Repo", "Login error", e)
            Result.failure(e)
        }
    }

    override suspend fun getMe(): Result<Comunidad> {
        return try {
            val response = comunidadApi.service.getMe()
            if (response.isSuccessful) {
                Result.success(response.body() ?: return Result.failure(Exception("Respuesta vacía")))
            } else {
                Log.e("Repo", "getMe failed: ${response.code()}")
                Result.failure(Exception("No autenticado"))
            }
        } catch (e: Exception) {
            Log.e("Repo", "getMe error", e)
            Result.failure(e)
        }
    }

    override suspend fun getArchivosSession(): Result<List<Archivo>> {
        return try {
            val response = comunidadApi.service.getArchivosSession()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Log.e("Repo", "getArchivosSession failed: ${response.code()}")
                Result.failure(Exception("Error al cargar documentos"))
            }
        } catch (e: Exception) {
            Log.e("Repo", "getArchivosSession error", e)
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
            Log.e("Repo", "getPdfSession error", e)
            Result.failure(e)
        }
    }
}
