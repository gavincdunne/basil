package org.weekendware.basil.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen

object SettingsScreen : Screen {
    @Composable
    override fun Content() {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(24.dp))

            var suppliesReminder by remember { mutableStateOf(true) }
            var dailyCheckInReminder by remember { mutableStateOf(false) }

            Row {
                Text("Remind me to order supplies")
                Spacer(Modifier.weight(1f))
                Switch(checked = suppliesReminder, onCheckedChange = { suppliesReminder = it })
            }

            Row {
                Text("Daily check-in")
                Spacer(Modifier.weight(1f))
                Switch(checked = dailyCheckInReminder, onCheckedChange = { dailyCheckInReminder = it })
            }
        }
    }
}
