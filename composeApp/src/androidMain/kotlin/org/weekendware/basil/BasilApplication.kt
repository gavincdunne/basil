package org.weekendware.basil

import android.app.Application
import android.content.Context
import net.zetetic.android.database.sqlcipher.SQLiteDatabase
import org.koin.dsl.module
import org.weekendware.basil.crash.initSentry
import org.weekendware.basil.di.initKoin

/**
 * Custom [Application] that initialises process-scoped singletons exactly once.
 *
 * Moving Koin and Sentry init here (rather than [MainActivity.onCreate]) prevents
 * [org.koin.core.error.KoinApplicationAlreadyStartedException] on activity
 * recreation (rotation, multi-window, etc.), because [Application.onCreate] is
 * only called once per process lifetime.
 */
class BasilApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        SQLiteDatabase.loadLibs(this)
        initSentry()

        initKoin {
            // Register applicationContext so DatabaseDriverFactory can receive
            // it via get<Context>() in the platform module.
            modules(module { single<Context> { applicationContext } })
        }
    }
}
