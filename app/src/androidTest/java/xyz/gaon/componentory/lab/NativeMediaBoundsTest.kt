package xyz.gaon.componentory.lab

import android.view.WindowManager
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R

@RunWith(AndroidJUnit4::class)
class NativeMediaBoundsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun openComparison() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("nav_compare").performClick()
    }

    @Test
    fun bothRealMediaWidgetsHaveVisibleBoundsAndPrepareTheBundledClipInEveryTheme() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            compose.onNodeWithTag("family_LEFT").performScrollTo().performClick()
            compose.onNodeWithTag("family_LEFT_${family.name}").performScrollTo().performClick()
            listOf(LabComponent.VIDEO_VIEW, LabComponent.MEDIA_CONTROLLER).forEach { component ->
                compose.onNodeWithTag("component_picker").performScrollTo().performClick()
                compose.onNodeWithTag("picker_category_ALL").performClick()
                compose.onNodeWithTag("picker_search").performTextReplacement(component.label)
                compose.onNodeWithTag("picker_search").performImeAction()
                compose.waitUntil(5_000) {
                    compose.onNodeWithTag("picker_category_ALL").isDisplayed()
                }
                compose
                    .onNodeWithTag("component_picker_list")
                    .performScrollToNode(hasTestTag("component_${component.name}"))
                compose.onNodeWithTag("component_${component.name}").performClick()
                compose
                    .onNode(
                        hasTestTag("sample-${component.name.lowercase()}") and
                            hasAnyAncestor(hasTestTag("panel_LEFT"))
                    )
                    .performScrollTo()
                    .assertIsDisplayed()
                compose.waitUntil(10_000) {
                    var prepared = false
                    compose.runOnUiThread {
                        prepared =
                            compose.activity
                                .findViewById<VideoView>(R.id.sample_left)
                                ?.duration
                                ?.let { it > 0 } == true
                    }
                    prepared
                }
                compose.runOnIdle {
                    val video = compose.activity.findViewById<VideoView>(R.id.sample_left)
                    assertEquals(VideoView::class.java, video.javaClass)
                    assertTrue(video.isShown && video.width > 0 && video.height > 0)
                    if (component == LabComponent.MEDIA_CONTROLLER)
                        assertTrue(video.tag is MediaController)
                }
            }
        }
    }
}
