package xyz.gaon.componentory.lab

import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

internal object SampleDates {
    const val INITIAL_UTC_MILLIS = 1705276800000L
    const val INITIAL_MONTH_UTC_MILLIS = 1704067200000L

    data class Parts(val year: Int, val month: Int, val day: Int)

    fun utcMillis(year: Int, month: Int, day: Int): Long =
        GregorianCalendar(TimeZone.getTimeZone("UTC"))
            .apply {
                clear()
                isLenient = false
                set(year, month - 1, day)
            }
            .timeInMillis

    fun parts(utcMillis: Long): Parts {
        val calendar = GregorianCalendar(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = utcMillis
        return Parts(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }

    fun monthUtcMillis(dateUtcMillis: Long): Long {
        val date = parts(dateUtcMillis)
        return utcMillis(date.year, date.month, 1)
    }

    // CalendarView consumes local timestamps; UTC midnight can select the previous local day.
    fun localMillis(utcMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val date = parts(utcMillis)
        return GregorianCalendar(timeZone)
            .apply {
                clear()
                isLenient = false
                // Local noon avoids ordinary daylight-saving gaps at midnight.
                set(date.year, date.month - 1, date.day, 12, 0)
            }
            .timeInMillis
    }

    fun utcMillisFromLocal(localMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val calendar = GregorianCalendar(timeZone)
        calendar.timeInMillis = localMillis
        return utcMillis(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }

    fun format(utcMillis: Long, locale: Locale): String =
        DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(utcMillis))
}
