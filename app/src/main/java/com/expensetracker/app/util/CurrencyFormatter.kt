package com.expensetracker.app.util

import java.text.DecimalFormat

fun formatAmount(amount: Double, symbol: String = "$"): String {
    val df = DecimalFormat("#,##0.00")
    return "$symbol${df.format(amount)}"
}

fun formatAmountCompact(amount: Double, symbol: String = "$"): String {
    return when {
        amount >= 1_000_000 -> "$symbol${DecimalFormat("#,##0.0").format(amount / 1_000_000)}M"
        amount >= 1_000 -> "$symbol${DecimalFormat("#,##0.0").format(amount / 1_000)}k"
        else -> formatAmount(amount, symbol)
    }
}
