package es.afinavila.core

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class AfinavilaApp:Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AfinavilaApp)
            modules(
                Appmodule().module
            )
        }
    }
}