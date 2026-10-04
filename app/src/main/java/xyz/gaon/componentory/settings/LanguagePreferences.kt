package xyz.gaon.componentory.settings

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.core.content.edit
import java.util.Locale

enum class AppLanguage(val tag: String, val nativeName: String) {
    SYSTEM("", ""),
    KOREAN("ko", "한국어"),
    ENGLISH("en", "English"),
    JAPANESE("ja", "日本語"),
    SIMPLIFIED_CHINESE("zh-Hans", "简体中文"),
    TRADITIONAL_CHINESE("zh-Hant", "繁體中文");

    companion object {
        fun fromTag(tag: String): AppLanguage {
            if (tag.isEmpty()) return SYSTEM
            val locale = Locale.forLanguageTag(tag)
            return when (locale.language) {
                "ko" -> KOREAN
                "en" -> ENGLISH
                "ja" -> JAPANESE
                "zh" ->
                    when {
                        locale.script == "Hant" -> TRADITIONAL_CHINESE
                        locale.script == "Hans" -> SIMPLIFIED_CHINESE
                        locale.country in listOf("TW", "HK", "MO") -> TRADITIONAL_CHINESE
                        else -> SIMPLIFIED_CHINESE
                    }
                else -> SYSTEM
            }
        }
    }
}

object LanguagePreferences {
    fun readTag(context: Context): String =
        if (Build.VERSION.SDK_INT >= 33)
            context.getSystemService(LocaleManager::class.java).applicationLocales.toLanguageTags()
        else preferences(context).getString("language", "").orEmpty()

    fun read(context: Context): AppLanguage =
        AppLanguage.fromTag(readTag(context).substringBefore(','))

    fun apply(activity: Activity, language: AppLanguage) {
        if (read(activity) == language) return
        saveTag(activity, language.tag)
        if (Build.VERSION.SDK_INT < 33) activity.recreate()
    }

    fun saveTag(context: Context, tag: String) {
        if (Build.VERSION.SDK_INT >= 33) {
            // Use the OS setting so the in-app and system language pickers stay in sync.
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(tag)
        } else preferences(context).edit { putString("language", tag) }
    }

    fun localizedContext(base: Context): Context {
        if (Build.VERSION.SDK_INT >= 33) return base
        val tag = readTag(base)
        if (tag.isEmpty()) return base
        val configuration = Configuration(base.resources.configuration)
        val locale = Locale.forLanguageTag(tag)
        configuration.setLocales(LocaleList(locale))
        configuration.setLayoutDirection(locale)
        return base.createConfigurationContext(configuration)
    }

    private fun preferences(context: Context) =
        context.applicationContext.getSharedPreferences("language", Context.MODE_PRIVATE)
}
