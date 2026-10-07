package xyz.gaon.componentory.lab

import android.view.WindowManager
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

@RunWith(AndroidJUnit4::class)
class NativeWebBoundsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun webPreviewHasVisibleBoundsBeforeItsRealPageLoadsInEveryNativeTheme() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            val state = SampleState(0)
            compose.runOnUiThread {
                compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                compose.activity.setContent {
                    ComponentoryTheme(darkTheme = false) {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            SamplePanel("LEFT", family, {}, LabComponent.WEB_VIEW, true, 0, state)
                        }
                    }
                }
            }
            compose.waitForIdle()
            compose.runOnIdle {
                val web = compose.activity.findViewById<WebView>(R.id.sample_left)
                assertEquals(WebView::class.java, web.javaClass)
                assertTrue(
                    "${family.name}: web ${web.width} x ${web.height}",
                    web.isShown && web.width > 0 && web.height > 0,
                )
            }
            compose.onNodeWithTag("sample-web_view").assertIsDisplayed()
            compose.waitUntil(10_000) {
                compose.runOnIdle {
                    compose.activity.findViewById<WebView>(R.id.sample_left).contentHeight > 0
                }
            }
        }
    }
}
