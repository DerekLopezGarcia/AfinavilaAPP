package es.afinavila.feature.comunidad.domain

import android.util.Log
import org.koin.core.annotation.Single

@Single
class GetUrlArchivoByIdUseCase(private val comunidadRepository: ComunidadRepository) {
    suspend operator fun invoke(archivoId: Int): String? {
        val url = comunidadRepository.getArchivoById(archivoId)
        Log.d("GetArchivoByIdUseCase", "Archivo ID: $archivoId, URL obtenida: $url") // Registro de depuración
        return url // Devolver directamente la URL
    }
}
