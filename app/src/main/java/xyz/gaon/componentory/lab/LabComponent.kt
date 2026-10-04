package xyz.gaon.componentory.lab

enum class LabComponent(
    val label: String,
    val description: String,
    val platformSource: String? = null,
    val material2Function: String? = null,
    val material3Function: String? = material2Function,
    val minimumApi: Int = 1,
    val initialValue: Int = 0,
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
        material2Function = "Checkbox",
        material3Function = "Checkbox",
        minimumApi = 1,
        initialValue = 0,
    ),
    RADIO(
        "Radio buttons",
        "여러 옵션 중 하나를 선택하는 라디오 버튼",
        platformSource = "android.widget.RadioButton",
        material2Function = "RadioButton",
        material3Function = "RadioButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    SWITCH(
        "Switch",
        "켜고 끄는 스위치",
        platformSource = "android.widget.Switch",
        material2Function = "Switch",
        material3Function = "Switch",
        minimumApi = 14,
        initialValue = 0,
    ),
    TEXT_FIELD(
        "Text field",
        "텍스트를 입력하는 필드",
        platformSource = "android.widget.EditText",
        material2Function = "TextField",
        material3Function = "TextField",
        minimumApi = 1,
        initialValue = 0,
    ),
    SLIDER(
        "Slider",
        "드래그해서 값을 조절하는 슬라이더",
        platformSource = "android.widget.SeekBar",
        material2Function = "Slider",
        material3Function = "Slider",
        minimumApi = 1,
        initialValue = 50,
    ),
    PROGRESS(
        "Progress",
        "진행 정도를 표시하는 인디케이터",
        platformSource = "android.widget.ProgressBar",
        material2Function = "LinearProgressIndicator",
        material3Function = "LinearProgressIndicator",
        minimumApi = 1,
        initialValue = 50,
    ),
    DIALOG(
        "Dialog",
        "확인과 취소를 선택하는 대화상자",
        platformSource = "android.app.AlertDialog",
        material2Function = "AlertDialog",
        material3Function = "AlertDialog",
        minimumApi = 1,
        initialValue = 0,
    ),
    TOGGLE_BUTTON(
        "Toggle button",
        "켜짐과 꺼짐을 선택하는 토글 버튼",
        platformSource = "android.widget.ToggleButton",
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
        material2Function = null,
        material3Function = null,
        minimumApi = 1,
        initialValue = 0,
    ),
    NUMBER_PICKER(
        "Number picker",
        "숫자를 스크롤하거나 버튼으로 선택하는 피커",
        platformSource = "android.widget.NumberPicker",
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
            this == TEXT_FIELD -> if (text.isEmpty()) "Text: empty" else "Text: $text"
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
