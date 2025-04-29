package es.afinavila.core

import es.afinavila.feature.comunidad.data.ComunidadDataRepository
import es.afinavila.feature.comunidad.data.remote.ComunidadApi
import es.afinavila.feature.comunidad.domain.ComunidadRepository
import es.afinavila.feature.comunidad.domain.GetComunidadNameByIdUseCase
import es.afinavila.feature.comunidad.domain.GetIdComunidadByCodAccesUseCase
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.dsl.module

@Module
@ComponentScan("es.afinavila")
class Appmodule {
    @Single
    fun provideString(): String {
        return "Hello Koin"
    }

    val appModule = module {
        single { ComunidadApi() }
        single<ComunidadRepository> { ComunidadDataRepository(get()) }
        single { GetIdComunidadByCodAccesUseCase(get()) }
        single { GetComunidadNameByIdUseCase(get()) }
    }
}
