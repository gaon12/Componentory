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
    );

    fun feedback(value: Int, text: String): String =
        when (this) {
            BUTTON -> "Clicks: $value"
            CHECKBOX -> if (value == 1) "Checked" else "Unchecked"
            RADIO ->
                when (value) {
                    1 -> "Selected: Option A"
                    2 -> "Selected: Option B"
                    else -> "No selection"
                }
            SWITCH -> if (value == 1) "On" else "Off"
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
