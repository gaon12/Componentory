package xyz.gaon.componentory.settings

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.testing.openSettingsPage

@RunWith(AndroidJUnit4::class)
class PrivacyPolicyTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun offlinePolicySurvivesRecreationAndCanBeClosed() {
        compose.openSettingsPage("PRIVACY")
        compose.onNodeWithTag("privacy_policy_open").performClick()
        waitForPolicy()
        compose
            .onNodeWithTag("source_notice_body")
            .assertTextContains("Componentory Privacy Policy", substring = true)
        compose.activityRule.scenario.recreate()
        waitForPolicy()
        compose
            .onNodeWithTag("source_notice_body")
            .assertTextContains("Optional keyboard and autofill demonstration", substring = true)
        compose.onNodeWithTag("source_notice_close").performClick()
        compose.onNodeWithTag("source_notice_body").assertDoesNotExist()
        compose.onNodeWithTag("privacy_policy_open").assertIsDisplayed()
    }

    private fun waitForPolicy() {
        compose.waitUntil(5_000) {
            compose
                .onAllNodes(hasText("Componentory Privacy Policy", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun installedApplicationDoesNotEnableNetworkAccessOrAutomaticBackup() {
        val context = compose.activity
        val info =
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS,
            )
        assertFalse(info.requestedPermissions.orEmpty().contains("android.permission.INTERNET"))
        assertEquals(0, context.applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP)
    }
}
