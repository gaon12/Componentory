package xyz.gaon.componentory.lab

import android.content.Context
import xyz.gaon.componentory.R

fun LabComponent.feedback(
    context: Context,
    value: Int,
    text: String,
    rangeEnd: Int = 80,
    dateUtcMillis: Long = SampleDates.INITIAL_UTC_MILLIS,
    timeMinutes: Int = SampleTimes.INITIAL_MINUTES,
    time24Hour: Boolean = true,
    chronometerBaseMillis: Long = 0,
): String {
    val empty = context.getString(R.string.sample_state_empty)
    return when {
        isDivider ||
            this == LabComponent.DOT_BADGE ||
            this == LabComponent.TEXT ||
            this == LabComponent.ANALOG_CLOCK ||
            this == LabComponent.DIGITAL_CLOCK ||
            isScrollContainer ||
            isFrameworkLayout ||
            isContentSurface ->
            context.getString(R.string.status_preview, context.getString(labelRes))
        this == LabComponent.TEXT_CLOCK ->
            context.getString(
                R.string.status_clock_format,
                context.getString(
                    if (time24Hour) R.string.clock_format_24 else R.string.clock_format_12
                ),
            )
        this == LabComponent.CHRONOMETER ->
            if (value == 1) context.getString(R.string.status_chronometer_running)
            else
                context.getString(
                    R.string.status_chronometer_stopped,
                    SampleTimes.formatElapsed(chronometerBaseMillis),
                )
        isViewSwitcher ->
            context.getString(R.string.status_switcher_child, value + 1, switcherPageCount)
        isAdapterAnimator ->
            context.getString(R.string.status_switcher_child, value + 1, adapterPageCount)
        isTransientWindow -> context.getString(R.string.status_shown_times, value)
        this == LabComponent.POPUP_WINDOW -> context.getString(R.string.status_shown_times, value)
        this == LabComponent.LIST_POPUP_WINDOW ->
            if (value == 0) context.getString(R.string.sample_state_no_selection)
            else
                context.getString(
                    R.string.status_selected,
                    context.getString(R.string.list_item, value),
                )
        this == LabComponent.VIDEO_VIEW ->
            context.getString(if (value == 1) R.string.status_playing else R.string.status_paused)
        this == LabComponent.SHARE_ACTION_PROVIDER ->
            context.getString(R.string.status_shares, value)
        this == LabComponent.EDGE_EFFECT -> context.getString(R.string.status_pulls, value)
        this == LabComponent.SNACKBAR -> context.getString(R.string.status_shown_times, value)
        isAppBar -> context.getString(R.string.status_clicks, value)
        this == LabComponent.PERMANENT_NAVIGATION_DRAWER ->
            context.getString(R.string.status_preview, context.getString(labelRes))
        isTooltip -> context.getString(R.string.status_preview, context.getString(labelRes))
        this == LabComponent.SCAFFOLD ->
            context.getString(R.string.status_preview, context.getString(labelRes))
        isToggleableDrawer ->
            context.getString(if (value == 1) R.string.drawer_opened else R.string.drawer_closed)
        isSheetSuite ->
            context.getString(if (value == 1) R.string.sheet_open else R.string.sheet_closed)
        this == LabComponent.SWIPE_TO_DISMISS ->
            context.getString(if (value == 1) R.string.dismissed else R.string.settled)
        isNavigationSuite || isTabRow ->
            context.getString(
                R.string.status_selected,
                context.getString(R.string.list_item, value),
            )
        isCarousel ->
            if (value == 0) context.getString(R.string.sample_state_no_selection)
            else
                context.getString(
                    R.string.status_selected,
                    context.getString(R.string.list_item, value),
                )
        this == LabComponent.PULL_TO_REFRESH -> context.getString(R.string.status_refreshes, value)
        this == LabComponent.MEDIA_CONTROLLER ->
            context.getString(R.string.status_preview, context.getString(labelRes))
        isMenuHost ->
            if (value == 0) context.getString(R.string.sample_state_no_selection)
            else
                context.getString(
                    R.string.status_selected,
                    context.getString(
                        when (value) {
                            1 -> R.string.option_a
                            2 -> R.string.option_b
                            else -> R.string.toolbar_nav_action
                        }
                    ),
                )
        this == LabComponent.TAB_HOST -> context.getString(R.string.status_tab, value + 1, tabCount)
        this == LabComponent.GALLERY ->
            context.getString(R.string.status_gallery, value + 1, galleryItemCount)
        this == LabComponent.SLIDING_DRAWER ->
            context.getString(if (value == 1) R.string.drawer_opened else R.string.drawer_closed)
        this == LabComponent.TWO_LINE_LIST_ITEM || this == LabComponent.LIST_ITEM ->
            context.getString(R.string.status_preview, context.getString(labelRes))
        this == LabComponent.LIST_VIEW || this == LabComponent.GRID_VIEW ->
            if (value == 0) context.getString(R.string.sample_state_no_selection)
            else
                context.getString(
                    R.string.status_selected,
                    context.getString(R.string.list_item, value),
                )
        this == LabComponent.EXPANDABLE_LIST_VIEW ->
            context.getString(
                R.string.status_expanded,
                (0 until listRowCount)
                    .filter { value and (1 shl it) != 0 }
                    .joinToString(", ") { context.getString(R.string.list_group, it + 1) }
                    .ifEmpty { context.getString(R.string.sample_state_none) },
            )
        isZoomControl -> context.getString(R.string.status_zoom, value, zoomLevelMax)
        isCountedBadge -> context.getString(R.string.status_badge, value)
        isIndeterminateProgress -> context.getString(R.string.status_indeterminate_progress)
        this == LabComponent.RANGE_SLIDER ->
            context.getString(R.string.status_range, value, rangeEnd)
        isSecureInput -> context.getString(R.string.status_characters, value)
        this == LabComponent.SPINNER ->
            context.getString(R.string.status_selected, listOf("Alpha", "Beta", "Gamma")[value])
        this == LabComponent.SEARCH_VIEW ->
            context.getString(R.string.status_search, text.ifEmpty { empty }, value)
        this in
            listOf(
                LabComponent.TEXT_FIELD,
                LabComponent.OUTLINED_TEXT_FIELD,
                LabComponent.AUTOCOMPLETE,
                LabComponent.MULTI_AUTOCOMPLETE,
                LabComponent.DIALER_FILTER,
                LabComponent.EXPOSED_DROPDOWN,
                LabComponent.SEARCH_BAR,
                LabComponent.DOCKED_SEARCH_BAR,
                LabComponent.TOP_SEARCH_BAR,
                LabComponent.EXPANDED_DOCKED_SEARCH_BAR,
            ) -> context.getString(R.string.status_text, text.ifEmpty { empty })
        this == LabComponent.TRI_STATE_CHECKBOX ->
            context.getString(
                when (value) {
                    1 -> R.string.sample_state_checked
                    2 -> R.string.sample_state_indeterminate
                    else -> R.string.sample_state_unchecked
                }
            )
        this in
            listOf(
                LabComponent.FILTER_CHIP,
                LabComponent.ELEVATED_FILTER_CHIP,
                LabComponent.INPUT_CHIP,
            ) ->
            context.getString(
                if (value == 1) R.string.sample_state_selected
                else R.string.sample_state_not_selected
            )
        this == LabComponent.SINGLE_SEGMENTED ->
            if (value == 0) context.getString(R.string.sample_state_no_selection)
            else context.getString(R.string.status_selected, ('A'.code + value - 1).toChar())
        this == LabComponent.MULTI_SEGMENTED ->
            context.getString(
                R.string.status_selected,
                (0..2)
                    .filter { value and (1 shl it) != 0 }
                    .joinToString(", ") { ('A'.code + it).toChar().toString() }
                    .ifEmpty { context.getString(R.string.sample_state_none) },
            )
        isIconToggle || this == LabComponent.SWITCH || this == LabComponent.TOGGLE_BUTTON ->
            context.getString(
                if (value == 1) R.string.sample_state_on else R.string.sample_state_off
            )
        this == LabComponent.CHECKBOX || this == LabComponent.CHECKED_TEXT_VIEW ->
            context.getString(
                if (value == 1) R.string.sample_state_checked else R.string.sample_state_unchecked
            )
        this == LabComponent.RADIO ->
            if (value == 0) context.getString(R.string.sample_state_no_selection)
            else
                context.getString(
                    R.string.status_selected,
                    context.getString(if (value == 1) R.string.option_a else R.string.option_b),
                )
        this == LabComponent.POPUP_MENU ->
            context.getString(
                R.string.status_menu,
                context.getString(
                    when (value) {
                        1 -> R.string.option_a
                        2 -> R.string.option_b
                        else -> R.string.sample_state_no_selection
                    }
                ),
                context.getString(
                    when (text) {
                        SampleMenuAction.OPENED.name -> R.string.action_opened
                        SampleMenuAction.SELECTED.name -> R.string.sample_state_selected
                        SampleMenuAction.DISMISSED.name -> R.string.action_dismissed
                        else -> R.string.action_not_opened
                    }
                ),
            )
        this == LabComponent.RATING -> context.getString(R.string.status_rating, value)
        this == LabComponent.NUMBER_PICKER -> context.getString(R.string.status_number, value)
        this == LabComponent.SLIDER ||
            this == LabComponent.VERTICAL_DRAG_HANDLE ||
            isDeterminateProgress -> context.getString(R.string.status_value, value)
        this == LabComponent.DATE_PICKER_DIALOG ->
            context.getString(
                R.string.status_date_action,
                SampleDates.format(dateUtcMillis, context.resources.configuration.locales[0]),
                context.getString(dialogAction(value)),
            )
        this == LabComponent.TIME_PICKER_DIALOG || isInlineTime -> {
            val locale = context.resources.configuration.locales[0]
            val pattern =
                android.text.format.DateFormat.getBestDateTimePattern(
                    locale,
                    if (time24Hour) "Hm" else "hm",
                )
            val formatted = SampleTimes.format(timeMinutes, locale, pattern)
            if (isInlineTime) context.getString(R.string.status_inline_time, formatted)
            else
                context.getString(
                    R.string.status_time_action,
                    formatted,
                    context.getString(dialogAction(value)),
                )
        }
        this == LabComponent.DIALOG || this == LabComponent.BASIC_ALERT_DIALOG ->
            context.getString(R.string.status_action, context.getString(dialogAction(value)))
        else -> context.getString(R.string.status_clicks, value)
    }
}

private fun dialogAction(value: Int): Int =
    when (value) {
        1 -> R.string.action_opened
        2 -> R.string.action_confirmed
        3 -> R.string.action_cancelled
        4 -> R.string.action_dismissed
        else -> R.string.action_not_opened
    }
