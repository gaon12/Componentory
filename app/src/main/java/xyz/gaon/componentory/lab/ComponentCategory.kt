package xyz.gaon.componentory.lab

import xyz.gaon.componentory.R

enum class ComponentCategory(val labelRes: Int) {
    ACTION(R.string.category_action),
    SELECTION(R.string.category_selection),
    INPUT(R.string.category_input),
    INDICATOR(R.string.category_indicator),
    PICKER(R.string.category_picker),
    FEEDBACK(R.string.category_feedback),
    CONTENT(R.string.category_content),
    NAVIGATION(R.string.category_navigation),
    LAYOUT(R.string.category_layout),
    MEDIA(R.string.category_media),
    LEGACY(R.string.category_legacy),
    EASTER_EGG(R.string.category_easter_egg),
}
