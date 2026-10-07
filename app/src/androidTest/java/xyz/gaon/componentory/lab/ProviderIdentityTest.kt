package xyz.gaon.componentory.lab

import android.os.Build
import android.view.WindowManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences
import xyz.gaon.componentory.testing.openSettingsPage

@RunWith(AndroidJUnit4::class)
class ProviderIdentityTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @After
    fun restoreEnglish() {
        compose.runOnUiThread { LanguagePreferences.apply(compose.activity, AppLanguage.ENGLISH) }
        compose.waitForIdle()
    }

    @Test
    fun buttonProvidersUseThemeOrPinnedLibraryNamesAndExposeTheirSelectedMenuEntry() {
        openComponent(LabComponent.BUTTON)
        DesignFamily.entries.forEach { family ->
            chooseProvider(family)
            assertPrimaryLabel(family)
            assertSourceAndImplementation(LabComponent.BUTTON, family)
            if (family.platform != null) {
                val origin = compose.onNodeWithText(family.origin(compose.activity))
                origin.assertExists()
                compose.onNodeWithTag("family_LEFT").performScrollTo()
                assertTrue(
                    "The historical theme origin belongs below the primary provider label.",
                    compose.onNodeWithTag("family_LEFT").fetchSemanticsNode().boundsInRoot.bottom <=
                        origin.fetchSemanticsNode().boundsInRoot.top,
                )
            }
            openProviderMenu()
            DesignFamily.entries.forEach { option ->
                val entry = compose.onNodeWithTag("family_LEFT_${option.name}")
                entry.assertTextContains(primaryLabel(option), substring = true)
                if (option == family) entry.assertIsSelected() else entry.assertIsNotSelected()
                assertMenuAvailability(option, available = true)
            }
            // Selecting the current entry closes the menu without choosing another supplier.
            compose.onNodeWithTag("family_LEFT_${family.name}").performScrollTo().performClick()
            assertPrimaryLabel(family)
        }
    }

    @Test
    fun rangeSliderAvailabilityExplainsMissingFrameworkApiAndAllowsRecovery() {
        openComponent(LabComponent.RANGE_SLIDER)
        openProviderMenu()
        DesignFamily.entries.forEach { family ->
            assertMenuAvailability(family, available = family.platform == null)
        }
        compose.onNodeWithTag("family_LEFT_CLASSIC").performScrollTo().performClick()
        assertPrimaryLabel(DesignFamily.CLASSIC)
        compose.onNodeWithTag("unsupported_LEFT").assertExists()
        compose
            .onNodeWithText(
                compose.activity.getString(
                    R.string.unsupported_platform,
                    compose.activity.getString(LabComponent.RANGE_SLIDER.labelRes),
                )
            )
            .assertExists()
        compose.onNodeWithTag("native_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("library_LEFT").assertDoesNotExist()

        openProviderMenu()
        compose.onNodeWithTag("family_LEFT_CLASSIC").assertIsSelected()
        compose.onNodeWithTag("family_LEFT_MATERIAL3").assertIsNotSelected()
        assertMenuAvailability(DesignFamily.MATERIAL3, available = true)
        compose.onNodeWithTag("family_LEFT_MATERIAL3").performScrollTo().performClick()
        assertPrimaryLabel(DesignFamily.MATERIAL3)
        assertSourceAndImplementation(LabComponent.RANGE_SLIDER, DesignFamily.MATERIAL3)
        compose.onNodeWithTag("unsupported_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("library_LEFT").assertExists()
    }

    @Test
    fun fiveLanguagesLocalizeProviderGuidanceBeforeTheSampleWhileKeepingApiIdentity() {
        openComponent(LabComponent.RANGE_SLIDER)
        chooseProvider(DesignFamily.MATERIAL2)
        val titles = mutableSetOf<String>()
        val runtimeNotes = mutableSetOf<String>()
        val availableLabels = mutableSetOf<String>()
        val unsupportedLabels = mutableSetOf<String>()
        val languages = AppLanguage.entries.filter { it != AppLanguage.SYSTEM }
        languages.forEach { language ->
            changeLanguage(language)
            val title = compose.activity.getString(R.string.choose_ui_provider)
            val runtimeNote =
                compose.activity.getString(R.string.runtime_sample_note, Build.VERSION.RELEASE)
            titles += title
            runtimeNotes += runtimeNote
            availableLabels += compose.activity.getString(R.string.sample_available)
            unsupportedLabels += compose.activity.getString(R.string.unsupported)
            compose.onNodeWithText(title).assertExists()
            compose
                .onNodeWithTag("runtime_sample")
                .performScrollTo()
                .assertTextEquals(runtimeNote)
                .assertTextContains(Build.VERSION.RELEASE, substring = true)
            val noteBounds =
                compose.onNodeWithTag("runtime_sample").fetchSemanticsNode().boundsInRoot
            val panelBounds = compose.onNodeWithTag("panel_LEFT").fetchSemanticsNode().boundsInRoot
            assertTrue(
                "The current OS note must precede the sample panel.",
                noteBounds.bottom <= panelBounds.top,
            )
            assertPrimaryLabel(DesignFamily.MATERIAL2)
            assertSourceAndImplementation(LabComponent.RANGE_SLIDER, DesignFamily.MATERIAL2)
            openProviderMenu()
            DesignFamily.entries.forEach { family ->
                compose
                    .onNodeWithTag("family_LEFT_${family.name}")
                    .assertTextContains(primaryLabel(family), substring = true)
                assertMenuAvailability(family, available = family.platform == null)
            }
            compose.onNodeWithTag("family_LEFT_MATERIAL2").performScrollTo().performClick()
        }
        assertEquals(
            "Every supported language needs its own selector title.",
            languages.size,
            titles.size,
        )
        assertEquals(
            "Every supported language needs its own current OS note.",
            languages.size,
            runtimeNotes.size,
        )
        assertEquals(languages.size, availableLabels.size)
        assertEquals(languages.size, unsupportedLabels.size)
    }

    private fun openComponent(component: LabComponent) {
        compose.onNodeWithTag("component_search").performTextReplacement(component.label)
        compose.onNodeWithTag("component_search").performImeAction()
        compose
            .onNodeWithTag("component_list")
            .performScrollToNode(hasTestTag("list_${component.name}"))
        compose.onNodeWithTag("list_${component.name}").performClick()
    }

    private fun changeLanguage(language: AppLanguage) {
        compose.openSettingsPage("LANGUAGE")
        compose.onNodeWithTag("language_${language.name}").performScrollTo().performClick()
        compose.waitUntil(5_000) { LanguagePreferences.read(compose.activity) == language }
        compose.waitForIdle()
        keepScreenOn()
        compose.onNodeWithTag("nav_list").performClick()
        compose.onNodeWithTag("detail_screen").assertExists()
    }

    private fun openProviderMenu() {
        compose.onNodeWithTag("family_LEFT").performScrollTo().performClick()
    }

    private fun chooseProvider(family: DesignFamily) {
        openProviderMenu()
        compose.onNodeWithTag("family_LEFT_${family.name}").performScrollTo().performClick()
    }

    private fun assertMenuAvailability(family: DesignFamily, available: Boolean) {
        compose
            .onNodeWithTag("provider_availability_LEFT_${family.name}", useUnmergedTree = true)
            .assertTextEquals(
                compose.activity.getString(
                    if (available) R.string.sample_available else R.string.unsupported
                )
            )
    }

    private fun assertPrimaryLabel(family: DesignFamily) {
        val label = primaryLabel(family)
        compose.onNodeWithTag("family_LEFT").assertTextEquals("$label  ▾")
    }

    private fun primaryLabel(family: DesignFamily): String =
        when (family) {
            DesignFamily.MATERIAL2 -> "Compose Material 2 · ${BuildConfig.MATERIAL2_VERSION}"
            DesignFamily.MATERIAL3 -> "Compose Material 3 · ${BuildConfig.MATERIAL3_VERSION}"
            else -> "${family.label} · ${requireNotNull(family.platform).themeName}"
        }

    private fun assertSourceAndImplementation(component: LabComponent, family: DesignFamily) {
        val source =
            when (family) {
                DesignFamily.MATERIAL2 ->
                    "androidx.compose.material.${requireNotNull(component.material2Function)}"
                DesignFamily.MATERIAL3 ->
                    "androidx.compose.material3.${requireNotNull(component.material3Function)}"
                else -> requireNotNull(component.platformSource)
            }
        val implementation =
            when (family) {
                DesignFamily.MATERIAL2 ->
                    "androidx.compose.material:material:${BuildConfig.MATERIAL2_VERSION}"
                DesignFamily.MATERIAL3 ->
                    "androidx.compose.material3:material3:${BuildConfig.MATERIAL3_VERSION}"
                else -> "android:${requireNotNull(family.platform).themeName}"
            }
        expandDetails("LEFT")
        compose.onNodeWithTag("source_LEFT").assertTextEquals(source)
        compose
            .onNodeWithTag("implementation_LEFT")
            .assertTextContains(implementation, substring = true)
    }

    private fun expandDetails(panel: String) {
        val toggle = compose.onNodeWithTag("implementation_details_$panel").performScrollTo()
        if (
            toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] !=
                ToggleableState.On
        )
            toggle.performTouchInput { click() }
    }
}
