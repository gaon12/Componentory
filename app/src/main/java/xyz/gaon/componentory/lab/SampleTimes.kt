package xyz.gaon.componentory.lab

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal object SampleTimes {
    const val INITIAL_MINUTES = 630

    data class Parts(val hour: Int, val minute: Int)

    fun minutes(hour: Int, minute: Int): Int {
        require(hour in 0..23 && minute in 0..59) { "Time must be within one calendar day" }
        return hour * 60 + minute
    }

    fun parts(minutes: Int): Parts {
        require(minutes in 0..1439) { "Time must be within one calendar day" }
        return Parts(minutes / 60, minutes % 60)
    }

    fun format(minutes: Int, locale: Locale, pattern: String): String {
        parts(minutes)
        return SimpleDateFormat(pattern, locale)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(minutes * 60_000L))
    }
}
