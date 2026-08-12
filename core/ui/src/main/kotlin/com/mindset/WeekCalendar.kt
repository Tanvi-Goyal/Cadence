package com.mindset

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val DAY_MS = 86_400_000L

enum class DayState { TODAY, TRAINED, IDLE }

data class WeekCell(
    val epochDay: Long,
    val letter: String,
    val dayOfMonth: String,
    val state: DayState,
)

private val utcWeekdayFormat =
    SimpleDateFormat("EEEEE", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
private val utcDayOfMonthFormat =
    SimpleDateFormat("d", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

private fun cellFor(day: Long, today: Long, trained: Set<Long>): WeekCell {
    val date = Date(day * DAY_MS)
    val state = when (day) {
        today -> DayState.TODAY
        in trained -> DayState.TRAINED
        else -> DayState.IDLE
    }

    return WeekCell(
        day,
        utcWeekdayFormat.format(date),
        utcDayOfMonthFormat.format(date),
        state,
    )
}

/** [weeks] Monday-based rows, oldest first (top) → the week containing [todayEpochDay] last (bottom). */
fun buildCalendarWeeks(
    todayEpochDay: Long,
    trained: Set<Long>,
    weeks: Int,
): List<List<WeekCell>> {
    // epoch-day 0 (1970-01-01) is a Thursday → Monday-based index 3.
    val mondayIndex = (((todayEpochDay % 7) + 3) % 7).toInt()
    val currentMonday = todayEpochDay - mondayIndex
    return (weeks - 1 downTo 0).map { w ->
        val monday = currentMonday - w * 7
        (0..6).map { offset -> cellFor(monday + offset, todayEpochDay, trained) }
    }
}

fun buildDayCells(todayEpochDay: Long, trained: Set<Long>, count: Int): List<WeekCell> =
    ((count - 1) downTo 0).map { back -> cellFor(todayEpochDay - back, todayEpochDay, trained) }
