package xyz.gaon.componentory.settings

import android.content.ActivityNotFoundException
import android.content.ContextWrapper
import android.content.Intent
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

@RunWith(AndroidJUnit4::class)
class ProjectLinksTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val opened = mutableListOf<Intent>()
    private var browserAvailable = true

    @Before
    fun openAboutWithAnInterceptedExternalActivity() {
        val context =
            object : ContextWrapper(compose.activity) {
                override fun startActivity(intent: Intent) {
                    if (!browserAvailable) throw ActivityNotFoundException("No browser installed")
                    opened += intent
                }
            }
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            compose.activity.setContent {
                ComponentoryTheme {
                    CompositionLocalProvider(LocalContext provides context) {
                        SettingsScreen(AppAppearance.SYSTEM, {}, AppLanguage.ENGLISH, {})
                    }
                }
            }
        }
        compose.onNodeWithTag("settings_category_ABOUT").performScrollTo().performClick()
    }

    @Test
    fun repositoryAndFeedbackRowsOpenTheRequestedHttpsDestinations() {
        compose.onNodeWithTag("project_repository").performScrollTo().performClick()
        compose.onNodeWithTag("project_feedback").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(
                listOf(
                    "https://github.com/gaon12/Componentory",
                    "https://github.com/gaon12/Componentory/issues/new",
                ),
                opened.map { it.dataString },
            )
            assertEquals(listOf(Intent.ACTION_VIEW, Intent.ACTION_VIEW), opened.map { it.action })
        }
    }

    @Test
    fun aMissingBrowserShowsAnErrorAndASuccessfulRetryClearsIt() {
        compose.runOnIdle { browserAvailable = false }
        compose.onNodeWithTag("project_feedback").performScrollTo().performClick()
        compose
            .onNodeWithTag("project_link_failed")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextEquals("Cannot open this link. Check your browser settings.")
        compose.runOnIdle { browserAvailable = true }
        compose.onNodeWithTag("project_feedback").performScrollTo().performClick()
        compose.onNodeWithTag("project_link_failed").assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, opened.size) }
    }
}
