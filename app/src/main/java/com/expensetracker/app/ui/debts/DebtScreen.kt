package com.expensetracker.app.ui.debts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.app.data.Debt
import com.expensetracker.app.data.DebtType
import com.expensetracker.app.util.DateUtils
import com.expensetracker.app.util.formatAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtScreen(
    onBack: () -> Unit,
    viewModel: DebtViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debt & Lend Tracking") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Entry")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DebtSummaryCard(state)
            }

            if (state.debts.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No debts or loans tracked yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(state.debts) { debt ->
                    DebtRow(
                        debt = debt,
                        currencySymbol = state.currencySymbol,
                        onToggleResolved = { viewModel.toggleResolved(debt) },
                        onDelete = { viewModel.deleteDebt(debt) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddDebtDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amount, type, note ->
                viewModel.addDebt(name, amount, type, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DebtSummaryCard(state: DebtUiState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Total Lent", style = MaterialTheme.typography.labelMedium)
                Text(
                    formatAmount(state.totalLent, state.currencySymbol),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF4A90FF)
                )
            }
            Column(Modifier.weight(1f)) {
                Text("Total Borrowed", style = MaterialTheme.typography.labelMedium)
                Text(
                    formatAmount(state.totalBorrowed, state.currencySymbol),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFFF4A4A)
                )
            }
        }
    }
}

@Composable
private fun DebtRow(
    debt: Debt,
    currencySymbol: String,
    onToggleResolved: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (debt.isResolved) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val color = if (debt.type == DebtType.LENT) Color(0xFF4A90FF) else Color(0xFFFF4A4A)
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (debt.type == DebtType.LENT) Icons.Filled.CallMade else Icons.Filled.CallReceived,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    debt.personName,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (debt.isResolved) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                Text(
                    "${if (debt.type == DebtType.LENT) "Lent" else "Borrowed"} · ${DateUtils.formatDayMonth(debt.dateMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatAmount(debt.amount, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (debt.isResolved) MaterialTheme.colorScheme.onSurfaceVariant else color
                )
                Row {
                    IconButton(onClick = onToggleResolved, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (debt.isResolved) Icons.Filled.Undo else Icons.Filled.CheckCircle,
                            contentDescription = "Resolve",
                            tint = if (debt.isResolved) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF2FBF71),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddDebtDialog(onDismiss: () -> Unit, onConfirm: (String, Double, DebtType, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(DebtType.LENT) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Debt / Loan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DebtType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Person Name") }, singleLine = true)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note (optional)") }, maxLines = 2)
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amt > 0) onConfirm(name, amt, type, note) 
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
