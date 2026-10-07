package xyz.gaon.componentory.settings

import android.content.res.Configuration
import android.os.LocaleList
import android.view.WindowManager
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.catalog.matchesSearch
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.testing.openSettingsPage

@RunWith(AndroidJUnit4::class)
class LanguageSettingsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @After
    fun restoreEnglishBaseline() {
        applyLanguageAndWaitForRecreation(AppLanguage.ENGLISH)
    }

    @Test
    fun eachLanguageLocalizesSearchNativeAndBothLibraryButtonSamples() {
        val examples =
            listOf(
                Triple(AppLanguage.KOREAN, "버튼", "눌러 보세요"),
                Triple(AppLanguage.ENGLISH, "Button", "Tap me"),
                Triple(AppLanguage.JAPANESE, "ボタン", "押してください"),
                Triple(AppLanguage.SIMPLIFIED_CHINESE, "按钮", "点击试试"),
                Triple(AppLanguage.TRADITIONAL_CHINESE, "按鈕", "點一下試試"),
            )
        examples.forEach { (language, name, buttonText) ->
            changeLanguage(language)
            compose.onNodeWithTag("language_${language.name}").assertIsSelected()
            compose.activityRule.scenario.recreate()
            compose.onNodeWithTag("language_${language.name}").assertIsSelected()
            compose.onNodeWithTag("nav_list").performClick()
            compose.onNodeWithTag("nav_list").performClick()
            compose.onNodeWithTag("component_search").performTextReplacement(name)
            compose.onNodeWithTag("component_search").performImeAction()
            compose.onNodeWithTag("list_BUTTON").performScrollTo().performClick()
            compose.onNodeWithTag("detail_component_title").assertTextEquals(name)
            chooseFamily(DesignFamily.CLASSIC)
            compose.waitForIdle()
            onView(withId(R.id.sample_left)).check(matches(withText(buttonText)))
            compose.onNodeWithTag("reset").performClick()
            compose.onNodeWithTag("native_LEFT").performScrollTo().performClick()
            compose
                .onNodeWithTag("status_LEFT")
                .assertTextEquals(compose.activity.getString(R.string.status_clicks, 1))
            listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                chooseFamily(family)
                compose.onNodeWithTag("library_LEFT").assertTextEquals(buttonText)
                compose.onNodeWithTag("reset").performClick()
                compose.onNodeWithTag("library_LEFT").performScrollTo().performClick()
                compose
                    .onNodeWithTag("status_LEFT")
                    .assertTextEquals(compose.activity.getString(R.string.status_clicks, 1))
            }
        }
    }

    @Test
    fun localizedNamesDescriptionsAndUnsupportedReasonsResolveForEveryLanguage() {
        AppLanguage.entries
            .filter { it != AppLanguage.SYSTEM }
            .forEach { language ->
                val configuration = Configuration(compose.activity.resources.configuration)
                configuration.setLocales(LocaleList(Locale.forLanguageTag(language.tag)))
                val localized = compose.activity.createConfigurationContext(configuration)
                LabComponent.entries.forEach { component ->
                    val name = localized.getString(component.labelRes)
                    assertFalse(localized.getString(component.descriptionRes).isBlank())
                    assertEquals(true, component.matchesSearch(name, localized))
                    assertEquals(true, component.matchesSearch(component.source, localized))
                }
                assertEquals(
                    localized.getString(
                        R.string.unsupported_platform,
                        localized.getString(LabComponent.OUTLINED_BUTTON.labelRes),
                    ),
                    DesignFamily.CLASSIC.unsupportedReason(
                        LabComponent.OUTLINED_BUTTON,
                        36,
                        localized,
                    ),
                )
            }
    }

    @Test
    fun choosingSystemLanguageClearsTheAppOverride() {
        changeLanguage(AppLanguage.JAPANESE)
        changeLanguage(AppLanguage.SYSTEM)
        compose.onNodeWithTag("language_SYSTEM").assertIsSelected()
        assertEquals("", LanguagePreferences.readTag(compose.activity))
    }

    private fun changeLanguage(language: AppLanguage) {
        compose.openSettingsPage("LANGUAGE")
        val previousActivity = compose.activity
        val changing = LanguagePreferences.read(previousActivity) != language
        compose.onNodeWithTag("language_${language.name}").performScrollTo().performClick()
        waitForLanguageActivity(language, previousActivity, changing)
    }

    private fun applyLanguageAndWaitForRecreation(language: AppLanguage) {
        val previousActivity = compose.activity
        val changing = LanguagePreferences.read(previousActivity) != language
        compose.runOnUiThread { LanguagePreferences.apply(previousActivity, language) }
        waitForLanguageActivity(language, previousActivity, changing)
    }

    private fun waitForLanguageActivity(
        language: AppLanguage,
        previousActivity: MainActivity,
        changing: Boolean,
    ) {
        // LocaleManager saves the preference before the replacement Activity is ready.
        // Waiting for that Activity avoids asking a detached Compose root for its next frame.
        compose.waitUntil(10_000) {
            val current = runCatching { compose.activity }.getOrNull()
            current != null &&
                (!changing || current !== previousActivity) &&
                current.lifecycle.currentState == Lifecycle.State.RESUMED &&
                current.window.decorView.isAttachedToWindow &&
                LanguagePreferences.read(current) == language
        }
        compose.waitForIdle()
    }

    private fun chooseFamily(family: DesignFamily) {
        compose.onNodeWithTag("family_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("family_LEFT_${family.name}").performClick()
    }
}
