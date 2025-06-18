package org.weekendware.basil

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.weekendware.basil.di.initKoin

fun main() = application {
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Basil",
    ) {
        App()
    }
}