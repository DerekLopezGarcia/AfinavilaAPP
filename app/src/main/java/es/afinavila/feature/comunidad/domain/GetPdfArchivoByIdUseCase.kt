package es.afinavila.feature.comunidad.domain

import org.koin.core.annotation.Single

@Single
class GetPdfArchivoByIdUseCase(private val comunidadRepository: ComunidadRepository) {
    suspend operator fun invoke(archivoId: Int): ByteArray? {
        return comunidadRepository.getPdfArchivoById(archivoId) // Retornar directamente los bytes
    }
}
