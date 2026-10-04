package xyz.gaon.componentory.settings

import android.content.Context
import androidx.core.content.edit

enum class AppAppearance(val label: String) {
    SYSTEM("시스템 설정"),
    LIGHT("밝게"),
    DARK("어둡게"),
}

class AppearancePreferences(context: Context) {
    private val preferences =
        context.applicationContext.getSharedPreferences("appearance", Context.MODE_PRIVATE)

    fun read(): AppAppearance {
        val stored = preferences.getString("mode", null)
        return AppAppearance.entries.firstOrNull { it.name == stored } ?: AppAppearance.SYSTEM
    }

    fun save(appearance: AppAppearance) {
        preferences.edit { putString("mode", appearance.name) }
    }
}
