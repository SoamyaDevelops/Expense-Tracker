package com.expensetracker.app.ui.addedit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.app.data.Category
import com.expensetracker.app.data.RecurrenceType
import com.expensetracker.app.data.TransactionType
import com.expensetracker.app.util.DateUtils
import com.expensetracker.app.util.IconUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    expenseId: Long?,
    onDone: () -> Unit,
    viewModel: AddEditViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(expenseId) {
        if (expenseId != null) viewModel.loadExpense(expenseId)
    }
    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    val typeLabel = if (state.type == TransactionType.INCOME) "income" else "expense"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (expenseId == null) "Add $typeLabel" else "Edit $typeLabel") },
                navigationIcon = {
                    IconButton(onClick = onDone) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    TextButton(onClick = viewModel::save) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollStateCompat())
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TransactionType.entries.forEach { type ->
                    val selected = state.type == type
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.updateType(type) },
                        label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (type == TransactionType.INCOME) Color(0xFF2FBF71) else MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.amountText,
                onValueChange = viewModel::updateAmount,
                label = { Text("Amount") },
                leadingIcon = { Text(state.currencySymbol, style = MaterialTheme.typography.titleMedium) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            Text("Category", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.categories) { category ->
                    CategoryChip(
                        category = category,
                        selected = category.id == state.categoryId,
                        onClick = { viewModel.updateCategory(category.id) }
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            Text("Payment Account", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.accounts) { account ->
                    FilterChip(
                        selected = account.id == state.accountId,
                        onClick = { viewModel.updateAccount(account.id) },
                        label = { Text(account.name) }
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            Text("Date", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                val cal = Calendar.getInstance().apply { timeInMillis = state.dateMillis }
                android.app.DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val newCal = Calendar.getInstance()
                        newCal.set(year, month, day, 12, 0, 0)
                        viewModel.updateDate(newCal.timeInMillis)
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(DateUtils.formatDate(state.dateMillis))
            }
            Spacer(Modifier.height(20.dp))

            Text("Repeat", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RecurrenceType.entries.toList()) { rec ->
                    FilterChip(
                        selected = state.recurrence == rec,
                        onClick = { viewModel.updateRecurrence(rec) },
                        label = { Text(rec.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Auto Pay", style = MaterialTheme.typography.titleSmall)
                    Text("Remind me before payment", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = state.isAutoPay, onCheckedChange = viewModel::updateAutoPay)
            }
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::updateNote,
                label = { Text("Note (optional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            state.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (expenseId == null) "Save $typeLabel" else "Update $typeLabel",
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun CategoryChip(category: Category, selected: Boolean, onClick: () -> Unit) {
    val color = runCatching { Color(android.graphics.Color.parseColor(category.colorHex)) }.getOrDefault(Color.Gray)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                IconUtils.getCategoryIcon(category.iconKey),
                contentDescription = null,
                tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(category.name, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun rememberScrollStateCompat() = androidx.compose.foundation.rememberScrollState()
