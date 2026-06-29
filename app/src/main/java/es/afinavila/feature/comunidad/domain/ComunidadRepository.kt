package es.afinavila.feature.comunidad.domain

interface ComunidadRepository {
    suspend fun login(codigoAcceso: String): Result<Comunidad>
    suspend fun getMe(): Result<Comunidad>
    suspend fun getArchivosSession(): Result<List<Archivo>>
    suspend fun getPdfSession(archivoId: Int): Result<ByteArray>
    fun getCachedNombre(): String?
}
