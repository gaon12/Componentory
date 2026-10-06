package xyz.gaon.componentory.lab

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver

private val namedSampleStateSaver =
    mapSaver(
        save = { state: SampleState ->
            mapOf(
                "value" to state.value,
                "text" to state.text,
                "icon" to state.icon,
                "rangeEnd" to state.rangeEnd,
                "dateUtcMillis" to state.dateUtcMillis,
                "dateDraftUtcMillis" to state.dateDraftUtcMillis,
                "timeMinutes" to state.timeMinutes,
                "timeDraftMinutes" to state.timeDraftMinutes,
                "time24Hour" to state.time24Hour,
                "timeInputMode" to state.timeInputMode,
                "containerClickable" to state.containerClickable,
                "inlineDateUtcMillis" to state.inlineDateUtcMillis,
                "dateRangeStartUtcMillis" to state.dateRangeStartUtcMillis,
                "dateRangeEndUtcMillis" to state.dateRangeEndUtcMillis,
                "dateInputMode" to state.dateInputMode,
                "dateDisplayedMonthUtcMillis" to state.dateDisplayedMonthUtcMillis,
                "chronometerBaseMillis" to state.chronometerBaseMillis,
            )
        },
        restore = { fields ->
            val value = fields["value"] as? Int ?: return@mapSaver null
            val text = fields["text"] as? String ?: return@mapSaver null
            SampleState(
                initialValue = value,
                initialText = text,
                initialIcon = fields["icon"] as? String ?: "",
                initialRangeEnd = fields["rangeEnd"] as? Int ?: 80,
                initialDateUtcMillis =
                    fields["dateUtcMillis"] as? Long ?: SampleDates.INITIAL_UTC_MILLIS,
                initialDateDraftUtcMillis = fields["dateDraftUtcMillis"] as? Long,
                initialTimeMinutes = fields["timeMinutes"] as? Int ?: SampleTimes.INITIAL_MINUTES,
                initialTimeDraftMinutes = fields["timeDraftMinutes"] as? Int,
                initialTime24Hour = fields["time24Hour"] as? Boolean ?: true,
                initialTimeInputMode = fields["timeInputMode"] as? Boolean ?: false,
                initialContainerClickable = fields["containerClickable"] as? Boolean ?: true,
                initialInlineDateUtcMillis =
                    if (fields.containsKey("inlineDateUtcMillis"))
                        fields["inlineDateUtcMillis"] as? Long
                    else SampleDates.INITIAL_UTC_MILLIS,
                initialDateRangeStartUtcMillis = fields["dateRangeStartUtcMillis"] as? Long,
                initialDateRangeEndUtcMillis = fields["dateRangeEndUtcMillis"] as? Long,
                initialDateInputMode = fields["dateInputMode"] as? Boolean ?: false,
                initialDateDisplayedMonthUtcMillis =
                    fields["dateDisplayedMonthUtcMillis"] as? Long
                        ?: SampleDates.INITIAL_MONTH_UTC_MILLIS,
                initialChronometerBaseMillis = fields["chronometerBaseMillis"] as? Long ?: 0,
            )
        },
    )

internal val sampleStateSaver =
    Saver<SampleState, Any>(
        save = { state -> with(namedSampleStateSaver) { save(state) } },
        restore = { saved ->
            when {
                saved !is List<*> -> null
                saved.firstOrNull() is Int -> restoreLegacySampleState(saved)
                saved.size % 2 == 0 && saved.chunked(2).all { it[0] is String } ->
                    namedSampleStateSaver.restore(saved)
                else -> null
            }
        },
    )

// Only old saves depend on field order. New fields must use named keys above.
private fun restoreLegacySampleState(fields: List<*>): SampleState? {
    val value = fields.getOrNull(0) as? Int ?: return null
    val text = fields.getOrNull(1) as? String ?: return null
    return SampleState(
        initialValue = value,
        initialText = text,
        initialIcon = fields.getOrNull(2) as? String ?: "",
        initialRangeEnd = fields.getOrNull(3) as? Int ?: 80,
        initialDateUtcMillis = fields.getOrNull(4) as? Long ?: SampleDates.INITIAL_UTC_MILLIS,
        initialDateDraftUtcMillis =
            (fields.getOrNull(5) as? Long)?.takeUnless { it == Long.MIN_VALUE },
        initialTimeMinutes = fields.getOrNull(6) as? Int ?: SampleTimes.INITIAL_MINUTES,
        initialTimeDraftMinutes = (fields.getOrNull(7) as? Int)?.takeUnless { it == -1 },
        initialTime24Hour = fields.getOrNull(8) as? Boolean ?: true,
        initialTimeInputMode = fields.getOrNull(9) as? Boolean ?: false,
        initialContainerClickable = fields.getOrNull(10) as? Boolean ?: true,
        initialInlineDateUtcMillis =
            (fields.getOrNull(11) as? Long ?: SampleDates.INITIAL_UTC_MILLIS).takeUnless {
                it == Long.MIN_VALUE
            },
        initialDateRangeStartUtcMillis =
            (fields.getOrNull(12) as? Long)?.takeUnless { it == Long.MIN_VALUE },
        initialDateRangeEndUtcMillis =
            (fields.getOrNull(13) as? Long)?.takeUnless { it == Long.MIN_VALUE },
        initialDateInputMode = fields.getOrNull(14) as? Boolean ?: false,
        initialDateDisplayedMonthUtcMillis =
            fields.getOrNull(15) as? Long ?: SampleDates.INITIAL_MONTH_UTC_MILLIS,
        initialChronometerBaseMillis = fields.getOrNull(16) as? Long ?: 0,
    )
}
