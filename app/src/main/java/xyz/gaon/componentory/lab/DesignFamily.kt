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
        if (platform != null) return component.platformSource ?: "제공되지 않음"
        val packageName =
            if (this == MATERIAL2) "androidx.compose.material" else "androidx.compose.material3"
        val function =
            (if (this == MATERIAL2) component.material2Function else component.material3Function)
                ?: return "제공되지 않음"
        return "$packageName.$function"
    }

    fun unsupportedReason(component: LabComponent, runtimeApi: Int): String? {
        if (platform != null) {
            if (component.platformSource == null)
                return "Android 플랫폼은 ${component.label} 전용 컴포넌트를 제공하지 않습니다."
            return if (runtimeApi < component.minimumApi)
                "Android API ${component.minimumApi} 이상이 필요합니다. 현재 기기는 API ${runtimeApi}입니다."
            else null
        }
        val function =
            if (this == MATERIAL2) component.material2Function else component.material3Function
        return if (function == null) "$label 라이브러리는 ${component.label} 컴포넌트를 제공하지 않습니다." else null
    }
}
