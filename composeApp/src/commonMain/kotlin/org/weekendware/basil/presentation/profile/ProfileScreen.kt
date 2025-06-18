package org.weekendware.basil.presentation.profile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject

object ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<ProfileViewModel>()
        val title = viewModel.title.collectAsState()

        Text(title.value)
    }
}