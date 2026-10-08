package xyz.gaon.componentory.settings

import android.view.WindowManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.AnnotatedString
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
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
        compose.onNodeWithTag("source_notices_page").assertIsDisplayed()
        compose.onNodeWithTag("settings_page_LICENSES").assertExists()
        compose.onNodeWithTag("source_notices_dialog").assertDoesNotExist()
    }

    @Test
    fun eachBundledDocumentOpensWithItsCompleteOriginalText() {
        SourceNotice.entries.forEach { notice ->
            val original =
                compose.activity.assets
                    .open("legal/${notice.filename}")
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText() }
            compose
                .onNodeWithTag("source_notices_list")
                .performScrollToNode(hasTestTag("source_notice_${notice.name}"))
            compose.onNodeWithTag("source_notice_${notice.name}").performClick()
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
    fun searchAndTheSelectedDocumentSurviveRecreationAndBackReturnsToTheFilteredList() {
        search("apache")
        compose.onNodeWithTag("source_notice_APACHE").assertIsDisplayed().performClick()
        compose.onNodeWithTag("source_notices_search").assertDoesNotExist()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("source_notice_body").assertExists()
        pressBack()
        assertQuery("apache")
        compose.onNodeWithTag("source_notice_APACHE").assertIsDisplayed()
        compose.onNodeWithTag("source_notice_MIT").assertDoesNotExist()
        pressBack()
        compose.onNodeWithTag("settings_page_LICENSES").assertDoesNotExist()
        compose.onNodeWithTag("settings_category_APPEARANCE").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun returningFromARecreatedDocumentKeepsTheListScrollPosition() {
        compose
            .onNodeWithTag("source_notices_list")
            .performScrollToNode(hasTestTag("source_notice_AUTOFILL"))
        compose.onNodeWithTag("source_notice_AUTOFILL").assertIsDisplayed().performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("source_notice_close").performClick()
        compose.onNodeWithTag("source_notice_AUTOFILL").assertIsDisplayed()
    }

    @Test
    fun emptySearchCanBeClearedAndReselectingSettingsClosesTheDocumentAndResetsItsQuery() {
        search("no matching license")
        compose.onNodeWithTag("source_notices_empty").assertIsDisplayed()
        compose.onNodeWithTag("source_notices_clear").performClick()
        assertQuery("")
        search("componentory-mit.txt")
        compose.onNodeWithTag("source_notice_MIT").assertIsDisplayed().performClick()
        compose.onNodeWithTag("nav_settings").assertIsDisplayed().performClick()
        compose.onNodeWithTag("source_notice_body").assertDoesNotExist()
        compose.openSettingsPage("LICENSES")
        assertQuery("")
        compose.onNodeWithTag("source_notice_ATTRIBUTION").assertIsDisplayed()
    }

    private fun assertQuery(query: String) {
        compose
            .onNodeWithTag("source_notices_search")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.EditableText,
                    AnnotatedString(query),
                )
            )
    }

    private fun search(query: String) {
        compose.onNodeWithTag("source_notices_search").performTextReplacement(query)
        compose.onNodeWithTag("source_notices_search").performImeAction()
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
                "pgs-22.1.0-third_party_licenses.txt" to
                    "33dc9af3f3030cee8ff23d6839a10fb94cffdea889c739f6a45e4c202bd28049",
                "pgs-22.1.0-third_party_licenses.json" to
                    "85bac9f2ddd513a4c8b09af0d9ed734ed0ae5443e42a23fec4ef0e6867dc003f",
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
