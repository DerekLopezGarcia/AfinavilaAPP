package es.afinavila.core

import es.afinavila.feature.comunidad.data.ComunidadDataRepository
import es.afinavila.feature.comunidad.data.remote.ComunidadApi
import es.afinavila.feature.comunidad.domain.ComunidadRepository
import es.afinavila.ui.viewmodel.LoginViewModel
import es.afinavila.ui.viewmodel.ClienteViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.dsl.module

@Module
@ComponentScan("es.afinavila")
class Appmodule {
    val appModule = module {
        single { ComunidadApi() }
        single<ComunidadRepository> { ComunidadDataRepository(get()) }
        single { LoginViewModel(get()) }
        single { ClienteViewModel(get(), androidContext() as android.app.Application) }
    }
}
