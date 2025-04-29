package es.afinavila.feature.comunidad.domain

interface ComunidadRepository {
    suspend fun getComunidadIdByCodAcceso(codAcceso: String): Int?
    suspend fun getArchivosByComunidadId(comunidadId: Int): List<Archivo>
    suspend fun getComunidadNameById(comunidadId: Int): String?
    suspend fun getArchivoById(archivoId: Int): String?
    suspend fun getPdfArchivoById(archivoId: Int): ByteArray?
}