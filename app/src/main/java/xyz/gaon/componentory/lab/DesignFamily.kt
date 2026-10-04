package xyz.gaon.componentory.lab

import android.content.Context
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.R

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

    fun origin(context: Context): String =
        platform?.let { context.getString(R.string.theme_origin, it.origin) }
            ?: context.getString(R.string.library_origin, label)

    fun implementation(context: Context): String =
        when (this) {
            MATERIAL2 ->
                "androidx.compose.material:material:${BuildConfig.MATERIAL2_VERSION} · ${context.getString(R.string.light_theme)}"
            MATERIAL3 ->
                "androidx.compose.material3:material3:${BuildConfig.MATERIAL3_VERSION} · ${context.getString(R.string.light_theme)}"
            else -> "android:${requireNotNull(platform).themeName}"
        }

    fun source(component: LabComponent, context: Context? = null): String {
        val absent = context?.getString(R.string.not_provided) ?: "Not provided"
        if (platform != null) return component.platformSource ?: absent
        val packageName =
            if (this == MATERIAL2) "androidx.compose.material" else "androidx.compose.material3"
        val function =
            (if (this == MATERIAL2) component.material2Function else component.material3Function)
                ?: return absent
        return "$packageName.$function"
    }

    fun unsupportedReason(
        component: LabComponent,
        runtimeApi: Int,
        context: Context? = null,
    ): String? {
        val name = context?.getString(component.labelRes) ?: component.label
        if (platform != null) {
            if (component.platformSource == null)
                return context?.getString(R.string.unsupported_platform, name)
                    ?: "The Android platform does not provide a dedicated $name component."
            return if (runtimeApi < component.minimumApi)
                context?.getString(R.string.unsupported_api, component.minimumApi, runtimeApi)
                    ?: "Requires Android API ${component.minimumApi} or later. This device runs API $runtimeApi."
            else null
        }
        val function =
            if (this == MATERIAL2) component.material2Function else component.material3Function
        return if (function == null)
            context?.getString(R.string.unsupported_library, label, name)
                ?: "The $label library does not provide $name."
        else null
    }
}
