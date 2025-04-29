package es.afinavila.feature.comunidad.domain

import org.koin.core.annotation.Single

@Single
class GetArchivosByComunidadIdUseCase(private val comunidadRepository: ComunidadRepository) {
    suspend operator fun invoke(comunidadId: Int): List<Archivo>? {
        return comunidadRepository.getArchivosByComunidadId(comunidadId)
    }
}