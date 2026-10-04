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
) {
    private val hasNonIconInputs: Boolean
        get() =
            value != null ||
                text != null ||
                dateUtcMillis != null ||
                timeMinutes != null ||
                containerClickable != null

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
                    if (supported && component == LabComponent.TIME_PICKER_DIALOG) state.timeMinutes
                    else null,
                time24Hour =
                    if (supported && component == LabComponent.TIME_PICKER_DIALOG) state.time24Hour
                    else null,
                containerClickable =
                    if (supported && component.isContainer) state.containerClickable else null,
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
                ),
                Int.MAX_VALUE,
                values["icon"] as? String,
            )
        }
    }
}

enum class SetupCopyReason {
    SOURCE_UNSUPPORTED,
    TARGET_UNSUPPORTED,
    NO_INPUTS,
    ICON_UNAVAILABLE,
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
            )

private val LabComponent.hasCopiedValue: Boolean
    get() =
        isIconToggle ||
            isDeterminateProgress ||
            isCountedBadge ||
            this in
                listOf(
                    LabComponent.CHECKBOX,
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
                )
