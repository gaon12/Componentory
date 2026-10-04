package xyz.gaon.componentory.settings

import android.content.Context
import androidx.core.content.edit
import xyz.gaon.componentory.R

enum class AppAppearance(val labelRes: Int) {
    SYSTEM(R.string.appearance_system),
    LIGHT(R.string.appearance_light),
    DARK(R.string.appearance_dark),
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
