package xyz.gaon.componentory.lab

enum class LabComponent(
    val label: String,
    val description: String,
    val platformSource: String? = null,
    val material2Function: String? = null,
    val material3Function: String? = material2Function,
    val minimumApi: Int = 1,
    val initialValue: Int = 0,
    val category: ComponentCategory = ComponentCategory.ACTION,
) {
    BUTTON(
        "Button",
        "버튼을 누르고 반응을 확인하세요",
        platformSource = "android.widget.Button",
        material2Function = "Button",
        material3Function = "Button",
        minimumApi = 1,
        initialValue = 0,
    ),
    CHECKBOX(
        "Checkbox",
        "선택하거나 해제하는 체크박스",
        platformSource = "android.widget.CheckBox",
        category = ComponentCategory.SELECTION,
        material2Function = "Checkbox",
        material3Function = "Checkbox",
        minimumApi = 1,
        initialValue = 0,
    ),
    RADIO(
        "Radio buttons",
        "여러 옵션 중 하나를 선택하는 라디오 버튼",
        platformSource = "android.widget.RadioButton",
        category = ComponentCategory.SELECTION,
        material2Function = "RadioButton",
        material3Function = "RadioButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    SWITCH(
        "Switch",
        "켜고 끄는 스위치",
        platformSource = "android.widget.Switch",
        category = ComponentCategory.SELECTION,
        material2Function = "Switch",
        material3Function = "Switch",
        minimumApi = 14,
        initialValue = 0,
    ),
    TEXT_FIELD(
        "Text field",
        "텍스트를 입력하는 필드",
        platformSource = "android.widget.EditText",
        category = ComponentCategory.INPUT,
        material2Function = "TextField",
        material3Function = "TextField",
        minimumApi = 1,
        initialValue = 0,
    ),
    SLIDER(
        "Slider",
        "드래그해서 값을 조절하는 슬라이더",
        platformSource = "android.widget.SeekBar",
        category = ComponentCategory.INPUT,
        material2Function = "Slider",
        material3Function = "Slider",
        minimumApi = 1,
        initialValue = 50,
    ),
    PROGRESS(
        "Progress",
        "진행 정도를 표시하는 인디케이터",
        platformSource = "android.widget.ProgressBar",
        category = ComponentCategory.INDICATOR,
        material2Function = "LinearProgressIndicator",
        material3Function = "LinearProgressIndicator",
        minimumApi = 1,
        initialValue = 50,
    ),
    DIALOG(
        "Dialog",
        "확인과 취소를 선택하는 대화상자",
        platformSource = "android.app.AlertDialog",
        category = ComponentCategory.FEEDBACK,
        material2Function = "AlertDialog",
        material3Function = "AlertDialog",
        minimumApi = 1,
        initialValue = 0,
    ),
    TOGGLE_BUTTON(
        "Toggle button",
        "켜짐과 꺼짐을 선택하는 토글 버튼",
        platformSource = "android.widget.ToggleButton",
        category = ComponentCategory.SELECTION,
        material2Function = null,
        material3Function = null,
        minimumApi = 1,
        initialValue = 0,
    ),
    IMAGE_BUTTON(
        "Image button",
        "아이콘을 누르는 이미지 버튼",
        platformSource = "android.widget.ImageButton",
        material2Function = null,
        material3Function = null,
        minimumApi = 1,
        initialValue = 0,
    ),
    RATING(
        "Rating bar",
        "별을 눌러 점수를 선택하는 별점",
        platformSource = "android.widget.RatingBar",
        category = ComponentCategory.PICKER,
        material2Function = null,
        material3Function = null,
        minimumApi = 1,
        initialValue = 0,
    ),
    NUMBER_PICKER(
        "Number picker",
        "숫자를 스크롤하거나 버튼으로 선택하는 피커",
        platformSource = "android.widget.NumberPicker",
        category = ComponentCategory.PICKER,
        material2Function = null,
        material3Function = null,
        minimumApi = 11,
        initialValue = 5,
    ),
    OUTLINED_BUTTON(
        "Outlined button",
        "버튼을 누르고 디자인과 반응을 비교하세요",
        platformSource = null,
        material2Function = "OutlinedButton",
        material3Function = "OutlinedButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    TEXT_BUTTON(
        "Text button",
        "버튼을 누르고 디자인과 반응을 비교하세요",
        platformSource = null,
        material2Function = "TextButton",
        material3Function = "TextButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    ELEVATED_BUTTON(
        "Elevated button",
        "버튼을 누르고 디자인과 반응을 비교하세요",
        platformSource = null,
        material2Function = null,
        material3Function = "ElevatedButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    TONAL_BUTTON(
        "Tonal button",
        "버튼을 누르고 디자인과 반응을 비교하세요",
        platformSource = null,
        material2Function = null,
        material3Function = "FilledTonalButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    ICON_BUTTON(
        "Icon button",
        "버튼을 누르고 디자인과 반응을 비교하세요",
        platformSource = null,
        material2Function = "IconButton",
        material3Function = "IconButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    ICON_TOGGLE(
        "Icon toggle button",
        "아이콘을 눌러 선택 상태를 전환하세요",
        platformSource = null,
        material2Function = "IconToggleButton",
        material3Function = "IconToggleButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    FILLED_ICON_BUTTON(
        "Filled icon button",
        "버튼을 누르고 디자인과 반응을 비교하세요",
        platformSource = null,
        material2Function = null,
        material3Function = "FilledIconButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    FILLED_ICON_TOGGLE(
        "Filled icon toggle",
        "아이콘을 눌러 선택 상태를 전환하세요",
        platformSource = null,
        material2Function = null,
        material3Function = "FilledIconToggleButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    TONAL_ICON_BUTTON(
        "Tonal icon button",
        "버튼을 누르고 디자인과 반응을 비교하세요",
        platformSource = null,
        material2Function = null,
        material3Function = "FilledTonalIconButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    TONAL_ICON_TOGGLE(
        "Tonal icon toggle",
        "아이콘을 눌러 선택 상태를 전환하세요",
        platformSource = null,
        material2Function = null,
        material3Function = "FilledTonalIconToggleButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    OUTLINED_ICON_BUTTON(
        "Outlined icon button",
        "버튼을 누르고 디자인과 반응을 비교하세요",
        platformSource = null,
        material2Function = null,
        material3Function = "OutlinedIconButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    OUTLINED_ICON_TOGGLE(
        "Outlined icon toggle",
        "아이콘을 눌러 선택 상태를 전환하세요",
        platformSource = null,
        material2Function = null,
        material3Function = "OutlinedIconToggleButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    FAB(
        "Floating action button",
        "버튼을 누르고 디자인과 반응을 비교하세요",
        platformSource = null,
        material2Function = "FloatingActionButton",
        material3Function = "FloatingActionButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    EXTENDED_FAB(
        "Extended floating action button",
        "버튼을 누르고 디자인과 반응을 비교하세요",
        platformSource = null,
        material2Function = "ExtendedFloatingActionButton",
        material3Function = "ExtendedFloatingActionButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    SMALL_FAB(
        "Small floating action button",
        "버튼을 누르고 디자인과 반응을 비교하세요",
        platformSource = null,
        material2Function = null,
        material3Function = "SmallFloatingActionButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    LARGE_FAB(
        "Large floating action button",
        "버튼을 누르고 디자인과 반응을 비교하세요",
        platformSource = null,
        material2Function = null,
        material3Function = "LargeFloatingActionButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    CHIP(
        "Chip",
        "누르면 동작하는 Material 2 칩",
        material2Function = "Chip",
        material3Function = null,
        category = ComponentCategory.SELECTION,
    ),
    ASSIST_CHIP(
        "Assist chip",
        "관련 동작을 실행하는 칩",
        material2Function = null,
        material3Function = "AssistChip",
        category = ComponentCategory.SELECTION,
    ),
    ELEVATED_ASSIST_CHIP(
        "Elevated assist chip",
        "그림자가 있는 동작 칩",
        material2Function = null,
        material3Function = "ElevatedAssistChip",
        category = ComponentCategory.SELECTION,
    ),
    FILTER_CHIP(
        "Filter chip",
        "필터를 선택하거나 해제하는 칩",
        material2Function = "FilterChip",
        material3Function = "FilterChip",
        category = ComponentCategory.SELECTION,
    ),
    ELEVATED_FILTER_CHIP(
        "Elevated filter chip",
        "그림자가 있는 선택 칩",
        material2Function = null,
        material3Function = "ElevatedFilterChip",
        category = ComponentCategory.SELECTION,
    ),
    INPUT_CHIP(
        "Input chip",
        "선택한 입력 항목을 표시하는 칩",
        material2Function = null,
        material3Function = "InputChip",
        category = ComponentCategory.SELECTION,
    ),
    SUGGESTION_CHIP(
        "Suggestion chip",
        "추천 항목을 실행하는 칩",
        material2Function = null,
        material3Function = "SuggestionChip",
        category = ComponentCategory.SELECTION,
    ),
    ELEVATED_SUGGESTION_CHIP(
        "Elevated suggestion chip",
        "그림자가 있는 추천 칩",
        material2Function = null,
        material3Function = "ElevatedSuggestionChip",
        category = ComponentCategory.SELECTION,
    ),
    TRI_STATE_CHECKBOX(
        "Tri-state checkbox",
        "선택·해제·일부 선택의 세 상태 체크박스",
        material2Function = "TriStateCheckbox",
        material3Function = "TriStateCheckbox",
        category = ComponentCategory.SELECTION,
    ),
    SINGLE_SEGMENTED(
        "Single-choice segmented buttons",
        "분할된 버튼 중 하나를 선택하세요",
        material2Function = null,
        material3Function = "SingleChoiceSegmentedButtonRow",
        category = ComponentCategory.SELECTION,
    ),
    MULTI_SEGMENTED(
        "Multi-choice segmented buttons",
        "분할된 버튼을 여러 개 선택하세요",
        material2Function = null,
        material3Function = "MultiChoiceSegmentedButtonRow",
        category = ComponentCategory.SELECTION,
    ),
    OUTLINED_TEXT_FIELD(
        "Outlined text field",
        "외곽선이 있는 텍스트 입력창",
        material2Function = "OutlinedTextField",
        category = ComponentCategory.INPUT,
    ),
    SECURE_TEXT_FIELD(
        "Secure text field",
        "입력한 문자를 가리는 보안 입력창",
        material2Function = "SecureTextField",
        category = ComponentCategory.INPUT,
    ),
    OUTLINED_SECURE_TEXT_FIELD(
        "Outlined secure text field",
        "외곽선이 있는 보안 입력창",
        material2Function = "OutlinedSecureTextField",
        category = ComponentCategory.INPUT,
    ),
    AUTOCOMPLETE(
        "Autocomplete",
        "입력한 글자로 추천 항목을 찾고 선택하세요",
        platformSource = "android.widget.AutoCompleteTextView",
        category = ComponentCategory.INPUT,
    ),
    MULTI_AUTOCOMPLETE(
        "Multi autocomplete",
        "쉼표로 구분한 여러 항목을 자동완성하세요",
        platformSource = "android.widget.MultiAutoCompleteTextView",
        category = ComponentCategory.INPUT,
    ),
    SPINNER(
        "Spinner",
        "펼쳐지는 목록에서 항목을 선택하세요",
        platformSource = "android.widget.Spinner",
        category = ComponentCategory.INPUT,
    ),
    ICON(
        "Icon",
        "공급원에서 제공하는 모든 아이콘을 찾아 선택하세요",
        platformSource = "android.widget.ImageView",
        material2Function = "Icon",
        category = ComponentCategory.CONTENT,
    ),
    SEARCH_VIEW(
        "Search view",
        "검색어를 입력하고 검색·지우기 동작을 사용하세요",
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

    val usesIcon: Boolean
        get() =
            this == ICON ||
                this == IMAGE_BUTTON ||
                isIconToggle ||
                isFloatingAction ||
                this in
                    listOf(ICON_BUTTON, FILLED_ICON_BUTTON, TONAL_ICON_BUTTON, OUTLINED_ICON_BUTTON)

    fun matchesSearch(query: String): Boolean {
        val term = query.trim()
        val names =
            listOfNotNull(
                label,
                description,
                platformSource,
                material2Function?.let { "androidx.compose.material.$it" },
                material3Function?.let { "androidx.compose.material3.$it" },
            )
        return term.isEmpty() || names.any { it.contains(term, ignoreCase = true) }
    }

    fun feedback(value: Int, text: String): String =
        when {
            isSecureInput -> "Characters: $value"
            this == SPINNER -> "Selected: ${listOf("Alpha", "Beta", "Gamma")[value]}"
            this == SEARCH_VIEW -> "Query: ${text.ifEmpty { "empty" }} · Searches: $value"
            this in listOf(TEXT_FIELD, OUTLINED_TEXT_FIELD, AUTOCOMPLETE, MULTI_AUTOCOMPLETE) ->
                if (text.isEmpty()) "Text: empty" else "Text: $text"
            this == TRI_STATE_CHECKBOX ->
                when (value) {
                    1 -> "Checked"
                    2 -> "Indeterminate"
                    else -> "Unchecked"
                }
            this in listOf(FILTER_CHIP, ELEVATED_FILTER_CHIP, INPUT_CHIP) ->
                if (value == 1) "Selected" else "Not selected"
            this == SINGLE_SEGMENTED ->
                if (value == 0) "No selection" else "Selected: ${('A'.code + value - 1).toChar()}"
            this == MULTI_SEGMENTED ->
                "Selected: " +
                    (0..2)
                        .filter { value and (1 shl it) != 0 }
                        .joinToString(", ") { ('A'.code + it).toChar().toString() }
                        .ifEmpty { "none" }
            isIconToggle || this == SWITCH || this == TOGGLE_BUTTON ->
                if (value == 1) "On" else "Off"
            this == CHECKBOX -> if (value == 1) "Checked" else "Unchecked"
            this == RADIO ->
                when (value) {
                    1 -> "Selected: Option A"
                    2 -> "Selected: Option B"
                    else -> "No selection"
                }
            this == RATING -> "Rating: $value / 5"
            this == NUMBER_PICKER -> "Number: $value / 10"
            this == SLIDER || this == PROGRESS -> "Value: $value / 100"
            this == DIALOG ->
                "Last action: ${when (value) { 1 -> "Opened"
 2 -> "Confirmed"
 3 -> "Cancelled"
 4 -> "Dismissed"
 else -> "Not opened" }}"
            else -> "Clicks: $value"
        }
}
