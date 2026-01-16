package com.example.dukaai.util

import java.util.Calendar

/**
 * Utility object for common date/time operations.
 * Centralizes date calculations to avoid code duplication across repositories and ViewModels.
 */
object DateUtils {

    /**
     * Get the start of the current day (00:00:00.000)
     * @return Timestamp in milliseconds
     */
    fun getStartOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Get the end of the current day (23:59:59.999)
     * @return Timestamp in milliseconds
     */
    fun getEndOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    /**
     * Get the start of a specific day
     * @param timestamp Any timestamp within the target day
     * @return Timestamp for 00:00:00.000 of that day
     */
    fun getStartOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Get the end of a specific day
     * @param timestamp Any timestamp within the target day
     * @return Timestamp for 23:59:59.999 of that day
     */
    fun getEndOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    /**
     * Get the start of the current week (Monday 00:00:00.000)
     * @return Timestamp in milliseconds
     */
    fun getStartOfWeek(): Long {
        return Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Get the start of the current month (1st day, 00:00:00.000)
     * @return Timestamp in milliseconds
     */
    fun getStartOfMonth(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Get the end of the current month (last day, 23:59:59.999)
     * @return Timestamp in milliseconds
     */
    fun getEndOfMonth(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    /**
     * Get timestamp for N days ago at start of day
     * @param days Number of days to go back
     * @return Timestamp in milliseconds
     */
    fun getDaysAgo(days: Int): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Get the start of yesterday (00:00:00.000)
     * @return Timestamp in milliseconds
     */
    fun getStartOfYesterday(): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Get the end of yesterday (23:59:59.999)
     * @return Timestamp in milliseconds
     */
    fun getEndOfYesterday(): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    /**
     * Format timestamp to date string (YYYY-MM-DD)
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date string
     */
    fun formatDate(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    /**
     * Get hour of day from timestamp (0-23)
     * @param timestamp Timestamp in milliseconds
     * @return Hour of day (0-23)
     */
    fun getHourOfDay(timestamp: Long): Int {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
        }.get(Calendar.HOUR_OF_DAY)
    }

    /**
     * Format timestamp to full datetime string
     * @param timestamp Timestamp in milliseconds
     * @return Formatted datetime string
     */
    fun formatDateTime(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return String.format("%04d-%02d-%02d %02d:%02d", year, month, day, hour, minute)
    }
}
