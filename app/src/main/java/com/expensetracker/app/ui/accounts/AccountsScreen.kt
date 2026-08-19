package com.expensetracker.app.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.app.data.Account
import com.expensetracker.app.data.AccountType
import com.expensetracker.app.util.IconUtils
import com.expensetracker.app.util.formatAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    viewModel: AccountsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var accountToAdjust by remember { mutableStateOf<Account?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accounts & Balances") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { showTransferDialog = true }) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = "Transfer")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddAccountDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Account")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Your Accounts", style = MaterialTheme.typography.titleMedium)
            }
            items(state.accounts) { account ->
                AccountRow(
                    account = account, 
                    currencySymbol = state.currencySymbol,
                    onClick = { accountToAdjust = account }
                )
            }
            
            if (state.transfers.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Recent Transfers", style = MaterialTheme.typography.titleMedium)
                }
                items(state.transfers) { transfer ->
                    val from = state.accounts.find { it.id == transfer.fromAccountId }?.name ?: "Unknown"
                    val to = state.accounts.find { it.id == transfer.toAccountId }?.name ?: "Unknown"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text("$from → $to", style = MaterialTheme.typography.bodyLarge)
                            }
                            Text(formatAmount(transfer.amount, state.currencySymbol), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAddAccountDialog) {
        AddAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onConfirm = { name, balance, type ->
                viewModel.addAccount(name, balance, type)
                showAddAccountDialog = false
            }
        )
    }

    if (showTransferDialog) {
        TransferDialog(
            accounts = state.accounts,
            onDismiss = { showTransferDialog = false },
            onConfirm = { from, to, amount ->
                viewModel.transfer(from, to, amount)
                showTransferDialog = false
            }
        )
    }

    accountToAdjust?.let { account ->
        AdjustBalanceDialog(
            account = account,
            onDismiss = { accountToAdjust = null },
            onConfirm = { newBalance ->
                viewModel.adjustBalance(account, newBalance)
                accountToAdjust = null
            }
        )
    }
}

@Composable
fun AccountRow(account: Account, currencySymbol: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    IconUtils.getAccountIcon(account.type.name),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(account.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    account.type.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                formatAmount(account.balance, currencySymbol),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (account.balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun AddAccountDialog(onDismiss: () -> Unit, onConfirm: (String, Double, AccountType) -> Unit) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AccountType.CASH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Account Name") })
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("Initial Balance") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Text("Type", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, balance.toDoubleOrNull() ?: 0.0, type) }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdjustBalanceDialog(account: Account, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var balance by remember { mutableStateOf(account.balance.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Balance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Account: ${account.name}")
                Text("Current Balance: ${account.balance}")
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("New Balance") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(balance.toDoubleOrNull() ?: account.balance) }) { Text("Update") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun TransferDialog(accounts: List<Account>, onDismiss: () -> Unit, onConfirm: (Long, Long, Double) -> Unit) {
    var fromIdx by remember { mutableStateOf(0) }
    var toIdx by remember { mutableStateOf(if (accounts.size > 1) 1 else 0) }
    var amount by remember { mutableStateOf("") }

    if (accounts.size < 2) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Transfer") },
            text = { Text("You need at least two accounts to transfer funds.") },
            confirmButton = { Button(onClick = onDismiss) { Text("OK") } }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer Funds") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("From: ${accounts[fromIdx].name}")
                Slider(
                    value = fromIdx.toFloat(),
                    onValueChange = { fromIdx = it.toInt() },
                    valueRange = 0f..(accounts.size - 1).toFloat(),
                    steps = accounts.size - 2
                )
                Text("To: ${accounts[toIdx].name}")
                Slider(
                    value = toIdx.toFloat(),
                    onValueChange = { toIdx = it.toInt() },
                    valueRange = 0f..(accounts.size - 1).toFloat(),
                    steps = accounts.size - 2
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (fromIdx != toIdx) {
                    onConfirm(accounts[fromIdx].id, accounts[toIdx].id, amount.toDoubleOrNull() ?: 0.0)
                }
            }) { Text("Transfer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
