package org.weekendware.basil.presentation.logging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.weekendware.basil.domain.model.BgUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEntrySheet(
    viewModel: LoggingViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Log Entry", style = MaterialTheme.typography.titleLarge)

            // Blood glucose
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Blood Glucose", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.bgValue,
                        onValueChange = viewModel::onBgValueChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    BgUnit.entries.forEach { unit ->
                        if (state.bgUnit == unit) {
                            Button(onClick = {}) { Text(unit.label) }
                        } else {
                            OutlinedButton(onClick = { viewModel.onBgUnitChange(unit) }) {
                                Text(unit.label)
                            }
                        }
                    }
                }
            }

            // Insulin
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Insulin", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = state.insulinUnits,
                    onValueChange = viewModel::onInsulinChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Units") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            // Carbs
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Carbs", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = state.carbsGrams,
                    onValueChange = viewModel::onCarbsChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Grams") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            Button(
                onClick = { viewModel.save(onDismiss) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.hasAnyValue
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
