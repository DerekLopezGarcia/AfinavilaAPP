package es.afinavila.feature.comunidad.data

import es.afinavila.feature.comunidad.data.remote.ComunidadApi
import es.afinavila.feature.comunidad.domain.Archivo
import es.afinavila.feature.comunidad.domain.ComunidadRepository
import org.koin.core.annotation.Single
import android.util.Log
import okhttp3.ResponseBody

@Single
class ComunidadDataRepository(private val comunidadApi: ComunidadApi) : ComunidadRepository {
    override suspend fun getComunidadIdByCodAcceso(codAcceso: String): Int? {
        val response = comunidadApi.service.getComunidades()
        return if (response.isSuccessful) {
            val comunidades = response.body()
            comunidades?.find { it.codigoAcceso == codAcceso }?.id
        } else {
            Log.e("ComunidadDataRepository", "Error al obtener comunidades: ${response.errorBody()?.string()}")
            null
        }
    }

    override suspend fun getArchivosByComunidadId(comunidadId: Int): List<Archivo> {
        val response = comunidadApi.service.getArchivosByComunidadId(comunidadId)
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            Log.e("ComunidadDataRepository", "Error al obtener archivos: ${response.errorBody()?.string()}")
            emptyList()
        }
    }

    override suspend fun getComunidadNameById(comunidadId: Int): String? {
        val response = comunidadApi.service.getComunidades()
        return if (response.isSuccessful) {
            val comunidades = response.body()
            comunidades?.find { it.id == comunidadId }?.nombre
        } else {
            Log.e("ComunidadDataRepository", "Error al obtener nombre de comunidad: ${response.errorBody()?.string()}")
            null
        }
    }

    override suspend fun getArchivoById(archivoId: Int): String? {
        val response = comunidadApi.service.getArchivoById(archivoId)
        return if (response.isSuccessful) {
            response.body()?.url
        } else {
            Log.e("ComunidadDataRepository", "Error al obtener archivo: ${response.errorBody()?.string()}")
            null
        }
    }

    override suspend fun getPdfArchivoById(archivoId: Int): ByteArray? {
        val response = comunidadApi.service.getPdfArchivoById(archivoId)
        return if (response.isSuccessful) {
            response.body()?.bytes() // Leer el contenido como bytes
        } else {
            Log.e("ComunidadDataRepository", "Error al obtener PDF: ${response.errorBody()?.string()}")
            null
        }
    }
}
