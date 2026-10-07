package xyz.gaon.componentory.testing

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import xyz.gaon.componentory.onboarding.OnboardingPreferences
import xyz.gaon.componentory.settings.LanguagePreferences

class ComponentoryTestRunner : AndroidJUnitRunner() {
    private var originalLanguage = ""
    private var originalIntroductionCompleted = false

    override fun onCreate(arguments: Bundle) {
        originalLanguage = LanguagePreferences.readTag(targetContext)
        val introduction = OnboardingPreferences(targetContext)
        originalIntroductionCompleted = introduction.completed()
        // The introduction has dedicated tests; other suites start at the catalog.
        introduction.saveCompleted(true)
        // Existing behavior tests assert English feedback. Locale tests change it explicitly.
        LanguagePreferences.saveTag(targetContext, "en")
        super.onCreate(arguments)
    }

    override fun finish(resultCode: Int, results: Bundle) {
        LanguagePreferences.saveTag(targetContext, originalLanguage)
        OnboardingPreferences(targetContext).saveCompleted(originalIntroductionCompleted)
        super.finish(resultCode, results)
    }
}
