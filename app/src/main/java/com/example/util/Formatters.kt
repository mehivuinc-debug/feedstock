package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val localeId = Locale("id", "ID")
    private val intFormat = NumberFormat.getNumberInstance(localeId).apply {
        maximumFractionDigits = 0
    }
    private val decFormat = NumberFormat.getNumberInstance(localeId).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 2
    }

    fun formatInt(value: Number): String = intFormat.format(value)

    fun formatDecimal(value: Double): String = decFormat.format(value)

    fun todayIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun formatDateIndo(isoDate: String): String {
        if (isoDate.isBlank()) return "-"
        val parts = isoDate.split("-")
        if (parts.size != 3) return isoDate
        val months = listOf(
            "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
            "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
        )
        val day = parts[2].toIntOrNull() ?: return isoDate
        val monthIdx = (parts[1].toIntOrNull() ?: 1) - 1
        val monthName = months.getOrElse(monthIdx) { parts[1] }
        return "$day $monthName ${parts[0]}"
    }

    fun formatShortDate(isoDate: String): String {
        if (isoDate.isBlank()) return "-"
        val parts = isoDate.split("-")
        if (parts.size != 3) return isoDate
        val months = listOf(
            "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
            "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
        )
        val day = parts[2].toIntOrNull() ?: return isoDate
        val monthIdx = (parts[1].toIntOrNull() ?: 1) - 1
        val monthName = months.getOrElse(monthIdx) { parts[1] }
        return "$day $monthName"
    }
}
