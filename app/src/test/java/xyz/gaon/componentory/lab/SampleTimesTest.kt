package xyz.gaon.componentory.lab

import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SampleTimesTest {
    @Test
    fun fixedInitialTimeIsTenThirty() {
        assertEquals(630, SampleTimes.minutes(10, 30))
        assertEquals(SampleTimes.Parts(10, 30), SampleTimes.parts(SampleTimes.INITIAL_MINUTES))
    }

    @Test
    fun midnightNoonAndTheLastMinuteRoundTripWithinOneDay() {
        listOf(
                0 to SampleTimes.Parts(0, 0),
                720 to SampleTimes.Parts(12, 0),
                1439 to SampleTimes.Parts(23, 59),
            )
            .forEach { (minutes, parts) ->
                assertEquals(parts, SampleTimes.parts(minutes))
                assertEquals(minutes, SampleTimes.minutes(parts.hour, parts.minute))
            }
    }

    @Test
    fun invalidHoursMinutesAndDayTotalsAreRejected() {
        listOf(-1 to 30, 24 to 0, 10 to -1, 10 to 60).forEach { (hour, minute) ->
            assertThrows(IllegalArgumentException::class.java) { SampleTimes.minutes(hour, minute) }
        }
        listOf(-1, 1440).forEach { minutes ->
            assertThrows(IllegalArgumentException::class.java) { SampleTimes.parts(minutes) }
            assertThrows(IllegalArgumentException::class.java) {
                SampleTimes.format(minutes, Locale.ENGLISH, "HH:mm")
            }
        }
    }

    @Test
    fun requestedClockPatternKeepsAmPmAndLocalizedPeriods() {
        assertEquals("00:00", SampleTimes.format(0, Locale.ENGLISH, "HH:mm"))
        assertEquals("12:00 AM", SampleTimes.format(0, Locale.ENGLISH, "h:mm a"))
        assertEquals("12:00 PM", SampleTimes.format(720, Locale.ENGLISH, "h:mm a"))
        assertEquals("1:05 PM", SampleTimes.format(785, Locale.ENGLISH, "h:mm a"))
        assertEquals("오후 1:05", SampleTimes.format(785, Locale.KOREAN, "a h:mm"))
    }

    @Test
    fun civilTimeFormattingIgnoresDeviceTimeZoneAndThaiDefaultLocale() {
        val previousLocale = Locale.getDefault()
        val previousTimeZone = TimeZone.getDefault()
        try {
            Locale.setDefault(Locale("th", "TH"))
            listOf("Pacific/Pago_Pago", "Pacific/Kiritimati").forEach { zone ->
                TimeZone.setDefault(TimeZone.getTimeZone(zone))
                assertEquals("10:30", SampleTimes.format(630, Locale.ENGLISH, "HH:mm"))
                assertEquals("11:59 PM", SampleTimes.format(1439, Locale.ENGLISH, "h:mm a"))
                assertEquals(SampleTimes.Parts(23, 59), SampleTimes.parts(1439))
            }
        } finally {
            Locale.setDefault(previousLocale)
            TimeZone.setDefault(previousTimeZone)
        }
    }
}
