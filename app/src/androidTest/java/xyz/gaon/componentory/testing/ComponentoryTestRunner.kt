package xyz.gaon.componentory.testing

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import xyz.gaon.componentory.settings.LanguagePreferences

class ComponentoryTestRunner : AndroidJUnitRunner() {
    private var originalLanguage = ""

    override fun onCreate(arguments: Bundle) {
        originalLanguage = LanguagePreferences.readTag(targetContext)
        // Existing behavior tests assert English feedback. Locale tests change it explicitly.
        LanguagePreferences.saveTag(targetContext, "en")
        super.onCreate(arguments)
    }

    override fun finish(resultCode: Int, results: Bundle) {
        LanguagePreferences.saveTag(targetContext, originalLanguage)
        super.finish(resultCode, results)
    }
}
