package ru.limonella.alias

import android.app.Application
import ru.limonella.alias.di.AppContainer

class AliasApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
