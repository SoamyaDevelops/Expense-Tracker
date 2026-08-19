package com.expensetracker.app.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pattern
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.expensetracker.app.data.LockType
import com.expensetracker.app.util.HashUtil

private enum class SetupStep { CHOOSE_TYPE, ENTER_CODE, CONFIRM_CODE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupLockScreen(
    currentLockType: LockType,
    biometricEnabled: Boolean,
    onSave: (LockType, String?, Boolean) -> Unit,
    onBiometricToggle: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var step by remember { mutableStateOf(SetupStep.CHOOSE_TYPE) }
    var chosenType by remember { mutableStateOf(currentLockType) }
    var firstEntry by remember { mutableStateOf("") }
    var mismatchError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricAvailable = activity != null && BiometricHelper.isAvailable(activity)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Lock") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step == SetupStep.CHOOSE_TYPE) onBack() else step = SetupStep.CHOOSE_TYPE
                    }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp)
        ) {
            when (step) {
                SetupStep.CHOOSE_TYPE -> {
                    Text("Choose how you'd like to secure your data", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))

                    LockOptionRow(
                        title = "None",
                        subtitle = "No lock screen",
                        icon = Icons.Filled.Lock,
                        selected = currentLockType == LockType.NONE,
                        onClick = { onSave(LockType.NONE, null, false) }
                    )
                    LockOptionRow(
                        title = "PIN",
                        subtitle = "4-6 digit numeric code",
                        icon = Icons.Filled.Pin,
                        selected = currentLockType == LockType.PIN,
                        onClick = { chosenType = LockType.PIN; firstEntry = ""; step = SetupStep.ENTER_CODE }
                    )
                    LockOptionRow(
                        title = "Pattern",
                        subtitle = "Draw a connect-the-dots pattern",
                        icon = Icons.Filled.Pattern,
                        selected = currentLockType == LockType.PATTERN,
                        onClick = { chosenType = LockType.PATTERN; firstEntry = ""; step = SetupStep.ENTER_CODE }
                    )
                    LockOptionRow(
                        title = "Password",
                        subtitle = "Alphanumeric password",
                        icon = Icons.Filled.Password,
                        selected = currentLockType == LockType.PASSWORD,
                        onClick = { chosenType = LockType.PASSWORD; firstEntry = ""; step = SetupStep.ENTER_CODE }
                    )

                    if (currentLockType != LockType.NONE) {
                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Fingerprint, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Biometric unlock", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (biometricAvailable) "Use fingerprint or face unlock" else "Not available on this device",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = biometricEnabled,
                                enabled = biometricAvailable,
                                onCheckedChange = onBiometricToggle
                            )
                        }
                    }
                }

                SetupStep.ENTER_CODE, SetupStep.CONFIRM_CODE -> {
                    val isConfirm = step == SetupStep.CONFIRM_CODE
                    Text(
                        if (isConfirm) "Confirm your ${chosenType.name.lowercase()}" else "Set a new ${chosenType.name.lowercase()}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (mismatchError) {
                        Spacer(Modifier.height(8.dp))
                        Text("Didn't match — try again", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(24.dp))

                    fun handleEntry(value: String) {
                        if (!isConfirm) {
                            firstEntry = value
                            mismatchError = false
                            step = SetupStep.CONFIRM_CODE
                        } else {
                            if (value == firstEntry) {
                                onSave(chosenType, HashUtil.sha256(value), biometricEnabled)
                            } else {
                                mismatchError = true
                                step = SetupStep.ENTER_CODE
                                firstEntry = ""
                            }
                        }
                    }

                    when (chosenType) {
                        LockType.PIN -> SetupPinEntry(onComplete = ::handleEntry)
                        LockType.PASSWORD -> SetupPasswordEntry(onComplete = ::handleEntry)
                        LockType.PATTERN -> PatternView(
                            modifier = Modifier.fillMaxWidth(),
                            onPatternComplete = { pattern ->
                                if (pattern.size >= 4) handleEntry(pattern.joinToString(","))
                            }
                        )
                        LockType.NONE -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupPinEntry(onComplete: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(6) { i ->
            val filled = i < pin.length
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
    Spacer(Modifier.height(32.dp))
    NumericKeypad(
        onDigit = { d -> if (pin.length < 6) pin += d },
        onBackspace = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
        onDone = { if (pin.length >= 4) { onComplete(pin) } }
    )
}

@Composable
private fun SetupPasswordEntry(onComplete: (String) -> Unit) {
    var password by remember { mutableStateOf("") }
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Password (min 4 characters)") },
        singleLine = true,
        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(16.dp))
    Button(
        onClick = { if (password.length >= 4) onComplete(password) },
        modifier = Modifier.fillMaxWidth(),
        enabled = password.length >= 4
    ) { Text("Continue") }
}

@Composable
private fun LockOptionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (selected) {
                Icon(Icons.Filled.Lock, contentDescription = "Current", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
