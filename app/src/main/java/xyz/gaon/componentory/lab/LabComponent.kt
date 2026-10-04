package xyz.gaon.componentory.lab

import xyz.gaon.componentory.R

enum class LabComponent(
    val label: String,
    val labelRes: Int,
    val descriptionRes: Int,
    val platformSource: String? = null,
    val material2Function: String? = null,
    val material3Function: String? = material2Function,
    val minimumApi: Int = 1,
    val initialValue: Int = 0,
    val category: ComponentCategory = ComponentCategory.ACTION,
) {
    BUTTON(
        "Button",
        R.string.component_button,
        R.string.component_button_description,
        platformSource = "android.widget.Button",
        material2Function = "Button",
        material3Function = "Button",
        minimumApi = 1,
        initialValue = 0,
    ),
    CHECKBOX(
        "Checkbox",
        R.string.component_checkbox,
        R.string.component_checkbox_description,
        platformSource = "android.widget.CheckBox",
        category = ComponentCategory.SELECTION,
        material2Function = "Checkbox",
        material3Function = "Checkbox",
        minimumApi = 1,
        initialValue = 0,
    ),
    RADIO(
        "Radio buttons",
        R.string.component_radio,
        R.string.component_radio_description,
        platformSource = "android.widget.RadioButton",
        category = ComponentCategory.SELECTION,
        material2Function = "RadioButton",
        material3Function = "RadioButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    SWITCH(
        "Switch",
        R.string.component_switch,
        R.string.component_switch_description,
        platformSource = "android.widget.Switch",
        category = ComponentCategory.SELECTION,
        material2Function = "Switch",
        material3Function = "Switch",
        minimumApi = 14,
        initialValue = 0,
    ),
    TEXT_FIELD(
        "Text field",
        R.string.component_text_field,
        R.string.component_text_field_description,
        platformSource = "android.widget.EditText",
        category = ComponentCategory.INPUT,
        material2Function = "TextField",
        material3Function = "TextField",
        minimumApi = 1,
        initialValue = 0,
    ),
    SLIDER(
        "Slider",
        R.string.component_slider,
        R.string.component_slider_description,
        platformSource = "android.widget.SeekBar",
        category = ComponentCategory.INPUT,
        material2Function = "Slider",
        material3Function = "Slider",
        minimumApi = 1,
        initialValue = 50,
    ),
    RANGE_SLIDER(
        "Range slider",
        R.string.component_range_slider,
        R.string.component_range_slider_description,
        material2Function = "RangeSlider",
        initialValue = 20,
        category = ComponentCategory.INPUT,
    ),
    PROGRESS(
        "Progress",
        R.string.component_progress,
        R.string.component_progress_description,
        platformSource = "android.widget.ProgressBar",
        category = ComponentCategory.INDICATOR,
        material2Function = "LinearProgressIndicator",
        material3Function = "LinearProgressIndicator",
        minimumApi = 1,
        initialValue = 50,
    ),
    CIRCULAR_PROGRESS(
        "Circular progress (determinate)",
        R.string.component_circular_progress,
        R.string.component_circular_progress_description,
        material2Function = "CircularProgressIndicator",
        initialValue = 50,
        category = ComponentCategory.INDICATOR,
    ),
    INDETERMINATE_LINEAR_PROGRESS(
        "Linear progress (indeterminate)",
        R.string.component_indeterminate_linear_progress,
        R.string.component_indeterminate_linear_progress_description,
        platformSource = "android.widget.ProgressBar",
        material2Function = "LinearProgressIndicator",
        category = ComponentCategory.INDICATOR,
    ),
    INDETERMINATE_CIRCULAR_PROGRESS(
        "Circular progress (indeterminate)",
        R.string.component_indeterminate_circular_progress,
        R.string.component_indeterminate_circular_progress_description,
        platformSource = "android.widget.ProgressBar",
        material2Function = "CircularProgressIndicator",
        category = ComponentCategory.INDICATOR,
    ),
    HORIZONTAL_DIVIDER(
        "Horizontal divider",
        R.string.component_horizontal_divider,
        R.string.component_horizontal_divider_description,
        material2Function = "Divider",
        material3Function = "HorizontalDivider",
        category = ComponentCategory.LAYOUT,
    ),
    VERTICAL_DIVIDER(
        "Vertical divider",
        R.string.component_vertical_divider,
        R.string.component_vertical_divider_description,
        material3Function = "VerticalDivider",
        category = ComponentCategory.LAYOUT,
    ),
    LEGACY_DIVIDER(
        "Legacy divider",
        R.string.component_legacy_divider,
        R.string.component_legacy_divider_description,
        material3Function = "Divider",
        category = ComponentCategory.LAYOUT,
    ),
    BADGE(
        "Badge (number)",
        R.string.component_badge,
        R.string.component_badge_description,
        material2Function = "Badge",
        initialValue = 7,
        category = ComponentCategory.INDICATOR,
    ),
    DOT_BADGE(
        "Dot badge",
        R.string.component_dot_badge,
        R.string.component_dot_badge_description,
        material2Function = "Badge",
        category = ComponentCategory.INDICATOR,
    ),
    BADGED_BOX(
        "Badged icon",
        R.string.component_badged_box,
        R.string.component_badged_box_description,
        material2Function = "BadgedBox",
        initialValue = 7,
        category = ComponentCategory.INDICATOR,
    ),
    DIALOG(
        "Dialog",
        R.string.component_dialog,
        R.string.component_dialog_description,
        platformSource = "android.app.AlertDialog",
        category = ComponentCategory.FEEDBACK,
        material2Function = "AlertDialog",
        material3Function = "AlertDialog",
        minimumApi = 1,
        initialValue = 0,
    ),
    TOGGLE_BUTTON(
        "Toggle button",
        R.string.component_toggle_button,
        R.string.component_toggle_button_description,
        platformSource = "android.widget.ToggleButton",
        category = ComponentCategory.SELECTION,
        material2Function = null,
        material3Function = null,
        minimumApi = 1,
        initialValue = 0,
    ),
    IMAGE_BUTTON(
        "Image button",
        R.string.component_image_button,
        R.string.component_image_button_description,
        platformSource = "android.widget.ImageButton",
        material2Function = null,
        material3Function = null,
        minimumApi = 1,
        initialValue = 0,
    ),
    RATING(
        "Rating bar",
        R.string.component_rating,
        R.string.component_rating_description,
        platformSource = "android.widget.RatingBar",
        category = ComponentCategory.PICKER,
        material2Function = null,
        material3Function = null,
        minimumApi = 1,
        initialValue = 0,
    ),
    NUMBER_PICKER(
        "Number picker",
        R.string.component_number_picker,
        R.string.component_number_picker_description,
        platformSource = "android.widget.NumberPicker",
        category = ComponentCategory.PICKER,
        material2Function = null,
        material3Function = null,
        minimumApi = 11,
        initialValue = 5,
    ),
    OUTLINED_BUTTON(
        "Outlined button",
        R.string.component_outlined_button,
        R.string.component_outlined_button_description,
        platformSource = null,
        material2Function = "OutlinedButton",
        material3Function = "OutlinedButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    TEXT_BUTTON(
        "Text button",
        R.string.component_text_button,
        R.string.component_text_button_description,
        platformSource = null,
        material2Function = "TextButton",
        material3Function = "TextButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    ELEVATED_BUTTON(
        "Elevated button",
        R.string.component_elevated_button,
        R.string.component_elevated_button_description,
        platformSource = null,
        material2Function = null,
        material3Function = "ElevatedButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    TONAL_BUTTON(
        "Tonal button",
        R.string.component_tonal_button,
        R.string.component_tonal_button_description,
        platformSource = null,
        material2Function = null,
        material3Function = "FilledTonalButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    ICON_BUTTON(
        "Icon button",
        R.string.component_icon_button,
        R.string.component_icon_button_description,
        platformSource = null,
        material2Function = "IconButton",
        material3Function = "IconButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    ICON_TOGGLE(
        "Icon toggle button",
        R.string.component_icon_toggle,
        R.string.component_icon_toggle_description,
        platformSource = null,
        material2Function = "IconToggleButton",
        material3Function = "IconToggleButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    FILLED_ICON_BUTTON(
        "Filled icon button",
        R.string.component_filled_icon_button,
        R.string.component_filled_icon_button_description,
        platformSource = null,
        material2Function = null,
        material3Function = "FilledIconButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    FILLED_ICON_TOGGLE(
        "Filled icon toggle",
        R.string.component_filled_icon_toggle,
        R.string.component_filled_icon_toggle_description,
        platformSource = null,
        material2Function = null,
        material3Function = "FilledIconToggleButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    TONAL_ICON_BUTTON(
        "Tonal icon button",
        R.string.component_tonal_icon_button,
        R.string.component_tonal_icon_button_description,
        platformSource = null,
        material2Function = null,
        material3Function = "FilledTonalIconButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    TONAL_ICON_TOGGLE(
        "Tonal icon toggle",
        R.string.component_tonal_icon_toggle,
        R.string.component_tonal_icon_toggle_description,
        platformSource = null,
        material2Function = null,
        material3Function = "FilledTonalIconToggleButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    OUTLINED_ICON_BUTTON(
        "Outlined icon button",
        R.string.component_outlined_icon_button,
        R.string.component_outlined_icon_button_description,
        platformSource = null,
        material2Function = null,
        material3Function = "OutlinedIconButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    OUTLINED_ICON_TOGGLE(
        "Outlined icon toggle",
        R.string.component_outlined_icon_toggle,
        R.string.component_outlined_icon_toggle_description,
        platformSource = null,
        material2Function = null,
        material3Function = "OutlinedIconToggleButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    FAB(
        "Floating action button",
        R.string.component_fab,
        R.string.component_fab_description,
        platformSource = null,
        material2Function = "FloatingActionButton",
        material3Function = "FloatingActionButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    EXTENDED_FAB(
        "Extended floating action button",
        R.string.component_extended_fab,
        R.string.component_extended_fab_description,
        platformSource = null,
        material2Function = "ExtendedFloatingActionButton",
        material3Function = "ExtendedFloatingActionButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    SMALL_FAB(
        "Small floating action button",
        R.string.component_small_fab,
        R.string.component_small_fab_description,
        platformSource = null,
        material2Function = null,
        material3Function = "SmallFloatingActionButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    LARGE_FAB(
        "Large floating action button",
        R.string.component_large_fab,
        R.string.component_large_fab_description,
        platformSource = null,
        material2Function = null,
        material3Function = "LargeFloatingActionButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    CHIP(
        "Chip",
        R.string.component_chip,
        R.string.component_chip_description,
        material2Function = "Chip",
        material3Function = null,
        category = ComponentCategory.SELECTION,
    ),
    ASSIST_CHIP(
        "Assist chip",
        R.string.component_assist_chip,
        R.string.component_assist_chip_description,
        material2Function = null,
        material3Function = "AssistChip",
        category = ComponentCategory.SELECTION,
    ),
    ELEVATED_ASSIST_CHIP(
        "Elevated assist chip",
        R.string.component_elevated_assist_chip,
        R.string.component_elevated_assist_chip_description,
        material2Function = null,
        material3Function = "ElevatedAssistChip",
        category = ComponentCategory.SELECTION,
    ),
    FILTER_CHIP(
        "Filter chip",
        R.string.component_filter_chip,
        R.string.component_filter_chip_description,
        material2Function = "FilterChip",
        material3Function = "FilterChip",
        category = ComponentCategory.SELECTION,
    ),
    ELEVATED_FILTER_CHIP(
        "Elevated filter chip",
        R.string.component_elevated_filter_chip,
        R.string.component_elevated_filter_chip_description,
        material2Function = null,
        material3Function = "ElevatedFilterChip",
        category = ComponentCategory.SELECTION,
    ),
    INPUT_CHIP(
        "Input chip",
        R.string.component_input_chip,
        R.string.component_input_chip_description,
        material2Function = null,
        material3Function = "InputChip",
        category = ComponentCategory.SELECTION,
    ),
    SUGGESTION_CHIP(
        "Suggestion chip",
        R.string.component_suggestion_chip,
        R.string.component_suggestion_chip_description,
        material2Function = null,
        material3Function = "SuggestionChip",
        category = ComponentCategory.SELECTION,
    ),
    ELEVATED_SUGGESTION_CHIP(
        "Elevated suggestion chip",
        R.string.component_elevated_suggestion_chip,
        R.string.component_elevated_suggestion_chip_description,
        material2Function = null,
        material3Function = "ElevatedSuggestionChip",
        category = ComponentCategory.SELECTION,
    ),
    TRI_STATE_CHECKBOX(
        "Tri-state checkbox",
        R.string.component_tri_state_checkbox,
        R.string.component_tri_state_checkbox_description,
        material2Function = "TriStateCheckbox",
        material3Function = "TriStateCheckbox",
        category = ComponentCategory.SELECTION,
    ),
    SINGLE_SEGMENTED(
        "Single-choice segmented buttons",
        R.string.component_single_segmented,
        R.string.component_single_segmented_description,
        material2Function = null,
        material3Function = "SingleChoiceSegmentedButtonRow",
        category = ComponentCategory.SELECTION,
    ),
    MULTI_SEGMENTED(
        "Multi-choice segmented buttons",
        R.string.component_multi_segmented,
        R.string.component_multi_segmented_description,
        material2Function = null,
        material3Function = "MultiChoiceSegmentedButtonRow",
        category = ComponentCategory.SELECTION,
    ),
    OUTLINED_TEXT_FIELD(
        "Outlined text field",
        R.string.component_outlined_text_field,
        R.string.component_outlined_text_field_description,
        material2Function = "OutlinedTextField",
        category = ComponentCategory.INPUT,
    ),
    SECURE_TEXT_FIELD(
        "Secure text field",
        R.string.component_secure_text_field,
        R.string.component_secure_text_field_description,
        material2Function = "SecureTextField",
        category = ComponentCategory.INPUT,
    ),
    OUTLINED_SECURE_TEXT_FIELD(
        "Outlined secure text field",
        R.string.component_outlined_secure_text_field,
        R.string.component_outlined_secure_text_field_description,
        material2Function = "OutlinedSecureTextField",
        category = ComponentCategory.INPUT,
    ),
    AUTOCOMPLETE(
        "Autocomplete",
        R.string.component_autocomplete,
        R.string.component_autocomplete_description,
        platformSource = "android.widget.AutoCompleteTextView",
        category = ComponentCategory.INPUT,
    ),
    MULTI_AUTOCOMPLETE(
        "Multi autocomplete",
        R.string.component_multi_autocomplete,
        R.string.component_multi_autocomplete_description,
        platformSource = "android.widget.MultiAutoCompleteTextView",
        category = ComponentCategory.INPUT,
    ),
    SPINNER(
        "Spinner",
        R.string.component_spinner,
        R.string.component_spinner_description,
        platformSource = "android.widget.Spinner",
        category = ComponentCategory.INPUT,
    ),
    ICON(
        "Icon",
        R.string.component_icon,
        R.string.component_icon_description,
        platformSource = "android.widget.ImageView",
        material2Function = "Icon",
        category = ComponentCategory.CONTENT,
    ),
    SEARCH_VIEW(
        "Search view",
        R.string.component_search_view,
        R.string.component_search_view_description,
        platformSource = "android.widget.SearchView",
        minimumApi = 11,
        category = ComponentCategory.INPUT,
    );

    val source: String
        get() =
            platformSource
                ?: material2Function?.let { "androidx.compose.material.$it" }
                ?: "androidx.compose.material3.${requireNotNull(material3Function)}"

    val isIconToggle: Boolean
        get() =
            this in listOf(ICON_TOGGLE, FILLED_ICON_TOGGLE, TONAL_ICON_TOGGLE, OUTLINED_ICON_TOGGLE)

    val isFloatingAction: Boolean
        get() = this in listOf(FAB, EXTENDED_FAB, SMALL_FAB, LARGE_FAB)

    val isSecureInput: Boolean
        get() = this == SECURE_TEXT_FIELD || this == OUTLINED_SECURE_TEXT_FIELD

    val isDeterminateProgress: Boolean
        get() = this == PROGRESS || this == CIRCULAR_PROGRESS

    val isDivider: Boolean
        get() = this in listOf(HORIZONTAL_DIVIDER, VERTICAL_DIVIDER, LEGACY_DIVIDER)

    val isIndeterminateProgress: Boolean
        get() = this == INDETERMINATE_LINEAR_PROGRESS || this == INDETERMINATE_CIRCULAR_PROGRESS

    val isBadge: Boolean
        get() = this in listOf(BADGE, DOT_BADGE, BADGED_BOX)

    val isCountedBadge: Boolean
        get() = this == BADGE || this == BADGED_BOX

    val usesIcon: Boolean
        get() =
            this == ICON ||
                this == BADGED_BOX ||
                this == IMAGE_BUTTON ||
                isIconToggle ||
                isFloatingAction ||
                this in
                    listOf(ICON_BUTTON, FILLED_ICON_BUTTON, TONAL_ICON_BUTTON, OUTLINED_ICON_BUTTON)

    fun matchesSearch(query: String, context: android.content.Context? = null): Boolean {
        val term = query.trim()
        val names =
            listOfNotNull(
                label,
                context?.getString(labelRes),
                context?.getString(descriptionRes),
                platformSource,
                material2Function?.let { "androidx.compose.material.$it" },
                material3Function?.let { "androidx.compose.material3.$it" },
            )
        return term.isEmpty() || names.any { it.contains(term, ignoreCase = true) }
    }

    fun feedback(
        context: android.content.Context,
        value: Int,
        text: String,
        rangeEnd: Int = 80,
    ): String {
        val empty = context.getString(R.string.sample_state_empty)
        return when {
            isDivider || this == DOT_BADGE ->
                context.getString(R.string.status_preview, context.getString(labelRes))
            isCountedBadge -> context.getString(R.string.status_badge, value)
            isIndeterminateProgress -> context.getString(R.string.status_indeterminate_progress)
            this == RANGE_SLIDER -> context.getString(R.string.status_range, value, rangeEnd)
            isSecureInput -> context.getString(R.string.status_characters, value)
            this == SPINNER ->
                context.getString(R.string.status_selected, listOf("Alpha", "Beta", "Gamma")[value])
            this == SEARCH_VIEW ->
                context.getString(R.string.status_search, text.ifEmpty { empty }, value)
            this in listOf(TEXT_FIELD, OUTLINED_TEXT_FIELD, AUTOCOMPLETE, MULTI_AUTOCOMPLETE) ->
                context.getString(R.string.status_text, text.ifEmpty { empty })
            this == TRI_STATE_CHECKBOX ->
                context.getString(
                    when (value) {
                        1 -> R.string.sample_state_checked
                        2 -> R.string.sample_state_indeterminate
                        else -> R.string.sample_state_unchecked
                    }
                )
            this in listOf(FILTER_CHIP, ELEVATED_FILTER_CHIP, INPUT_CHIP) ->
                context.getString(
                    if (value == 1) R.string.sample_state_selected
                    else R.string.sample_state_not_selected
                )
            this == SINGLE_SEGMENTED ->
                if (value == 0) context.getString(R.string.sample_state_no_selection)
                else context.getString(R.string.status_selected, ('A'.code + value - 1).toChar())
            this == MULTI_SEGMENTED ->
                context.getString(
                    R.string.status_selected,
                    (0..2)
                        .filter { value and (1 shl it) != 0 }
                        .joinToString(", ") { ('A'.code + it).toChar().toString() }
                        .ifEmpty { context.getString(R.string.sample_state_none) },
                )
            isIconToggle || this == SWITCH || this == TOGGLE_BUTTON ->
                context.getString(
                    if (value == 1) R.string.sample_state_on else R.string.sample_state_off
                )
            this == CHECKBOX ->
                context.getString(
                    if (value == 1) R.string.sample_state_checked
                    else R.string.sample_state_unchecked
                )
            this == RADIO ->
                if (value == 0) context.getString(R.string.sample_state_no_selection)
                else
                    context.getString(
                        R.string.status_selected,
                        context.getString(if (value == 1) R.string.option_a else R.string.option_b),
                    )
            this == RATING -> context.getString(R.string.status_rating, value)
            this == NUMBER_PICKER -> context.getString(R.string.status_number, value)
            this == SLIDER || isDeterminateProgress ->
                context.getString(R.string.status_value, value)
            this == DIALOG ->
                context.getString(
                    R.string.status_action,
                    context.getString(
                        when (value) {
                            1 -> R.string.action_opened
                            2 -> R.string.action_confirmed
                            3 -> R.string.action_cancelled
                            4 -> R.string.action_dismissed
                            else -> R.string.action_not_opened
                        }
                    ),
                )
            else -> context.getString(R.string.status_clicks, value)
        }
    }
}
