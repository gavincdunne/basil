package org.weekendware.basil

import androidx.compose.ui.window.ComposeUIViewController
import org.weekendware.basil.di.initKoin

fun MainViewController() = run {
    initKoin()
    ComposeUIViewController { App() }
}