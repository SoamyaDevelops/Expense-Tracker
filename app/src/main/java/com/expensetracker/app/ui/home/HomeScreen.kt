package com.expensetracker.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.app.data.Account
import com.expensetracker.app.data.Category
import com.expensetracker.app.data.Expense
import com.expensetracker.app.data.TransactionType
import com.expensetracker.app.util.DateUtils
import com.expensetracker.app.util.IconUtils
import com.expensetracker.app.util.formatAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddExpense: () -> Unit,
    onEditExpense: (Long) -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenDebts: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val categoryMap = remember(state.categories) { state.categories.associateBy { it.id } }
    var showSearch by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showSearch) {
                TopAppBar(
                    title = {
                        TextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::setSearchQuery,
                            placeholder = { Text("Search expenses...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                            ),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { 
                                    viewModel.setSearchQuery("")
                                    showSearch = false 
                                }) { Icon(Icons.Filled.Close, contentDescription = "Close search") }
                            }
                        )
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("Expense Tracker", fontWeight = FontWeight.SemiBold) },
                    actions = {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = onOpenAccounts) {
                            Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = "Accounts")
                        }
                        IconButton(onClick = onOpenDebts) {
                            Icon(Icons.Outlined.HistoryEdu, contentDescription = "Debts")
                        }
                        IconButton(onClick = onOpenCategories) {
                            Icon(Icons.Outlined.Category, contentDescription = "Categories")
                        }
                        IconButton(onClick = onOpenStats) {
                            Icon(Icons.Outlined.BarChart, contentDescription = "Stats")
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense) {
                Icon(Icons.Filled.Add, contentDescription = "Add expense")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SummaryCard(state = state, onRangeSelected = viewModel::setRange)
            }

            if (state.streakDays >= 2 && state.searchQuery.isEmpty()) {
                item {
                    StreakBanner(days = state.streakDays)
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryTabRow(
                        selectedTabIndex = when (state.selectedType) {
                            null -> 0
                            TransactionType.EXPENSE -> 1
                            TransactionType.INCOME -> 2
                        },
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        Tab(
                            selected = state.selectedType == null,
                            onClick = { viewModel.setTransactionType(null) },
                            text = { Text("All") }
                        )
                        Tab(
                            selected = state.selectedType == TransactionType.EXPENSE,
                            onClick = { viewModel.setTransactionType(TransactionType.EXPENSE) },
                            text = { Text("Expenses") }
                        )
                        Tab(
                            selected = state.selectedType == TransactionType.INCOME,
                            onClick = { viewModel.setTransactionType(TransactionType.INCOME) },
                            text = { Text("Incomes") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (state.searchQuery.isNotEmpty()) "Search Results" else "Recent",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (state.filterCategoryId != null) {
                            TextButton(onClick = { viewModel.setFilterCategory(null) }) {
                                Text("Clear Filter")
                            }
                        }
                    }
                    
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.categories) { category ->
                            FilterChip(
                                selected = state.filterCategoryId == category.id,
                                onClick = { 
                                    viewModel.setFilterCategory(
                                        if (state.filterCategoryId == category.id) null else category.id
                                    )
                                },
                                label = { Text(category.name) }
                            )
                        }
                    }
                }
            }

            if (state.expenses.isEmpty()) {
                item { EmptyState() }
            } else {
                items(state.expenses, key = { it.id }) { expense ->
                    ExpenseRow(
                        expense = expense,
                        category = categoryMap[expense.categoryId],
                        currencySymbol = state.currencySymbol,
                        onClick = { onEditExpense(expense.id) },
                        onFavorite = { viewModel.toggleFavorite(expense) },
                        onDelete = { viewModel.deleteExpense(expense) }
                    )
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun SummaryCard(state: HomeUiState, onRangeSelected: (RangeFilter) -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        "Total Balance",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                    Text(
                        formatAmount(state.totalBalance, state.currencySymbol),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                rangeLabel(state.range),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                    Text(formatAmount(state.totalIncome, state.currencySymbol), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
                Column(Modifier.weight(1f)) {
                    Text("Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                    Text(formatAmount(state.totalExpenses, state.currencySymbol), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            if (state.budget > 0 && state.range == RangeFilter.MONTH) {
                Spacer(Modifier.height(16.dp))
                val progress = (state.totalExpenses / state.budget).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (progress >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${formatAmount(state.totalExpenses, state.currencySymbol)} of ${formatAmount(state.budget, state.currencySymbol)} budget",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RangeFilter.entries.forEach { range ->
                    val selected = state.range == range
                    FilterChip(
                        selected = selected,
                        onClick = { onRangeSelected(range) },
                        label = { Text(range.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = Color.Transparent,
                            labelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}

private fun rangeLabel(range: RangeFilter): String = when (range) {
    RangeFilter.TODAY -> "Today"
    RangeFilter.WEEK -> "This week"
    RangeFilter.MONTH -> DateUtils.monthLabel()
    RangeFilter.ALL -> "All time"
}

@Composable
private fun StreakBanner(days: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "$days-day logging streak — keep tracking daily!",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No expenses yet", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            "Tap \"Add expense\" to log your first one.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExpenseRow(
    expense: Expense,
    category: Category?,
    currencySymbol: String,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(expense.id) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 6 },
        exit = fadeOut(tween(150))
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val color = runCatching { Color(android.graphics.Color.parseColor(category?.colorHex ?: "#8A8F98")) }
                    .getOrDefault(Color.Gray)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        IconUtils.getCategoryIcon(category?.iconKey ?: ""),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(expense.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${category?.name ?: "Uncategorized"} · ${dateLabel(expense.dateMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    (if (expense.type == TransactionType.INCOME) "+" else "-") + formatAmount(expense.amount, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (expense.type == TransactionType.INCOME) Color(0xFF2FBF71) else MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onFavorite) {
                    Icon(
                        if (expense.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (expense.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                var showDeleteConfirm by remember { mutableStateOf(false) }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        title = { Text("Delete expense?") },
                        text = { Text("This can't be undone.") },
                        confirmButton = {
                            TextButton(onClick = { onDelete(); showDeleteConfirm = false }) { Text("Delete") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                        }
                    )
                }
            }
        }
    }
}

private fun dateLabel(millis: Long): String = when {
    DateUtils.isToday(millis) -> "Today"
    DateUtils.isYesterday(millis) -> "Yesterday"
    else -> DateUtils.formatDayMonth(millis)
}
