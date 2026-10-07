package xyz.gaon.componentory.onboarding

import android.content.Context
import androidx.core.content.edit

class OnboardingPreferences(context: Context) {
    private val preferences =
        context.applicationContext.getSharedPreferences("onboarding", Context.MODE_PRIVATE)

    fun completed(): Boolean = preferences.getBoolean("completed", false)

    fun saveCompleted(completed: Boolean) {
        preferences.edit(commit = true) { putBoolean("completed", completed) }
    }
}
