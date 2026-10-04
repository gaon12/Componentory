package xyz.gaon.componentory.lab

enum class LabComponent(
    val label: String,
    val instruction: String,
    val source: String,
    val minimumApi: Int = 1,
    val initialValue: Int = 0,
) {
    BUTTON("Button", "Tap the samples and compare their response.", "android.widget.Button"),
    CHECKBOX("Checkbox", "Toggle each checkbox independently.", "android.widget.CheckBox"),
    RADIO("Radio buttons", "Choose one option in each panel.", "android.widget.RadioButton"),
    SWITCH("Switch", "Turn each switch on and off.", "android.widget.Switch", 14),
    TEXT_FIELD(
        "Text field",
        "Enter text and inspect the native input styling.",
        "android.widget.EditText",
    ),
    SLIDER(
        "Slider",
        "Drag each thumb to change its value.",
        "android.widget.SeekBar",
        initialValue = 50,
    ),
    PROGRESS(
        "Progress",
        "Use the value controls; this indicator does not accept touch input.",
        "android.widget.ProgressBar",
        initialValue = 50,
    ),
    DIALOG(
        "Dialog",
        "Open a themed dialog and try confirm, cancel, or Back.",
        "android.app.AlertDialog",
    ),
    TOGGLE_BUTTON(
        "Toggle button",
        "Toggle the native on/off button.",
        "android.widget.ToggleButton",
    ),
    IMAGE_BUTTON("Image button", "Tap the framework image button.", "android.widget.ImageButton"),
    RATING("Rating bar", "Touch a star to change the rating.", "android.widget.RatingBar"),
    NUMBER_PICKER(
        "Number picker",
        "Scroll the wheel or use the native increment buttons.",
        "android.widget.NumberPicker",
        minimumApi = 11,
        initialValue = 5,
    );

    val description: String
        get() =
            when (this) {
                BUTTON -> "버튼을 누르고 반응을 확인하세요"
                CHECKBOX -> "선택하거나 해제하는 체크박스"
                RADIO -> "여러 옵션 중 하나를 선택하는 라디오 버튼"
                SWITCH -> "켜고 끄는 스위치"
                TEXT_FIELD -> "텍스트를 입력하는 필드"
                SLIDER -> "드래그해서 값을 조절하는 슬라이더"
                PROGRESS -> "진행 정도를 표시하는 인디케이터"
                DIALOG -> "확인과 취소를 선택하는 대화상자"
                TOGGLE_BUTTON -> "켜짐과 꺼짐을 선택하는 토글 버튼"
                IMAGE_BUTTON -> "아이콘을 누르는 이미지 버튼"
                RATING -> "별을 눌러 점수를 선택하는 별점"
                NUMBER_PICKER -> "숫자를 스크롤하거나 버튼으로 선택하는 피커"
            }

    fun matchesSearch(query: String): Boolean {
        val term = query.trim()
        return term.isEmpty() ||
            listOf(label, description, source).any { it.contains(term, ignoreCase = true) }
    }

    fun feedback(value: Int, text: String): String =
        when (this) {
            BUTTON,
            IMAGE_BUTTON -> "Clicks: $value"
            CHECKBOX -> if (value == 1) "Checked" else "Unchecked"
            RADIO ->
                when (value) {
                    1 -> "Selected: Option A"
                    2 -> "Selected: Option B"
                    else -> "No selection"
                }
            SWITCH,
            TOGGLE_BUTTON -> if (value == 1) "On" else "Off"
            RATING -> "Rating: $value / 5"
            NUMBER_PICKER -> "Number: $value / 10"
            TEXT_FIELD -> if (text.isEmpty()) "Text: empty" else "Text: $text"
            SLIDER,
            PROGRESS -> "Value: $value / 100"
            DIALOG ->
                "Last action: ${when (value) { 1 -> "Opened"
 2 -> "Confirmed"
 3 -> "Cancelled"
 4 -> "Dismissed"
 else -> "Not opened" }}"
        }
}
