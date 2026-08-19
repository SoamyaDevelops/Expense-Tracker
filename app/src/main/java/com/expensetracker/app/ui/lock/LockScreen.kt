package com.expensetracker.app.ui.lock

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.expensetracker.app.data.LockType
import com.expensetracker.app.util.HashUtil
import kotlinx.coroutines.launch

@Composable
fun LockScreen(
    lockType: LockType,
    correctHash: String?,
    biometricEnabled: Boolean,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var error by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val shake = remember { Animatable(0f) }

    fun triggerError() {
        error = true
        scope.launch {
            shake.animateTo(1f, tween(50))
            shake.animateTo(-1f, tween(50))
            shake.animateTo(1f, tween(50))
            shake.animateTo(0f, tween(50))
        }
    }

    fun checkCode(input: String): Boolean {
        val ok = correctHash != null && HashUtil.sha256(input) == correctHash
        if (ok) onUnlocked() else triggerError()
        return ok
    }

    LaunchedEffect(Unit) {
        if (biometricEnabled && activity != null && BiometricHelper.isAvailable(activity)) {
            BiometricHelper.authenticate(
                activity = activity,
                onSuccess = onUnlocked,
                onError = { /* fall back to code entry silently */ }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Text("Expense Tracker", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                if (error) "Incorrect, try again" else "Enter your code to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            when (lockType) {
                LockType.PIN -> PinEntry(onSubmit = { pin -> checkCode(pin) })
                LockType.PASSWORD -> PasswordEntry(onSubmit = { pw -> checkCode(pw) })
                LockType.PATTERN -> PatternView(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    onPatternComplete = { pattern -> checkCode(pattern.joinToString(",")) }
                )
                LockType.NONE -> LaunchedEffect(Unit) { onUnlocked() }
            }

            if (biometricEnabled && activity != null && BiometricHelper.isAvailable(activity)) {
                Spacer(Modifier.height(24.dp))
                OutlinedButton(onClick = {
                    BiometricHelper.authenticate(activity, onSuccess = onUnlocked, onError = {})
                }) {
                    Icon(Icons.Filled.Fingerprint, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Use biometric")
                }
            }
        }
    }
}

@Composable
private fun PinEntry(onSubmit: (String) -> Boolean) {
    var pin by remember { mutableStateOf("") }
    val maxLen = 6

    LaunchedEffect(pin) {
        if (pin.length in 4..maxLen && pin.length == 4) {
            // allow submit at 4 digits by default; handled by button below instead
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(6) { i ->
            val filled = i < pin.length
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
            )
        }
    }
    Spacer(Modifier.height(32.dp))
    NumericKeypad(
        onDigit = { d -> if (pin.length < maxLen) pin += d },
        onBackspace = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
        onDone = { if (pin.length >= 4) { onSubmit(pin); pin = "" } }
    )
}

@Composable
private fun PasswordEntry(onSubmit: (String) -> Boolean) {
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Password") },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
            }
        },
        modifier = Modifier.fillMaxWidth(0.85f)
    )
    Spacer(Modifier.height(16.dp))
    Button(
        onClick = { onSubmit(password); password = "" },
        modifier = Modifier.fillMaxWidth(0.85f)
    ) { Text("Unlock") }
}

@Composable
fun NumericKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onDone: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "back")
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                row.forEach { key ->
                    when (key) {
                        "" -> Spacer(modifier = Modifier.size(64.dp))
                        "back" -> IconButton(
                            onClick = onBackspace,
                            modifier = Modifier.size(64.dp)
                        ) { Icon(Icons.Filled.Backspace, contentDescription = "Backspace") }
                        else -> FilledTonalButton(
                            onClick = { onDigit(key) },
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text(key, style = MaterialTheme.typography.titleLarge) }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onDone) { Text("Confirm") }
    }
}
