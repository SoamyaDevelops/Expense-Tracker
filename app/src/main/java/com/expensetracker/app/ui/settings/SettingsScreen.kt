package com.expensetracker.app.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.app.data.LockType
import com.expensetracker.app.data.ThemeMode
import com.expensetracker.app.util.CsvExporter
import com.expensetracker.app.util.CsvImporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenLockSetup: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            scope.launch {
                val count = viewModel.importCsv(it)
                Toast.makeText(context, "Imported $count expenses", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { SectionLabel("Appearance") }
            item {
                SettingsRow(
                    icon = Icons.Filled.DarkMode,
                    title = "Theme",
                    subtitle = state.themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { showThemeDialog = true }
                )
            }

            item { Spacer(Modifier.height(6.dp)); SectionLabel("Security") }
            item {
                SettingsRow(
                    icon = Icons.Filled.Lock,
                    title = "App lock",
                    subtitle = when (state.lockType) {
                        LockType.NONE -> "Off"
                        else -> state.lockType.name.lowercase().replaceFirstChar { it.uppercase() } +
                            if (state.biometricEnabled) " + biometric" else ""
                    },
                    onClick = onOpenLockSetup
                )
            }

            item { Spacer(Modifier.height(6.dp)); SectionLabel("Budget & currency") }
            item {
                SettingsRow(
                    icon = Icons.Filled.Savings,
                    title = "Monthly budget",
                    subtitle = if (state.monthlyBudget > 0) "${state.currencySymbol}${state.monthlyBudget}" else "Not set",
                    onClick = { showBudgetDialog = true }
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Filled.ChevronRight,
                    title = "Currency symbol",
                    subtitle = state.currencySymbol,
                    onClick = { showCurrencyDialog = true }
                )
            }

            item { Spacer(Modifier.height(6.dp)); SectionLabel("Data") }
            item {
                SettingsRow(
                    icon = Icons.Filled.FileUpload,
                    title = "Import from CSV",
                    subtitle = "Load expenses from a file",
                    onClick = { importLauncher.launch("text/*") }
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Filled.FileDownload,
                    title = "Export as CSV",
                    subtitle = "Share all your expenses",
                    onClick = {
                        scope.launch {
                            val (expenses, categories, accounts) = viewModel.exportCsv()
                            val uri = CsvExporter.export(context, expenses, categories, accounts)
                            if (uri != null) {
                                context.startActivity(CsvExporter.shareIntent(context, uri))
                            }
                        }
                    }
                )
            }

            item { Spacer(Modifier.height(60.dp)) }
        }
    }

    if (showThemeDialog) {
        ThemePickerDialog(
            current = state.themeMode,
            onDismiss = { showThemeDialog = false },
            onSelect = { viewModel.setThemeMode(it); showThemeDialog = false }
        )
    }
    if (showCurrencyDialog) {
        CurrencyDialog(
            current = state.currencySymbol,
            onDismiss = { showCurrencyDialog = false },
            onConfirm = { viewModel.setCurrency(it); showCurrencyDialog = false }
        )
    }
    if (showBudgetDialog) {
        BudgetDialog(
            current = state.monthlyBudget,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { viewModel.setMonthlyBudget(it); showBudgetDialog = false }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 2.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ThemePickerDialog(current: ThemeMode, onDismiss: () -> Unit, onSelect: (ThemeMode) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Theme") },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == mode, onClick = { onSelect(mode) })
                        Spacer(Modifier.width(8.dp))
                        Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun CurrencyDialog(current: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var value by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Currency symbol") },
        text = {
            OutlinedTextField(value = value, onValueChange = { value = it }, singleLine = true, modifier = Modifier.fillMaxWidth())
        },
        confirmButton = { TextButton(onClick = { if (value.isNotBlank()) onConfirm(value) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun BudgetDialog(current: Double, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var value by remember { mutableStateOf(if (current > 0) current.toString() else "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monthly budget") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it.filter { c -> c.isDigit() || c == '.' } },
                singleLine = true,
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(value.toDoubleOrNull() ?: 0.0) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
