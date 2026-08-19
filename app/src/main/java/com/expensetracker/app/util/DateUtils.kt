package com.expensetracker.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    fun startOfDay(millis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun endOfDay(millis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun startOfMonth(monthsAgo: Int = 0): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -monthsAgo)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun endOfMonth(monthsAgo: Int = 0): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -monthsAgo)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun startOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun formatDate(millis: Long, pattern: String = "d MMM yyyy"): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))

    fun formatDayMonth(millis: Long): String = formatDate(millis, "d MMM")

    fun parseDate(dateStr: String, pattern: String = "yyyy-MM-dd"): Long? =
        try { SimpleDateFormat(pattern, Locale.getDefault()).parse(dateStr)?.time } catch (e: Exception) { null }

    fun monthLabel(monthsAgo: Int = 0): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -monthsAgo)
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    fun isToday(millis: Long): Boolean {
        val today = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = millis }
        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    fun isYesterday(millis: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val target = Calendar.getInstance().apply { timeInMillis = millis }
        return cal.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }
}
