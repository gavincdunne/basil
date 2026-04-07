package org.weekendware.basil.presentation.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject
import org.weekendware.basil.presentation.logging.LogEntrySheet
import org.weekendware.basil.presentation.logging.LoggingViewModel

object DashboardScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<DashboardViewModel>()
        val loggingViewModel = koinInject<LoggingViewModel>()
        val showSheet by viewModel.showLogSheet.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = {
                    loggingViewModel.reset()
                    viewModel.openLogSheet()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Entry")
            }
        }

        if (showSheet) {
            LogEntrySheet(
                viewModel = loggingViewModel,
                onDismiss = viewModel::closeLogSheet
            )
        }
    }
}
