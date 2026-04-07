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
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject
import org.weekendware.basil.presentation.logging.LogEntrySheet
import org.weekendware.basil.presentation.logging.LoggingViewModel
import org.weekendware.basil.presentation.theme.BasilTokens

/**
 * The main Dashboard screen, displayed on the home tab.
 *
 * Currently shows a [FloatingActionButton] that opens the [LogEntrySheet]
 * for recording blood glucose, insulin, and carbohydrate data.
 *
 * Future additions:
 * - Summary card with the most recent blood glucose reading
 * - Today's log timeline
 * - Trend indicators and quick-stat chips
 *
 * ViewModels are injected via Koin:
 * - [DashboardViewModel] — controls sheet visibility
 * - [LoggingViewModel] — manages the log entry form state
 */
object DashboardScreen : Screen {

    /**
     * Renders the dashboard content.
     *
     * Observes [DashboardViewModel.showLogSheet] and conditionally presents
     * the [LogEntrySheet]. The FAB resets the form before opening the sheet
     * to ensure a clean state for each new entry.
     */
    @Composable
    override fun Content() {
        val viewModel        = koinInject<DashboardViewModel>()
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
                    .padding(BasilTokens.FabEdgePadding)
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
