package es.afinavila.feature.comunidad.domain

import org.koin.core.annotation.Single

@Single
class GetComunidadNameByIdUseCase(private val comunidadRepository: ComunidadRepository) {
    suspend fun invoke(comunidadId: Int): String? {
        return comunidadRepository.getComunidadNameById(comunidadId)
    }
}