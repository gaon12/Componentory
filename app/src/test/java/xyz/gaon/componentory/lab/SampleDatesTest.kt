package xyz.gaon.componentory.lab

import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SampleDatesTest {
    @Test
    fun fixedInitialDateIsJanuary15AtMidnightUtc() {
        assertEquals(1705276800000L, SampleDates.utcMillis(2024, 1, 15))
        assertEquals(
            SampleDates.Parts(2024, 1, 15),
            SampleDates.parts(SampleDates.INITIAL_UTC_MILLIS),
        )
    }

    @Test
    fun leapDayRoundTripsWithoutMovingToAnotherCalendarDay() {
        val leapDay = SampleDates.utcMillis(2024, 2, 29)
        assertEquals(1709164800000L, leapDay)
        assertEquals(SampleDates.Parts(2024, 2, 29), SampleDates.parts(leapDay))
        assertEquals(SampleDates.Parts(2024, 2, 29), SampleDates.parts(leapDay + 86_399_999))
        assertEquals(SampleDates.Parts(2024, 3, 1), SampleDates.parts(leapDay + 86_400_000))
    }

    @Test
    fun invalidCivilDatesAreRejectedInsteadOfSilentlyNormalized() {
        listOf(Triple(2023, 2, 29), Triple(2024, 0, 15), Triple(2024, 13, 15), Triple(2024, 4, 31))
            .forEach { (year, month, day) ->
                assertThrows(IllegalArgumentException::class.java) {
                    SampleDates.utcMillis(year, month, day)
                }
            }
    }

    @Test
    fun dateStorageIgnoresThaiDefaultLocaleAndDeviceTimeZone() {
        val previousLocale = Locale.getDefault()
        val previousTimeZone = TimeZone.getDefault()
        try {
            Locale.setDefault(Locale("th", "TH"))
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"))
            assertEquals(SampleDates.INITIAL_UTC_MILLIS, SampleDates.utcMillis(2024, 1, 15))
            assertEquals(
                SampleDates.Parts(2024, 1, 15),
                SampleDates.parts(SampleDates.INITIAL_UTC_MILLIS),
            )
            assertEquals(
                "Jan 15, 2024",
                SampleDates.format(SampleDates.INITIAL_UTC_MILLIS, Locale.ENGLISH),
            )
        } finally {
            Locale.setDefault(previousLocale)
            TimeZone.setDefault(previousTimeZone)
        }
    }

    @Test
    fun dateFeedbackUsesTheRequestedLocaleAndUtcInOppositeDeviceTimeZones() {
        val previousTimeZone = TimeZone.getDefault()
        try {
            listOf("Pacific/Pago_Pago", "Pacific/Kiritimati").forEach { zone ->
                TimeZone.setDefault(TimeZone.getTimeZone(zone))
                assertEquals(
                    "Jan 15, 2024",
                    SampleDates.format(SampleDates.INITIAL_UTC_MILLIS, Locale.ENGLISH),
                )
                assertEquals(
                    "15.01.2024",
                    SampleDates.format(SampleDates.INITIAL_UTC_MILLIS, Locale.GERMAN),
                )
            }
        } finally {
            TimeZone.setDefault(previousTimeZone)
        }
    }
}
