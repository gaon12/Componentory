package xyz.gaon.componentory.lab

import xyz.gaon.componentory.BuildConfig

enum class DesignFamily(val label: String, val platform: PlatformFamily? = null) {
    CLASSIC("Classic", PlatformFamily.CLASSIC),
    HOLO("Holo", PlatformFamily.HOLO),
    MATERIAL("Material", PlatformFamily.MATERIAL),
    MATERIAL2("Material 2"),
    MATERIAL3("Material 3");

    val selectionLabel: String
        get() =
            when (this) {
                CLASSIC -> "Android 1.0 · Classic"
                HOLO -> "Android 3.0 · Holo"
                MATERIAL -> "Android 5.0 · Material"
                MATERIAL2 -> "Material 2 · Compose"
                MATERIAL3 -> "Material 3 · Compose"
            }

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
                else -> return "제공되지 않음"
            }
        return "$packageName.$function"
    }

    fun unsupportedReason(component: LabComponent, runtimeApi: Int): String? {
        if (platform != null) {
            return if (runtimeApi < component.minimumApi)
                "Android API ${component.minimumApi} 이상이 필요합니다. 현재 기기는 API ${runtimeApi}입니다."
            else null
        }
        return when (component) {
            LabComponent.TOGGLE_BUTTON,
            LabComponent.IMAGE_BUTTON,
            LabComponent.RATING,
            LabComponent.NUMBER_PICKER ->
                "$label 라이브러리는 ${component.source.substringAfterLast('.')} 컴포넌트를 제공하지 않습니다."
            else -> null
        }
    }
}
