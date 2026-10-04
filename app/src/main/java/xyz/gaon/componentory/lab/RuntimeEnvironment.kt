package xyz.gaon.componentory.lab

import android.content.Context
import android.os.Build

data class RuntimeEnvironment(val summary: String, val details: String) {
    companion object {
        fun read(context: Context): RuntimeEnvironment {
            val metrics = context.resources.displayMetrics
            val configuration = context.resources.configuration
            return RuntimeEnvironment(
                summary =
                    "Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT} · ${Build.MODEL}",
                details =
                    listOf(
                            "Device: ${Build.MANUFACTURER} ${Build.MODEL}",
                            "OS build: ${Build.DISPLAY}",
                            "Fingerprint: ${Build.FINGERPRINT}",
                            "Target SDK: ${context.applicationInfo.targetSdkVersion}",
                            "App display: ${metrics.widthPixels} × ${metrics.heightPixels} px · ${metrics.densityDpi} dpi",
                            "Font scale: ${configuration.fontScale}",
                            "Locale: ${configuration.locales.toLanguageTags()}",
                            "Orientation: ${if (configuration.screenWidthDp > configuration.screenHeightDp) "landscape" else "portrait"}",
                        )
                        .joinToString("\n"),
            )
        }
    }
}
