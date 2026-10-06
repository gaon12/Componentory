package xyz.gaon.componentory.compare

import androidx.compose.runtime.saveable.mapSaver
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.lab.SampleDates
import xyz.gaon.componentory.lab.SampleState
import xyz.gaon.componentory.lab.SampleTimes

// Only fields consumed by a displayed control belong here. Results and drafts never do.
class SampleSetup
private constructor(
    val component: LabComponent,
    val sourceFamily: DesignFamily,
    val value: Int?,
    val text: String?,
    val iconId: String?,
    val rangeEnd: Int?,
    val dateUtcMillis: Long?,
    val timeMinutes: Int?,
    val time24Hour: Boolean?,
    val containerClickable: Boolean?,
    val inlineDate: InlineDateInput?,
    val dateRange: DateRangeInput?,
    val chronometerBaseMillis: Long?,
) {
    private val hasNonIconInputs: Boolean
        get() =
            value != null ||
                text != null ||
                dateUtcMillis != null ||
                timeMinutes != null ||
                time24Hour != null ||
                containerClickable != null ||
                inlineDate != null ||
                dateRange != null ||
                chronometerBaseMillis != null

    fun copyTo(
        targetFamily: DesignFamily,
        runtimeApi: Int,
        iconAvailable: Boolean = false,
        targetIconId: String = "",
    ): SetupCopyResult {
        if (sourceFamily.unsupportedReason(component, runtimeApi) != null) {
            return SetupCopyResult(reason = SetupCopyReason.SOURCE_UNSUPPORTED)
        }
        if (targetFamily.unsupportedReason(component, runtimeApi) != null) {
            return SetupCopyResult(reason = SetupCopyReason.TARGET_UNSUPPORTED)
        }
        if (!hasNonIconInputs && iconId == null) {
            return SetupCopyResult(reason = SetupCopyReason.NO_INPUTS)
        }
        if (inlineDate != null && inlineDate.utcMillis == null && targetFamily.platform != null) {
            return SetupCopyResult(reason = SetupCopyReason.DATE_REQUIRED)
        }
        val sameIconCatalog = (sourceFamily.platform != null) == (targetFamily.platform != null)
        val iconSkipped = iconId != null && (!sameIconCatalog || !iconAvailable)
        if (iconSkipped && !hasNonIconInputs) {
            return SetupCopyResult(reason = SetupCopyReason.ICON_UNAVAILABLE)
        }
        return SetupCopyResult(
            state =
                SampleState(
                    initialValue = value ?: component.initialValue,
                    initialText = text ?: "",
                    initialIcon = if (iconSkipped) targetIconId else iconId ?: "",
                    initialRangeEnd = rangeEnd ?: 80,
                    initialDateUtcMillis = dateUtcMillis ?: SampleDates.INITIAL_UTC_MILLIS,
                    initialTimeMinutes = timeMinutes ?: SampleTimes.INITIAL_MINUTES,
                    initialTime24Hour = time24Hour ?: true,
                    initialContainerClickable = containerClickable ?: true,
                    initialInlineDateUtcMillis =
                        if (inlineDate != null) inlineDate.utcMillis
                        else SampleDates.INITIAL_UTC_MILLIS,
                    initialDateRangeStartUtcMillis = dateRange?.startUtcMillis,
                    initialDateRangeEndUtcMillis = dateRange?.endUtcMillis,
                    initialChronometerBaseMillis = chronometerBaseMillis ?: 0,
                    initialDateDisplayedMonthUtcMillis =
                        SampleDates.monthUtcMillis(
                            inlineDate?.utcMillis
                                ?: dateRange?.startUtcMillis
                                ?: SampleDates.INITIAL_UTC_MILLIS
                        ),
                ),
            iconSkipped = iconSkipped,
        )
    }

    internal fun savedValues(): Map<String, Any> = buildMap {
        put("component", component.name)
        put("family", sourceFamily.name)
        value?.let { put("value", it) }
        text?.let { put("text", it) }
        iconId?.let { put("icon", it) }
        rangeEnd?.let { put("rangeEnd", it) }
        dateUtcMillis?.let { put("date", it) }
        timeMinutes?.let { put("time", it) }
        time24Hour?.let { put("time24Hour", it) }
        containerClickable?.let { put("containerClickable", it) }
        // A present empty selection is an input, not the absence of an eligible field.
        inlineDate?.let {
            put("inlineDatePresent", true)
            it.utcMillis?.let { date -> put("inlineDate", date) }
        }
        dateRange?.let {
            put("dateRangePresent", true)
            it.startUtcMillis?.let { date -> put("dateRangeStart", date) }
            it.endUtcMillis?.let { date -> put("dateRangeEnd", date) }
        }
        chronometerBaseMillis?.let { put("chronometerBase", it) }
    }

    companion object {
        fun capture(
            component: LabComponent,
            family: DesignFamily,
            state: SampleState,
            runtimeApi: Int,
            displayedIconId: String? = null,
        ): SampleSetup {
            val supported = family.unsupportedReason(component, runtimeApi) == null
            val value = if (supported && component.hasCopiedValue) state.value else null
            return SampleSetup(
                component = component,
                sourceFamily = family,
                value = value,
                text = if (supported && component.hasCopiedText) state.text else null,
                iconId = if (supported && component.usesIcon) displayedIconId else null,
                rangeEnd =
                    if (supported && component == LabComponent.RANGE_SLIDER) state.rangeEnd
                    else null,
                dateUtcMillis =
                    if (supported && component == LabComponent.DATE_PICKER_DIALOG)
                        state.dateUtcMillis
                    else null,
                timeMinutes =
                    if (
                        supported &&
                            (component == LabComponent.TIME_PICKER_DIALOG || component.isInlineTime)
                    )
                        state.timeMinutes
                    else null,
                time24Hour =
                    if (
                        supported &&
                            (component == LabComponent.TIME_PICKER_DIALOG ||
                                component.isInlineTime ||
                                component == LabComponent.TEXT_CLOCK)
                    )
                        state.time24Hour
                    else null,
                containerClickable =
                    if (supported && component.isContainer) state.containerClickable else null,
                inlineDate =
                    if (
                        supported &&
                            component in
                                listOf(LabComponent.DATE_PICKER, LabComponent.CALENDAR_VIEW)
                    )
                        InlineDateInput(state.inlineDateUtcMillis)
                    else null,
                dateRange =
                    if (supported && component == LabComponent.DATE_RANGE_PICKER)
                        DateRangeInput(state.dateRangeStartUtcMillis, state.dateRangeEndUtcMillis)
                    else null,
                chronometerBaseMillis =
                    if (supported && component == LabComponent.CHRONOMETER)
                        state.chronometerBaseMillis
                    else null,
            )
        }

        internal fun restore(values: Map<String, Any?>): SampleSetup? {
            val component =
                LabComponent.entries.firstOrNull { it.name == values["component"] } ?: return null
            val family =
                DesignFamily.entries.firstOrNull { it.name == values["family"] } ?: return null
            // Reapply the whitelist so saved data cannot introduce secure text or action history.
            return capture(
                component,
                family,
                SampleState(
                    initialValue = values["value"] as? Int ?: component.initialValue,
                    initialText = values["text"] as? String ?: "",
                    initialRangeEnd = values["rangeEnd"] as? Int ?: 80,
                    initialDateUtcMillis =
                        values["date"] as? Long ?: SampleDates.INITIAL_UTC_MILLIS,
                    initialTimeMinutes = values["time"] as? Int ?: SampleTimes.INITIAL_MINUTES,
                    initialTime24Hour = values["time24Hour"] as? Boolean ?: true,
                    initialContainerClickable = values["containerClickable"] as? Boolean ?: true,
                    initialInlineDateUtcMillis =
                        if (values["inlineDatePresent"] == true) values["inlineDate"] as? Long
                        else SampleDates.INITIAL_UTC_MILLIS,
                    initialDateRangeStartUtcMillis =
                        if (values["dateRangePresent"] == true) values["dateRangeStart"] as? Long
                        else null,
                    initialDateRangeEndUtcMillis =
                        if (values["dateRangePresent"] == true) values["dateRangeEnd"] as? Long
                        else null,
                    initialChronometerBaseMillis = values["chronometerBase"] as? Long ?: 0,
                ),
                Int.MAX_VALUE,
                values["icon"] as? String,
            )
        }
    }
}

