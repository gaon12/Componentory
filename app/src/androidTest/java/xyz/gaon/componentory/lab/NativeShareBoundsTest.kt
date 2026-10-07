package xyz.gaon.componentory.lab

import android.view.MenuItem
import android.view.WindowManager
import android.widget.ActionMenuView
import android.widget.ShareActionProvider
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
class NativeShareBoundsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun shareProviderHasItsRealVisibleActionViewInEveryNativeTheme() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            val state = SampleState(0)
            compose.runOnUiThread {
                compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                compose.activity.setContent {
                    ComponentoryTheme(darkTheme = false) {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            SamplePanel(
                                "LEFT",
                                family,
                                {},
                                LabComponent.SHARE_ACTION_PROVIDER,
                                true,
                                0,
                                state,
                            )
                        }
                    }
                }
            }
            compose.waitForIdle()
            compose.runOnIdle {
                val host = compose.activity.findViewById<ActionMenuView>(R.id.sample_left)
                val item = host.tag as MenuItem
                assertEquals(ActionMenuView::class.java, host.javaClass)
                assertTrue(item.actionProvider is ShareActionProvider)
                assertTrue(
                    "${family.name}: host ${host.width} x ${host.height}, ${host.childCount} children",
                    host.isShown && host.width > 0 && host.height > 0,
                )
                val action = requireNotNull(item.actionView)
                assertTrue(
                    "${family.name}: action ${action.width} x ${action.height}",
                    action.isShown && action.width > 0 && action.height > 0,
                )
            }
            compose.onNodeWithTag("sample-share_action_provider").assertIsDisplayed()
        }
    }
}
