package xyz.gaon.componentory.lab

import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

internal object SampleDates {
    const val INITIAL_UTC_MILLIS = 1705276800000L

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

    fun format(utcMillis: Long, locale: Locale): String =
        DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(utcMillis))
}
