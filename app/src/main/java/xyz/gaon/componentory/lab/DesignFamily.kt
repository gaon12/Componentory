package xyz.gaon.componentory.lab

import xyz.gaon.componentory.BuildConfig

enum class DesignFamily(val label: String, val platform: PlatformFamily? = null) {
    CLASSIC("Classic", PlatformFamily.CLASSIC),
    HOLO("Holo", PlatformFamily.HOLO),
    MATERIAL("Material", PlatformFamily.MATERIAL),
    MATERIAL2("Material 2"),
    MATERIAL3("Material 3");

    val origin: String
        get() = platform?.let { "Theme from ${it.origin}" } ?: "Compose library · $label design"

    val implementation: String
        get() =
            when (this) {
                MATERIAL2 ->
                    "androidx.compose.material:material:${BuildConfig.MATERIAL2_VERSION} · light"
                MATERIAL3 ->
                    "androidx.compose.material3:material3:${BuildConfig.MATERIAL3_VERSION} · light"
                else -> "android:${requireNotNull(platform).themeName}"
            }

    fun source(component: LabComponent): String {
        if (platform != null) return component.source
        val packageName =
            if (this == MATERIAL2) "androidx.compose.material" else "androidx.compose.material3"
        val function =
            when (component) {
                LabComponent.BUTTON -> "Button"
                LabComponent.CHECKBOX -> "Checkbox"
                LabComponent.RADIO -> "RadioButton"
                LabComponent.SWITCH -> "Switch"
                LabComponent.TEXT_FIELD -> "TextField"
                LabComponent.SLIDER -> "Slider"
                LabComponent.PROGRESS -> "LinearProgressIndicator"
                LabComponent.DIALOG -> "AlertDialog"
            }
        return "$packageName.$function"
    }
}
