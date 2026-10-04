package xyz.gaon.componentory.lab

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SampleDatesTest {
    @Test
    fun localCalendarTimestampsKeepCivilDatesAcrossZonesAndDaylightSavingChanges() {
        val dates =
            listOf(
                SampleDates.utcMillis(2024, 1, 15),
                SampleDates.utcMillis(2024, 2, 29),
                SampleDates.utcMillis(2024, 3, 10),
                SampleDates.utcMillis(2024, 11, 3),
            )
        listOf(
                "Pacific/Pago_Pago",
                "Pacific/Kiritimati",
                "America/Los_Angeles",
                "Europe/Berlin",
                "Asia/Seoul",
            )
            .forEach { name ->
                val zone = TimeZone.getTimeZone(name)
                dates.forEach { date ->
                    val local = SampleDates.localMillis(date, zone)
                    val calendar = GregorianCalendar(zone).apply { timeInMillis = local }
                    val expected = SampleDates.parts(date)
                    assertEquals(name, expected.year, calendar.get(Calendar.YEAR))
                    assertEquals(name, expected.month, calendar.get(Calendar.MONTH) + 1)
                    assertEquals(name, expected.day, calendar.get(Calendar.DAY_OF_MONTH))
                    assertEquals(name, 12, calendar.get(Calendar.HOUR_OF_DAY))
                    assertEquals(name, date, SampleDates.utcMillisFromLocal(local, zone))
                }
            }
    }

    @Test
    fun utcMidnightWouldMoveBackwardInNegativeZonesButTheLocalBridgeDoesNot() {
        val zone = TimeZone.getTimeZone("Pacific/Pago_Pago")
        val date = SampleDates.INITIAL_UTC_MILLIS
        val unconverted = GregorianCalendar(zone).apply { timeInMillis = date }
        assertEquals(14, unconverted.get(Calendar.DAY_OF_MONTH))
        val converted =
            GregorianCalendar(zone).apply { timeInMillis = SampleDates.localMillis(date, zone) }
        assertEquals(15, converted.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun copiedDatesChooseTheirOwnMonthWithoutUsingTheBrowsedMonth() {
        assertEquals(
            SampleDates.utcMillis(2024, 2, 1),
            SampleDates.monthUtcMillis(SampleDates.utcMillis(2024, 2, 29)),
        )
        assertEquals(
            SampleDates.INITIAL_MONTH_UTC_MILLIS,
            SampleDates.monthUtcMillis(SampleDates.INITIAL_UTC_MILLIS),
        )
    }

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
