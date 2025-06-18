package org.weekendware.basil.presentation.dashboard

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject
import androidx.compose.runtime.collectAsState

object DashboardScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<DashboardViewModel>()
        val title = viewModel.title.collectAsState()

        Text(title.value)
    }
}