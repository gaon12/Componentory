package xyz.gaon.componentory.runs

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import xyz.gaon.componentory.BuildConfig

// The evidence a reopened run needs to say where it actually ran, matching the
// environment list in docs/product-plan.md.
fun environmentSnapshot(context: Context): Map<String, String> {
    val metrics = context.resources.displayMetrics
    val configuration = context.resources.configuration
    return mapOf(
        "appVersion" to BuildConfig.VERSION_NAME,
        "targetSdk" to context.applicationInfo.targetSdkVersion.toString(),
        "material2" to BuildConfig.MATERIAL2_VERSION,
        "material3" to BuildConfig.MATERIAL3_VERSION,
        "androidRelease" to Build.VERSION.RELEASE,
        "api" to Build.VERSION.SDK_INT.toString(),
        "build" to Build.DISPLAY,
        "manufacturer" to Build.MANUFACTURER,
        "model" to Build.MODEL,
        "display" to "${metrics.widthPixels}x${metrics.heightPixels}",
        "densityDpi" to metrics.densityDpi.toString(),
        "fontScale" to configuration.fontScale.toString(),
        "locale" to configuration.locales[0].toLanguageTag(),
        "orientation" to
            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) "landscape"
            else "portrait",
    )
}
