package es.afinavila.feature.comunidad.domain

import org.koin.core.annotation.Single

@Single
class GetIdComunidadByCodAccesUseCase(private val comunidadRepository: ComunidadRepository) {
    suspend operator fun invoke(codAcceso: String): Int? {
        return comunidadRepository.getComunidadIdByCodAcceso(codAcceso)
    }
}
