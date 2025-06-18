package org.weekendware.basil.presentation.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject

object SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<SettingsViewModel>()
        val title = viewModel.title.collectAsState()

        Text(title.value)
    }
}