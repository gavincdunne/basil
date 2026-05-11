package org.weekendware.basil

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.weekendware.basil.crash.initSentry
import org.weekendware.basil.di.initKoin

/**
 * Desktop entry point for the Basil application.
 *
 * Initialises Koin before the Compose window is created, then opens a
 * single window titled "Basil" that hosts the [App] composable.
 *
 * **Note:** The desktop [DatabaseDriverFactory] currently uses an in-memory
 * SQLite database, so data does not persist across sessions. Switch to a
 * file-backed [JdbcSqliteDriver] before shipping a desktop release.
 */
fun main() = application {
    initSentry()
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Basil"
    ) {
        App()
    }
}
