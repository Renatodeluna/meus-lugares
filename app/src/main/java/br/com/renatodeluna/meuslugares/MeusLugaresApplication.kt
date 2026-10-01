package br.com.renatodeluna.meuslugares

import android.app.Application
import br.com.renatodeluna.meuslugares.di.AppContainer

class MeusLugaresApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
