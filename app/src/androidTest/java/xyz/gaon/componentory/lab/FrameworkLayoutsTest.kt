package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.WindowManager
import android.widget.AbsoluteLayout
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences

// Static containers render their own children; the scenarios verify class
// identity, structure, unsupported libraries, copying semantics and locale text.
@RunWith(AndroidJUnit4::class)
@Suppress("DEPRECATION")
class FrameworkLayoutsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun prepareEnglishComparison() {
        keepScreenOn()
        changeLanguage(AppLanguage.ENGLISH)
        compose.onNodeWithTag("nav_compare").performClick()
    }

    @After
    fun restoreEnglishBaseline() {
        compose.runOnUiThread { LanguagePreferences.apply(compose.activity, AppLanguage.ENGLISH) }
        compose.waitForIdle()
        keepScreenOn()
    }

    @Test
    fun nativeFrameworkLayoutsRenderThemedChildrenInEveryPlatformTheme() {
        mapOf(
                LabComponent.FRAME_LAYOUT to FrameLayout::class.java,
                LabComponent.LINEAR_LAYOUT to LinearLayout::class.java,
                LabComponent.TABLE_LAYOUT to TableLayout::class.java,
                LabComponent.GRID_LAYOUT to GridLayout::class.java,
                LabComponent.RELATIVE_LAYOUT to RelativeLayout::class.java,
                LabComponent.ABSOLUTE_LAYOUT to AbsoluteLayout::class.java,
            )
            .forEach { (component, type) ->
                configure(component, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
                assertNativeIdentity("LEFT", component, DesignFamily.CLASSIC)
                compose.runOnIdle {
                    val layout = layout("LEFT")
                    assertEquals(type, layout.javaClass)
                    assertTrue(layout.isShown && layout.width > 0 && layout.height > 0)
                }
                status("LEFT", "Preview: ${component.label}")
            }

        configure(LabComponent.FRAME_LAYOUT, DesignFamily.HOLO, DesignFamily.MATERIAL3)
        compose.runOnIdle {
            val frame = layout("LEFT") as FrameLayout
            assertEquals(2, frame.childCount)
            assertEquals(
                compose.activity.getString(R.string.layout_child, 1),
                (frame.getChildAt(0) as TextView).text.toString(),
            )
        }

        configure(LabComponent.LINEAR_LAYOUT, DesignFamily.MATERIAL, DesignFamily.MATERIAL3)
        compose.runOnIdle {
            val linear = layout("LEFT") as LinearLayout
            assertEquals(LinearLayout.HORIZONTAL, linear.orientation)
            assertEquals(3, linear.childCount)
            repeat(3) { index ->
                val params = linear.getChildAt(index).layoutParams as LinearLayout.LayoutParams
                assertEquals(0, params.width)
                assertEquals(1f, params.weight, 0f)
            }
        }

        configure(LabComponent.TABLE_LAYOUT, DesignFamily.MATERIAL, DesignFamily.MATERIAL3)
        compose.runOnIdle {
            val table = layout("LEFT") as TableLayout
            assertEquals(2, table.childCount)
            val row = table.getChildAt(0) as TableRow
            assertEquals(2, row.childCount)
            assertEquals(
                compose.activity.getString(R.string.layout_cell, 1, 1),
                (row.getChildAt(0) as TextView).text.toString(),
            )
        }

        configure(LabComponent.GRID_LAYOUT, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
        compose.runOnIdle {
            val grid = layout("LEFT") as GridLayout
            assertEquals(2, grid.columnCount)
            assertEquals(4, grid.childCount)
        }

        configure(LabComponent.RELATIVE_LAYOUT, DesignFamily.MATERIAL, DesignFamily.MATERIAL3)
        compose.runOnIdle {
            val relative = layout("LEFT") as RelativeLayout
            assertEquals(2, relative.childCount)
            val below = relative.getChildAt(1).layoutParams as RelativeLayout.LayoutParams
            assertEquals(
                "The second child must keep a real BELOW rule",
                relative.getChildAt(0).id,
                below.rules[RelativeLayout.BELOW],
            )
        }

        configure(LabComponent.SPACE, DesignFamily.HOLO, DesignFamily.MATERIAL3)
        compose.runOnIdle {
            val space = layout("LEFT")
            assertEquals(Space::class.java, space.javaClass)
            assertEquals(View.VISIBLE, space.visibility)
            assertTrue("Space keeps a real gap", space.width > 0)
        }

        configure(LabComponent.ABSOLUTE_LAYOUT, DesignFamily.MATERIAL, DesignFamily.MATERIAL3)
        compose.runOnIdle {
            val absolute = layout("LEFT") as AbsoluteLayout
            assertEquals(2, absolute.childCount)
            val params = absolute.getChildAt(1).layoutParams as AbsoluteLayout.LayoutParams
            assertTrue(params.x > 0 && params.y > 0)
        }
        compose
            .onNodeWithTag("clock_note_LEFT")
            .assertTextContains("Deprecated since API 3", substring = true)
    }

    @Test
    fun layoutsReportNoInputsAndUnsupportedLibraryTargets() {
        configure(LabComponent.LINEAR_LAYOUT, DesignFamily.CLASSIC, DesignFamily.HOLO)
        blockedCopy("LEFT_TO_RIGHT", "This sample has no inputs to copy.")

        chooseFamily("RIGHT", DesignFamily.MATERIAL2)
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains(
                "The Material 2 library does not provide Linear layout.",
                substring = true,
            )
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")

        setEnabled(false)
        compose.runOnIdle { assertFalse(layout("LEFT").isEnabled) }
    }

    @Test
    fun fiveLanguagesLocalizeLayoutLabelsAndNotes() {
        names.forEach { text ->
            changeLanguage(text.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configure(LabComponent.TABLE_LAYOUT, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
            status("LEFT", "${text.preview}: ${text.tableLabel}")
            compose.onNodeWithTag("layout_note_LEFT").assertTextEquals(text.note)
            compose.runOnIdle {
                val row = (layout("LEFT") as TableLayout).getChildAt(0) as TableRow
                assertEquals(
                    String.format(text.cell, 1, 1),
                    (row.getChildAt(0) as TextView).text.toString(),
                )
            }
        }
        changeLanguage(AppLanguage.ENGLISH)
    }

    private fun configure(component: LabComponent, left: DesignFamily, right: DesignFamily) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(component.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
        chooseFamily("LEFT", left)
        chooseFamily("RIGHT", right)
        setEnabled(true)
        resetSamples()
    }

    private fun chooseFamily(panel: String, family: DesignFamily) {
        compose.onNodeWithTag("family_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performScrollTo().performClick()
    }

    private fun nativeId(panel: String) =
        if (panel == "LEFT") R.id.sample_left else R.id.sample_right

    private fun layout(panel: String): View = compose.activity.findViewById(nativeId(panel))

    private fun assertNativeIdentity(panel: String, component: LabComponent, family: DesignFamily) {
        compose
            .onNodeWithTag("source_$panel")
            .performScrollTo()
            .assertTextEquals(component.platformSource!!)
        compose
            .onNodeWithTag("implementation_$panel")
            .performScrollTo()
            .assertTextContains(
                "android:${requireNotNull(family.platform).themeName}",
                substring = true,
            )
            .assertTextContains(
                compose.activity.getString(R.string.widget_api, component.minimumApi),
                substring = true,
            )
        compose.runOnIdle {
            val view = layout(panel)
            assertEquals(ContextThemeWrapper::class.java, view.context.javaClass)
            val expected = TypedValue()
            val actual = TypedValue()
            val theme =
                ContextThemeWrapper(compose.activity, requireNotNull(family.platform).themeId).theme
            assertEquals(
                theme.resolveAttribute(android.R.attr.colorBackground, expected, true),
                view.context.theme.resolveAttribute(android.R.attr.colorBackground, actual, true),
            )
            assertEquals(expected.resourceId, actual.resourceId)
        }
    }

    private fun status(panel: String, expected: String) {
        compose.onNodeWithTag("status_$panel").performScrollTo().assertTextEquals(expected)
    }

    private fun blockedCopy(direction: String, reason: String) {
        touchTag("copy_inputs")
        compose
            .onNodeWithTag("copy_reason_$direction", useUnmergedTree = true)
            .assertTextEquals(reason)
        compose
            .onNodeWithTag("copy_setup_$direction")
            .performScrollTo()
            .assertIsNotEnabled()
            .performTouchInput { click() }
        compose.onNodeWithTag("copy_inputs_menu").assertIsDisplayed()
        pressBack()
    }

    private fun setEnabled(enabled: Boolean) {
        val control = compose.onNodeWithTag("enabled").performScrollTo().assertIsDisplayed()
        if (
            (control.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] ==
                ToggleableState.On) != enabled
        )
            control.performTouchInput { click() }
        if (enabled) control.assertIsOn() else control.assertIsOff()
    }

    private fun resetSamples() = touchTag("reset")

    private fun touchTag(tag: String) {
        compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performTouchInput {
            click()
        }
    }

    private fun changeLanguage(language: AppLanguage) {
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("language_${language.name}").performScrollTo().performClick()
        compose.waitUntil(10_000) { LanguagePreferences.read(compose.activity) == language }
        compose.waitForIdle()
        recreateActivity()
    }

    private fun recreateActivity() {
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        keepScreenOn()
    }

    private fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private data class LayoutNames(
        val language: AppLanguage,
        val preview: String,
        val tableLabel: String,
        val cell: String,
        val note: String,
    )

    private val names =
        listOf(
            LayoutNames(
                AppLanguage.ENGLISH,
                "Preview",
                "Table layout",
                "R%1\$d C%2\$d",
                "A static preview of the real container and its themed children. The layout has no inputs to copy.",
            ),
            LayoutNames(
                AppLanguage.KOREAN,
                "미리보기",
                "테이블 레이아웃",
                "R%1\$d C%2\$d",
                "실제 컨테이너와 테마의 자식들을 보여주는 정적 미리보기입니다. 레이아웃에는 복사할 입력이 없습니다.",
            ),
            LayoutNames(
                AppLanguage.JAPANESE,
                "プレビュー",
                "テーブルレイアウト",
                "R%1\$d C%2\$d",
                "実際のコンテナーとテーマの子を示す静的プレビューです。レイアウトにコピーする入力はありません。",
            ),
            LayoutNames(
                AppLanguage.SIMPLIFIED_CHINESE,
                "预览",
                "表格布局",
                "R%1\$d C%2\$d",
                "展示真实容器及其主题子项的静态预览。布局没有可复制的输入。",
            ),
            LayoutNames(
                AppLanguage.TRADITIONAL_CHINESE,
                "預覽",
                "表格版面配置",
                "R%1\$d C%2\$d",
                "展示真實容器及其主題子項的靜態預覽。版面配置沒有可複製的輸入。",
            ),
        )
}
