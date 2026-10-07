package xyz.gaon.componentory.settings

import android.view.WindowManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.security.MessageDigest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.testing.openSettingsPage

@RunWith(AndroidJUnit4::class)
class SourceNoticesTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun openSettings() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        // A previous sample can leave the real IME closing while settings reflows.
        closeSoftKeyboard()
        compose.waitUntil(5_000) {
            ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                ?.isVisible(WindowInsetsCompat.Type.ime()) != true
        }
        compose.openSettingsPage("LICENSES")
    }

    @Test
    fun eachBundledDocumentOpensWithItsCompleteOriginalText() {
        SourceNotice.entries.forEach { notice ->
            val original =
                compose.activity.assets
                    .open("legal/${notice.filename}")
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText() }
            compose.onNodeWithTag("source_notice_${notice.name}").performScrollTo().performClick()
            try {
                compose.waitUntil(10_000) {
                    val nodes =
                        compose
                            .onAllNodes(androidx.compose.ui.test.hasTestTag("source_notice_body"))
                            .fetchSemanticsNodes()
                    nodes
                        .singleOrNull()
                        ?.config
                        ?.getOrNull(SemanticsProperties.Text)
                        ?.singleOrNull()
                        ?.text == original
                }
            } catch (failure: Throwable) {
                val actual =
                    compose
                        .onNodeWithTag("source_notice_body")
                        .fetchSemanticsNode()
                        .config
                        .getOrNull(SemanticsProperties.Text)
                        ?.joinToString("\n") { it.text }
                throw AssertionError(
                    "${notice.name}: expected ${original.length} characters; " +
                        "received ${actual?.length}: ${actual?.take(100)}",
                    failure,
                )
            }
            compose
                .onNodeWithTag("source_notice_body")
                .assertIsDisplayed()
                .assertTextEquals(original)
            compose.onNodeWithTag("source_notice_close").assertIsDisplayed().performClick()
            compose.onNodeWithTag("source_notice_body").assertDoesNotExist()
        }
    }

    @Test
    fun bundledUpstreamNoticesMatchTheirAuditedSourceHashes() {
        val metadata =
            compose.activity.assets
                .open("legal/provenance.json")
                .bufferedReader(Charsets.UTF_8)
                .use { JSONObject(it.readText()) }
        val expected =
            mapOf(
                "aosp-frameworks-base-NOTICE.txt" to
                    "cf4dd7cea5b6bcd2622ddd3529b3c74d16ba2b4db444575bc6f5c33f8a960530",
                "android-sdk-NOTICE.txt" to
                    "29714c52481af51064f56bca49bfbbec4d1004eadfc82415fc35bd200bee4e3c",
                "androidx-autofill-LICENSE.txt" to
                    "809fa1ed21450f59827d1e9aec720bbc4b687434fa22283c6cb5dd82a47ab9c0",
                "Apache-2.0.txt" to
                    "cfc7749b96f63bd31c3c42b5c471bf756814053e847c10f3eb003417bc523d30",
            )
        val files = metadata.getJSONArray("files")
        assertEquals(expected.size, files.length())
        for (index in 0 until files.length()) {
            val record = files.getJSONObject(index)
            val path = record.getString("path")
            assertTrue(path in expected)
            val bytes = compose.activity.assets.open("legal/$path").use { it.readBytes() }
            val hash =
                MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") {
                    "%02x".format(it)
                }
            assertEquals(expected.getValue(path), hash)
            assertEquals(hash, record.getString("sha256"))
        }
    }
}
