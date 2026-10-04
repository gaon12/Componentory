package xyz.gaon.componentory.lab

import android.content.Context
import android.os.Build
import xyz.gaon.componentory.R

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
                            context.getString(
                                R.string.device_detail,
                                "${Build.MANUFACTURER} ${Build.MODEL}",
                            ),
                            context.getString(R.string.os_build_detail, Build.DISPLAY),
                            context.getString(R.string.fingerprint_detail, Build.FINGERPRINT),
                            context.getString(
                                R.string.target_sdk_detail,
                                context.applicationInfo.targetSdkVersion,
                            ),
                            context.getString(
                                R.string.display_detail,
                                metrics.widthPixels,
                                metrics.heightPixels,
                                metrics.densityDpi,
                            ),
                            context.getString(
                                R.string.font_scale_detail,
                                configuration.fontScale.toString(),
                            ),
                            context.getString(
                                R.string.locale_detail,
                                configuration.locales.toLanguageTags(),
                            ),
                            context.getString(
                                R.string.orientation_detail,
                                context.getString(
                                    if (configuration.screenWidthDp > configuration.screenHeightDp)
                                        R.string.landscape
                                    else R.string.portrait
                                ),
                            ),
                        )
                        .joinToString("\n"),
            )
        }
    }
}
