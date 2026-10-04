package xyz.gaon.componentory.lab

import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.InputDevice
import android.view.MenuItem
import android.view.MotionEvent
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.PopupMenu
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
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
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click as nativeClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Description
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher
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

@RunWith(AndroidJUnit4::class)
class PopupMenusTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
    private val platformFamilies =
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
    private val libraryFamilies = listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
    private val allFamilies = platformFamilies + libraryFamilies
    private val english =
        MenuNames(
            AppLanguage.ENGLISH,
            "Open menu",
            "Option A",
            "Option B",
            "Disabled option",
            "No selection",
            "Not opened",
            "Opened",
            "Selected",
            "Dismissed",
            "Choice: {choice} · Last action: {action}",
        )
    private var names = english

    @Before
    fun prepareEnglishComparison() {
        keepScreenOn()
        changeLanguage(english)
        compose.onNodeWithTag("nav_compare").performClick()
        chooseFamily("RIGHT", DesignFamily.MATERIAL3)
        chooseMenuComponent()
    }

    @After
    fun restoreEnglishBaseline() {
        compose.runOnUiThread { LanguagePreferences.apply(compose.activity, AppLanguage.ENGLISH) }
        compose.waitForIdle()
        keepScreenOn()
    }

    @Test
    fun frameworkThemesUseExactPopupMenusAndRealItemSelectionKeepsTheSelectedAction() {
        platformFamilies.forEach { family ->
            chooseFamily("LEFT", family)
            resetSamples()
            assertIdentity("LEFT", family)
            feedback("LEFT", 0, MenuAction.NOT_OPENED)
            openMenu("LEFT", family)
            assertNativePopup("LEFT")
            selectItem("LEFT", family, 1)
            feedback("LEFT", 1, MenuAction.SELECTED)
            feedback("RIGHT", 0, MenuAction.NOT_OPENED)
            openMenu("LEFT", family)
            selectItem("LEFT", family, 2)
            // Framework selection also dismisses the popup. Its dismiss callback must not win.
            feedback("LEFT", 2, MenuAction.SELECTED)
            feedback("RIGHT", 0, MenuAction.NOT_OPENED)
        }
    }

    @Test
    fun bothLibrariesRenderNamedOriginalItemsWithoutInventedRolesAndSelectByPointer() {
        libraryFamilies.forEach { family ->
            chooseFamily("LEFT", family)
            resetSamples()
            assertIdentity("LEFT", family)
            openMenu("LEFT", family)
            listOf("A" to names.a, "B" to names.b).forEach { (id, label) ->
                compose
                    .onNodeWithTag("menu_item_LEFT_$id")
                    .performScrollTo()
                    .assertTextEquals(label)
                    .assertHasClickAction()
                    .assertIsEnabled()
                    .assertIsDisplayed()
                    .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
            }
            selectItem("LEFT", family, 1)
            feedback("LEFT", 1, MenuAction.SELECTED)
            openMenu("LEFT", family)
            selectItem("LEFT", family, 2)
            feedback("LEFT", 2, MenuAction.SELECTED)
            feedback("RIGHT", 0, MenuAction.NOT_OPENED)
        }
    }

    @Test
    fun disabledRowsAndLaunchersIgnorePointersAndHostDisableClosesWithoutAUserDismissal() {
        allFamilies.forEach { family ->
            chooseFamily("LEFT", family)
            resetSamples()
            openMenu("LEFT", family)
            touchDisabledItem("LEFT", family)
            assertMenuOpen("LEFT", family)
            feedback("LEFT", 0, MenuAction.OPENED)
            assertLastItemInsideMeasuredWindow(family)
            pressBack()
            assertNoMenu("LEFT", family)
            feedback("LEFT", 0, MenuAction.DISMISSED)

            setEnabled(false)
            assertDisabledLauncher("LEFT", family)
            touchLauncher("LEFT", family)
            assertNoMenu("LEFT", family)
            feedback("LEFT", 0, MenuAction.DISMISSED)
            setEnabled(true)
            openMenu("LEFT", family)
            selectItem("LEFT", family, 2)
            feedback("LEFT", 2, MenuAction.SELECTED)

            openMenu("LEFT", family)
            // A focusable popup intercepts background touches. Dispatch the real host action to
            // verify cleanup separately from pointer proof, without changing private sample state.
            compose.onNodeWithTag("enabled").assertIsOn().performClick().assertIsOff()
            assertNoMenu("LEFT", family)
            feedback("LEFT", 2, MenuAction.OPENED)
            setEnabled(true)
            openMenu("LEFT", family)
            selectItem("LEFT", family, 1)
            feedback("LEFT", 1, MenuAction.SELECTED)
        }
    }

    @Test
    fun actualBackAndMeasuredOutsideTouchDismissMenusWithoutChangingTheChosenItem() {
        allFamilies.forEach { family ->
            chooseFamily("LEFT", family)
            resetSamples()
            openMenu("LEFT", family)
            selectItem("LEFT", family, 2)
            openMenu("LEFT", family)
            pressBack()
            assertNoMenu("LEFT", family)
            feedback("LEFT", 2, MenuAction.DISMISSED)

            openMenu("LEFT", family)
            touchOutsideActualPopup()
            assertNoMenu("LEFT", family)
            feedback("LEFT", 2, MenuAction.DISMISSED)
            feedback("RIGHT", 0, MenuAction.NOT_OPENED)
            openMenu("LEFT", family)
            selectItem("LEFT", family, 1)
            feedback("LEFT", 1, MenuAction.SELECTED)
        }
    }

    @Test
    fun equalProviderPanelsKeepSeparateChoicesAcrossRecreationDisposalAndReset() {
        allFamilies.forEach { family ->
            chooseFamily("LEFT", family)
            chooseFamily("RIGHT", family)
            resetSamples()
            openMenu("LEFT", family)
            selectItem("LEFT", family, 1)
            openMenu("RIGHT", family)
            selectItem("RIGHT", family, 2)
            feedback("LEFT", 1, MenuAction.SELECTED)
            feedback("RIGHT", 2, MenuAction.SELECTED)
            openMenu("LEFT", family)
            recreateActivity()
            assertNoMenu("LEFT", family)
            feedback("LEFT", 1, MenuAction.OPENED)
            feedback("RIGHT", 2, MenuAction.SELECTED)

            openMenu("LEFT", family)
            // Navigation dispatch exercises disposal while the popup owns focus.
            compose.onNodeWithTag("nav_settings").performClick()
            compose.onNodeWithTag("nav_compare").performClick()
            assertNoMenu("LEFT", family)
            feedback("LEFT", 1, MenuAction.OPENED)
            feedback("RIGHT", 2, MenuAction.SELECTED)
            resetSamples()
            feedback("LEFT", 0, MenuAction.NOT_OPENED)
            feedback("RIGHT", 0, MenuAction.NOT_OPENED)
            openMenu("RIGHT", family)
            selectItem("RIGHT", family, 1)
            feedback("LEFT", 0, MenuAction.NOT_OPENED)
            feedback("RIGHT", 1, MenuAction.SELECTED)
        }
    }

    @Test
    fun fiveSettingsLanguagesRenderActualPopupChoicesAndLocalizedSelectionFeedback() {
        localizedNames().forEach { language ->
            changeLanguage(language)
            compose.onNodeWithTag("nav_compare").performClick()
            chooseFamily("RIGHT", DesignFamily.MATERIAL3)
            chooseMenuComponent()
            listOf(DesignFamily.MATERIAL, DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach {
                family ->
                chooseFamily("LEFT", family)
                resetSamples()
                assertIdentity("LEFT", family)
                feedback("LEFT", 0, MenuAction.NOT_OPENED)
                openMenu("LEFT", family)
                touchDisabledItem("LEFT", family)
                feedback("LEFT", 0, MenuAction.OPENED)
                selectItem("LEFT", family, 1)
                feedback("LEFT", 1, MenuAction.SELECTED)
                openMenu("LEFT", family)
                selectItem("LEFT", family, 2)
                feedback("LEFT", 2, MenuAction.SELECTED)
                feedback("RIGHT", 0, MenuAction.NOT_OPENED)
                openMenu("LEFT", family)
                pressBack()
                assertNoMenu("LEFT", family)
                feedback("LEFT", 2, MenuAction.DISMISSED)
            }
        }
    }

    private fun chooseMenuComponent() {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(LabComponent.POPUP_MENU.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_POPUP_MENU"))
        compose.onNodeWithTag("component_POPUP_MENU").performClick()
    }

    private fun chooseFamily(panel: String, family: DesignFamily) {
        compose.onNodeWithTag("family_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performScrollTo().performClick()
    }

    private fun assertIdentity(panel: String, family: DesignFamily) {
        compose.onNodeWithTag("unsupported_$panel").assertDoesNotExist()
        if (family.platform != null) {
            val platform = requireNotNull(family.platform)
            compose.onNodeWithTag("source_$panel").assertTextEquals("android.widget.PopupMenu")
            compose
                .onNodeWithTag("implementation_$panel")
                .assertTextContains("android:${platform.themeName}")
            compose.onNodeWithTag("library_$panel").assertDoesNotExist()
            compose.onNodeWithTag("menu_item_source_$panel").assertDoesNotExist()
            compose.onNodeWithTag("native_$panel").performScrollTo().assertIsDisplayed()
            compose.runOnIdle {
                val launcher = compose.activity.findViewById<Button>(nativeId(panel))
                assertEquals(Button::class.java, launcher.javaClass)
                assertEquals(ContextThemeWrapper::class.java, launcher.context.javaClass)
                val expectedTheme = ContextThemeWrapper(compose.activity, platform.themeId).theme
                listOf(android.R.attr.buttonStyle, android.R.attr.popupMenuStyle).forEach {
                    attribute ->
                    val expected = TypedValue()
                    val actual = TypedValue()
                    assertTrue(expectedTheme.resolveAttribute(attribute, expected, true))
                    assertTrue(launcher.context.theme.resolveAttribute(attribute, actual, true))
                    assertEquals(expected.resourceId, actual.resourceId)
                }
            }
        } else {
            val material2 = family == DesignFamily.MATERIAL2
            val packageName =
                if (material2) "androidx.compose.material" else "androidx.compose.material3"
            val dependency =
                if (material2) {
                    "androidx.compose.material:material:${BuildConfig.MATERIAL2_VERSION}"
                } else {
                    "androidx.compose.material3:material3:${BuildConfig.MATERIAL3_VERSION}"
                }
            compose.onNodeWithTag("source_$panel").assertTextEquals("$packageName.DropdownMenu")
            compose
                .onNodeWithTag("menu_item_source_$panel")
                .assertTextEquals("$packageName.DropdownMenuItem")
            compose.onNodeWithTag("implementation_$panel").assertTextContains(dependency)
            compose.onNodeWithTag("native_$panel").assertDoesNotExist()
        }
    }

    private fun assertNativePopup(panel: String) {
        compose.runOnIdle {
            val popup =
                requireNotNull(
                    compose.activity.findViewById<Button>(nativeId(panel)).tag as? PopupMenu
                )
            assertEquals(PopupMenu::class.java, popup.javaClass)
            assertEquals(3, popup.menu.size())
            assertEquals(names.a, popup.menu.findItem(1).title.toString())
            assertEquals(names.b, popup.menu.findItem(2).title.toString())
            assertTrue(popup.menu.findItem(1).isEnabled)
            assertTrue(popup.menu.findItem(2).isEnabled)
            assertEquals(names.disabled, popup.menu.findItem(3).title.toString())
            assertEquals(false, popup.menu.findItem(3).isEnabled)
        }
    }

    private fun touchLauncher(panel: String, family: DesignFamily) {
        if (family.platform != null) {
            compose.onNodeWithTag("native_$panel").performScrollTo().assertIsDisplayed()
            onView(withId(nativeId(panel))).perform(nativeClick())
        } else {
            compose
                .onNodeWithTag("library_$panel")
                .performScrollTo()
                .assertIsDisplayed()
                .performTouchInput { click() }
        }
    }

    private fun openMenu(panel: String, family: DesignFamily) {
        if (family.platform != null) {
            compose.runOnIdle {
                assertEquals(
                    names.open,
                    compose.activity.findViewById<Button>(nativeId(panel)).text.toString(),
                )
            }
        } else {
            compose
                .onNodeWithTag("library_$panel")
                .assertTextEquals(names.open)
                .assertIsEnabled()
                .assertHasClickAction()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        }
        touchLauncher(panel, family)
        assertMenuOpen(panel, family)
        feedback(panel, -1, MenuAction.OPENED)
    }

    private fun assertMenuOpen(panel: String, family: DesignFamily) {
        if (family.platform != null) {
            waitForNode("visible framework popup item") { it.text?.toString() in names.itemLabels }
        } else {
            compose.onNodeWithTag("menu_$panel").assertIsDisplayed()
            compose.onNodeWithTag("menu_item_${panel}_A").assertExists().assertTextEquals(names.a)
        }
    }

    private fun selectItem(panel: String, family: DesignFamily, choice: Int) {
        val label = if (choice == 1) names.a else names.b
        if (family.platform != null) {
            onData(nativeItem(choice, label))
                .inRoot(isPlatformPopup())
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .perform(nativeClick())
        } else {
            compose
                .onNodeWithTag("menu_item_${panel}_${if (choice == 1) "A" else "B"}")
                .performScrollTo()
                .assertIsDisplayed()
                .assertTextEquals(label)
                .assertIsEnabled()
                .performTouchInput { click() }
        }
        assertNoMenu(panel, family)
    }

    private fun touchDisabledItem(panel: String, family: DesignFamily) {
        if (family.platform != null) {
            onData(nativeItem(3, names.disabled))
                .inRoot(isPlatformPopup())
                .check(matches(isDisplayed()))
                .check(matches(not(isEnabled())))
                .perform(nativeClick())
        } else {
            compose
                .onNodeWithTag("menu_item_${panel}_DISABLED")
                .performScrollTo()
                .assertIsDisplayed()
                .assertTextEquals(names.disabled)
                .assertIsNotEnabled()
                .assertHasClickAction()
                .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
                .performTouchInput { click() }
        }
    }

    private fun assertDisabledLauncher(panel: String, family: DesignFamily) {
        if (family.platform != null) {
            onView(withId(nativeId(panel))).check(matches(not(isEnabled())))
        } else {
            compose.onNodeWithTag("library_$panel").assertIsNotEnabled()
        }
    }

    private fun assertNoMenu(panel: String, family: DesignFamily) {
        compose.waitForIdle()
        if (family.platform != null) {
            compose.waitUntil(10_000) {
                findNode(automation.rootInActiveWindow) {
                    it.text?.toString() in names.itemLabels
                } == null
            }
        } else {
            compose.onNodeWithTag("menu_$panel").assertDoesNotExist()
        }
    }

    private fun assertLastItemInsideMeasuredWindow(family: DesignFamily) {
        val row = waitForNode(names.disabled) { it.text?.toString() == names.disabled }
        val rowBounds = Rect().also { row.getBoundsInScreen(it) }
        val popupBounds = actualPopupBounds()
        val available = visibleWindowFrame()
        assertTrue("Last original row has empty bounds: $rowBounds", !rowBounds.isEmpty)
        assertTrue(
            "Last row $rowBounds is outside visible window $available",
            available.contains(rowBounds),
        )
        assertTrue(
            "Last row $rowBounds is outside actual popup $popupBounds",
            popupBounds.contains(rowBounds),
        )
        // These are actual window measurements, not parent-content constraints or compact claims.
        InstrumentationRegistry.getInstrumentation()
            .sendStatus(
                2,
                Bundle().apply {
                    putString(
                        "stream",
                        "PopupMenusTest: provider=${family.name} window=$available popup=$popupBounds lastItem=$rowBounds\n",
                    )
                },
            )
    }

    private fun touchOutsideActualPopup() {
        val popup = actualPopupBounds()
        val available = visibleWindowFrame()
        val inset = compose.activity.resources.displayMetrics.density * 32f
        val candidates =
            listOf(
                PointF(available.exactCenterX(), available.top + inset),
                PointF(available.exactCenterX(), available.bottom - inset),
                PointF(available.left + inset, available.exactCenterY()),
                PointF(available.right - inset, available.exactCenterY()),
            )
        val point =
            requireNotNull(
                candidates.firstOrNull {
                    available.contains(it.x.toInt(), it.y.toInt()) &&
                        !popup.contains(it.x.toInt(), it.y.toInt())
                }
            ) {
                "No safe point outside measured popup $popup in $available"
            }
        injectScreenTouch(point)
    }

    private fun actualPopupBounds(): Rect {
        waitForNode("visible popup item") { it.text?.toString() in names.itemLabels }
        val root = requireNotNull(automation.rootInActiveWindow)
        assertTrue(
            "Active window is not the menu",
            findNode(root) { it.text?.toString() in names.itemLabels } != null,
        )
        return Rect().also { root.getBoundsInScreen(it) }
    }

    private fun visibleWindowFrame(): Rect {
        val frame = Rect()
        compose.runOnIdle { compose.activity.window.decorView.getWindowVisibleDisplayFrame(frame) }
        assertTrue("Visible window frame is empty", !frame.isEmpty)
        return frame
    }

    private fun injectScreenTouch(point: PointF) {
        val start = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
            val event =
                MotionEvent.obtain(
                    start,
                    start + if (action == MotionEvent.ACTION_UP) 50 else 0,
                    action,
                    point.x,
                    point.y,
                    0,
                )
            event.source = InputDevice.SOURCE_TOUCHSCREEN
            try {
                assertTrue(automation.injectInputEvent(event, true))
            } finally {
                event.recycle()
            }
        }
    }

    private fun waitForNode(
        description: String,
        predicate: (AccessibilityNodeInfo) -> Boolean,
    ): AccessibilityNodeInfo {
        val end = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < end) {
            findNode(automation.rootInActiveWindow, predicate)?.let {
                return it
            }
            SystemClock.sleep(50)
        }
        error("The actual popup node did not appear: $description")
    }

    private fun findNode(
        node: AccessibilityNodeInfo?,
        predicate: (AccessibilityNodeInfo) -> Boolean,
    ): AccessibilityNodeInfo? {
        if (node == null) return null
        if (predicate(node)) return node
        for (index in 0 until node.childCount) {
            findNode(node.getChild(index), predicate)?.let {
                return it
            }
        }
        return null
    }

    private fun nativeItem(id: Int, label: String) =
        object : TypeSafeMatcher<MenuItem>() {
            override fun describeTo(description: Description) {
                description.appendText("real framework menu item $id with title $label")
            }

            override fun matchesSafely(item: MenuItem): Boolean =
                item.itemId == id && item.title.toString() == label
        }

    private fun feedback(panel: String, choice: Int, action: MenuAction) {
        val result = compose.onNodeWithTag("status_$panel")
        if (choice < 0) {
            result.assertTextContains(names.action(action))
        } else {
            val label =
                when (choice) {
                    1 -> names.a
                    2 -> names.b
                    else -> names.none
                }
            result.assertTextEquals(
                names.feedback.replace("{choice}", label).replace("{action}", names.action(action))
            )
        }
    }

    private fun setEnabled(enabled: Boolean) {
        val control = compose.onNodeWithTag("enabled").performScrollTo().assertIsDisplayed()
        if (enabled) control.assertIsOff() else control.assertIsOn()
        control.performTouchInput { click() }
        if (enabled) control.assertIsOn() else control.assertIsOff()
    }

    private fun resetSamples() {
        compose.onNodeWithTag("reset").performScrollTo().performClick()
    }

    private fun recreateActivity() {
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        keepScreenOn()
    }

    private fun changeLanguage(language: MenuNames) {
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("language_${language.language.name}").performScrollTo().performClick()
        compose.waitUntil(10_000) {
            LanguagePreferences.read(compose.activity) == language.language
        }
        compose.waitForIdle()
        recreateActivity()
        names = language
    }

    private fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun nativeId(panel: String) =
        if (panel == "LEFT") R.id.sample_left else R.id.sample_right

    private fun localizedNames(): List<MenuNames> =
        listOf(
            english,
            MenuNames(
                AppLanguage.KOREAN,
                "메뉴 열기",
                "옵션 A",
                "옵션 B",
                "비활성 항목",
                "선택 없음",
                "아직 열지 않음",
                "열림",
                "선택됨",
                "닫힘",
                "선택 항목: {choice} · 마지막 동작: {action}",
            ),
            MenuNames(
                AppLanguage.JAPANESE,
                "メニューを開く",
                "選択肢 A",
                "選択肢 B",
                "無効な項目",
                "選択なし",
                "未表示",
                "開きました",
                "選択済み",
                "閉じました",
                "選択項目: {choice} · 最後の操作: {action}",
            ),
            MenuNames(
                AppLanguage.SIMPLIFIED_CHINESE,
                "打开菜单",
                "选项 A",
                "选项 B",
                "禁用选项",
                "未选择",
                "尚未打开",
                "已打开",
                "已选择",
                "已关闭",
                "所选项: {choice} · 上次操作: {action}",
            ),
            MenuNames(
                AppLanguage.TRADITIONAL_CHINESE,
                "開啟選單",
                "選項 A",
                "選項 B",
                "停用選項",
                "未選取",
                "尚未開啟",
                "已開啟",
                "已選取",
                "已關閉",
                "所選項: {choice} · 上次操作: {action}",
            ),
        )

    private enum class MenuAction {
        NOT_OPENED,
        OPENED,
        SELECTED,
        DISMISSED,
    }

    private data class MenuNames(
        val language: AppLanguage,
        val open: String,
        val a: String,
        val b: String,
        val disabled: String,
        val none: String,
        val notOpened: String,
        val opened: String,
        val selected: String,
        val dismissed: String,
        val feedback: String,
    ) {
        val itemLabels
            get() = listOf(a, b, disabled)

        fun action(action: MenuAction): String =
            when (action) {
                MenuAction.NOT_OPENED -> notOpened
                MenuAction.OPENED -> opened
                MenuAction.SELECTED -> selected
                MenuAction.DISMISSED -> dismissed
            }
    }
}
