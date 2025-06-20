package org.weekendware.basil.presentation.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject

object SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<SettingsViewModel>()
        val title = viewModel.title.collectAsState()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFB2BDAF) // 🌿 Sage Green
        ) {
            Text(
                text = title.value,
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}