data class InlineDateInput(val utcMillis: Long?)

data class DateRangeInput(val startUtcMillis: Long?, val endUtcMillis: Long?)

enum class SetupCopyReason {
    SOURCE_UNSUPPORTED,
    TARGET_UNSUPPORTED,
    NO_INPUTS,
    ICON_UNAVAILABLE,
    DATE_REQUIRED,
}

data class SetupCopyResult(
    val state: SampleState? = null,
    val reason: SetupCopyReason? = null,
    val iconSkipped: Boolean = false,
)

class ComparisonEntry(val setup: SampleSetup, val enabled: Boolean) {
    companion object {
        val Saver =
            mapSaver<ComparisonEntry?>(
                save = { entry ->
                    entry?.let { it.setup.savedValues() + ("enabled" to it.enabled) } ?: emptyMap()
                },
                restore = { values ->
                    SampleSetup.restore(values)?.let {
                        ComparisonEntry(it, values["enabled"] as? Boolean ?: true)
                    }
                },
            )
    }
}

private val LabComponent.hasCopiedText: Boolean
    get() =
        this in
            listOf(
                LabComponent.TEXT_FIELD,
                LabComponent.OUTLINED_TEXT_FIELD,
                LabComponent.AUTOCOMPLETE,
                LabComponent.MULTI_AUTOCOMPLETE,
                LabComponent.SEARCH_VIEW,
                LabComponent.DIALER_FILTER,
            )

private val LabComponent.hasCopiedValue: Boolean
    get() =
        isIconToggle ||
            isDeterminateProgress ||
            isCountedBadge ||
            isViewSwitcher ||
            isAdapterList ||
            isZoomControl ||
            isAdapterAnimator ||
            isTransientWindow ||
            this == LabComponent.TAB_HOST ||
            this == LabComponent.GALLERY ||
            this == LabComponent.SLIDING_DRAWER ||
            isPopupWindow ||
            isMenuHost ||
            this in
                listOf(
                    LabComponent.CHECKBOX,
                    LabComponent.CHECKED_TEXT_VIEW,
                    LabComponent.SWITCH,
                    LabComponent.TOGGLE_BUTTON,
                    LabComponent.RADIO,
                    LabComponent.TRI_STATE_CHECKBOX,
                    LabComponent.FILTER_CHIP,
                    LabComponent.ELEVATED_FILTER_CHIP,
                    LabComponent.INPUT_CHIP,
                    LabComponent.SINGLE_SEGMENTED,
                    LabComponent.MULTI_SEGMENTED,
                    LabComponent.SPINNER,
                    LabComponent.RATING,
                    LabComponent.NUMBER_PICKER,
                    LabComponent.SLIDER,
                    LabComponent.RANGE_SLIDER,
                    LabComponent.CHRONOMETER,
                    LabComponent.VIDEO_VIEW,
                    LabComponent.SHARE_ACTION_PROVIDER,
                    LabComponent.EDGE_EFFECT,
                )
