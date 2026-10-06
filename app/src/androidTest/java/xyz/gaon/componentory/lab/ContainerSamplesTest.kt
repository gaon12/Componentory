package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences

@RunWith(AndroidJUnit4::class)
class ContainerSamplesTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private val components =
        listOf(
            ContainerCase(LabComponent.CARD, "Card", "Card"),
            ContainerCase(LabComponent.ELEVATED_CARD, "Elevated card", "ElevatedCard"),
            ContainerCase(LabComponent.OUTLINED_CARD, "Outlined card", "OutlinedCard"),
            ContainerCase(LabComponent.SURFACE, "Surface", "Surface"),
        )
    private val supported =
        listOf(
            SupportedCase(components[0], DesignFamily.MATERIAL2),
            SupportedCase(components[0], DesignFamily.MATERIAL3),
            SupportedCase(components[1], DesignFamily.MATERIAL3),
            SupportedCase(components[2], DesignFamily.MATERIAL3),
            SupportedCase(components[3], DesignFamily.MATERIAL2),
            SupportedCase(components[3], DesignFamily.MATERIAL3),
        )

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
    fun everySupportedLibraryContainerRendersItsIdentityAndCountsIndependentPointerClicks() {
        supported.forEach { selected ->
            selectSample(selected)
            assertIdentity("LEFT", selected)
            assertIdentity("RIGHT", selected)
            assertClickableConfiguration("LEFT", true)
            assertClickableConfiguration("RIGHT", true)
            assertOriginalContent("LEFT", selected.container.label, "Sample content")
            assertOriginalContent("RIGHT", selected.container.label, "Sample content")
            assertClickableSample("LEFT")
            assertClickableSample("RIGHT")
            count("LEFT", 0)
            count("RIGHT", 0)

            touchSample("LEFT")
            touchSample("LEFT")
            count("LEFT", 2)
            count("RIGHT", 0)
            touchSample("RIGHT")
            count("RIGHT", 1)
            count("LEFT", 2)
        }
    }

    @Test
    fun nonClickableOverloadsExposeNoClickActionAndNeverInvokeTheCounterCallback() {
        supported.forEach { selected ->
            selectSample(selected)
            touchSample("LEFT")
            count("LEFT", 1)
            setClickable("LEFT", false)
            assertClickableConfiguration("LEFT", false)
            assertPlainSample("LEFT")
            assertOriginalContent(
                "LEFT",
                selected.container.label,
                "Sample content",
                clickable = false,
            )
            touchSample("LEFT")
            count("LEFT", 1)
            count("RIGHT", 0)
            assertClickableConfiguration("RIGHT", true)
            touchSample("RIGHT")
            count("RIGHT", 1)

            setClickable("LEFT", true)
            assertClickableSample("LEFT")
            touchSample("LEFT")
            count("LEFT", 2)
            count("RIGHT", 1)
        }
    }

    @Test
    fun globalDisableStopsClickableContainersAndConfigurationWithoutDecoratingPlainOverloads() {
        supported.forEach { selected ->
            selectSample(selected)
            touchSample("LEFT")
            setGloballyEnabled(false)
            listOf("LEFT", "RIGHT").forEach { panel ->
                sample(panel)
                    .assertHasClickAction()
                    .assertIsNotEnabled()
                    .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
                touchSample(panel)
                clickableControl(panel)
                    .assertIsNotEnabled()
                    .assertIsOn()
                    .performScrollTo()
                    .assertIsDisplayed()
                    .performTouchInput { click() }
                    .assertIsOn()
            }
            count("LEFT", 1)
            count("RIGHT", 0)
            setGloballyEnabled(true)
            setClickable("LEFT", false)
            setGloballyEnabled(false)
            assertPlainSample("LEFT")
            touchSample("LEFT")
            count("LEFT", 1)
            clickableControl("LEFT").assertIsNotEnabled().assertIsOff()

            setGloballyEnabled(true)
            setClickable("LEFT", true)
            touchSample("LEFT")
            touchSample("RIGHT")
            count("LEFT", 2)
            count("RIGHT", 1)
        }
    }

    @Test
    fun sameProviderPanelsRestoreSeparateModesAndCountsThenResetToClickableZero() {
        components.forEach { container ->
            val selected = SupportedCase(container, DesignFamily.MATERIAL3)
            selectSample(selected)
            touchSample("LEFT")
            touchSample("LEFT")
            touchSample("RIGHT")
            setClickable("LEFT", false)
            recreateActivity()
            assertIdentity("LEFT", selected)
            assertIdentity("RIGHT", selected)
            assertClickableConfiguration("LEFT", false)
            assertClickableConfiguration("RIGHT", true)
            assertPlainSample("LEFT")
            assertClickableSample("RIGHT")
            count("LEFT", 2)
            count("RIGHT", 1)
            touchSample("LEFT")
            touchSample("RIGHT")
            count("LEFT", 2)
            count("RIGHT", 2)

            resetSamples()
            listOf("LEFT", "RIGHT").forEach { panel ->
                assertClickableConfiguration(panel, true)
                assertClickableSample(panel)
                count(panel, 0)
            }
            touchSample("LEFT")
            count("LEFT", 1)
            count("RIGHT", 0)
        }
    }

    @Test
    fun unsupportedProvidersShowExplicitAbsenceAndRecoverToTheActualMaterial3Sample() {
        val unavailable =
            components.flatMap { container ->
                listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).map {
                    SupportedCase(container, it)
                }
            } +
                listOf(
                    SupportedCase(components[1], DesignFamily.MATERIAL2),
                    SupportedCase(components[2], DesignFamily.MATERIAL2),
                )
        unavailable.forEach { selected ->
            chooseComponent(selected.container.component)
            chooseFamily("LEFT", selected.family)
            chooseFamily("RIGHT", DesignFamily.MATERIAL3)
            resetSamples()
            compose.onNodeWithTag("unsupported_LEFT").performScrollTo().assertIsDisplayed()
            val reason =
                if (selected.family == DesignFamily.MATERIAL2) {
                    "The Material 2 library does not provide ${selected.container.label}."
                } else {
                    "The Android platform does not provide a dedicated ${selected.container.label} component."
                }
            compose.onNodeWithText(reason).assertExists()
            expandDetails("LEFT")
            compose.onNodeWithTag("source_LEFT").assertTextEquals("Not provided")
            compose.onNodeWithTag("library_LEFT").assertDoesNotExist()
            compose.onNodeWithTag("native_LEFT").assertDoesNotExist()
            compose.onNodeWithTag("status_LEFT").assertDoesNotExist()
            compose.onNodeWithTag("container_clickable_LEFT").assertDoesNotExist()
            compose.onNodeWithTag("container_note_LEFT").assertDoesNotExist()
            compose.onNodeWithTag("container_overload_LEFT").assertDoesNotExist()

            chooseFamily("LEFT", DesignFamily.MATERIAL3)
            val recovered = SupportedCase(selected.container, DesignFamily.MATERIAL3)
            assertIdentity("LEFT", recovered)
            compose.onNodeWithTag("unsupported_LEFT").assertDoesNotExist()
            assertClickableConfiguration("LEFT", true)
            assertClickableSample("LEFT")
            count("LEFT", 0)
            count("RIGHT", 0)
            touchSample("LEFT")
            count("LEFT", 1)
            count("RIGHT", 0)
        }
    }

    @Test
    fun fiveSettingsLanguagesNameTheRealHostSwitchAndShowLocalizedContainerCounts() {
        val languages =
            listOf(
                LocalizedContainer(
                    AppLanguage.ENGLISH,
                    "Clickable container",
                    "Clicks: ",
                    "Sample content",
                    listOf("Card", "Elevated card", "Outlined card", "Surface"),
                ),
                LocalizedContainer(
                    AppLanguage.KOREAN,
                    "누를 수 있는 컨테이너",
                    "클릭 횟수: ",
                    "샘플 내용",
                    listOf("카드", "높이가 있는 카드", "테두리 카드", "서피스"),
                ),
                LocalizedContainer(
                    AppLanguage.JAPANESE,
                    "クリック可能なコンテナ",
                    "クリック回数: ",
                    "サンプルの内容",
                    listOf("カード", "浮き上がったカード", "枠線付きカード", "サーフェス"),
                ),
                LocalizedContainer(
                    AppLanguage.SIMPLIFIED_CHINESE,
                    "可点击容器",
                    "点击次数：",
                    "示例内容",
                    listOf("卡片", "浮起卡片", "描边卡片", "表面"),
                ),
                LocalizedContainer(
                    AppLanguage.TRADITIONAL_CHINESE,
                    "可點擊容器",
                    "點擊次數：",
                    "範例內容",
                    listOf("卡片", "浮起卡片", "外框卡片", "表面"),
                ),
            )
        languages.forEach { names ->
            changeLanguage(names.language)
            compose.onNodeWithTag("nav_compare").performClick()
            components.forEachIndexed { index, container ->
                val selected = SupportedCase(container, DesignFamily.MATERIAL3)
                selectSample(selected)
                assertIdentity("LEFT", selected)
                assertOriginalContent("LEFT", names.titles[index], names.content)
                clickableControl("LEFT")
                    .assertContentDescriptionEquals(names.clickable)
                    .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
                    .assertIsOn()
                    .assertHasClickAction()
                touchSample("LEFT")
                count("LEFT", 1, names.countPrefix)
                count("RIGHT", 0, names.countPrefix)
                setClickable("LEFT", false)
                assertPlainSample("LEFT")
                touchSample("LEFT")
                count("LEFT", 1, names.countPrefix)
                clickableControl("LEFT")
                    .assertContentDescriptionEquals(names.clickable)
                    .assertIsOff()
                setClickable("LEFT", true)
                touchSample("LEFT")
                count("LEFT", 2, names.countPrefix)
                count("RIGHT", 0, names.countPrefix)
            }
        }
    }

    private fun selectSample(selected: SupportedCase) {
        chooseComponent(selected.container.component)
        chooseFamily("LEFT", selected.family)
        chooseFamily("RIGHT", selected.family)
        resetSamples()
    }

    private fun chooseComponent(component: LabComponent) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(component.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
    }

    private fun chooseFamily(panel: String, family: DesignFamily) {
        compose.onNodeWithTag("family_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performScrollTo().performClick()
    }

    private fun assertIdentity(panel: String, selected: SupportedCase) {
        val material2 = selected.family == DesignFamily.MATERIAL2
        val packageName =
            if (material2) "androidx.compose.material" else "androidx.compose.material3"
        val dependency =
            if (material2) {
                "androidx.compose.material:material:${BuildConfig.MATERIAL2_VERSION}"
            } else {
                "androidx.compose.material3:material3:${BuildConfig.MATERIAL3_VERSION}"
            }
        expandDetails(panel)
        compose
            .onNodeWithTag("source_$panel")
            .assertTextEquals("$packageName.${selected.container.function}")
        expandDetails(panel)
        compose
            .onNodeWithTag("implementation_$panel")
            .assertTextContains(dependency, substring = true)
        compose.onNodeWithTag("native_$panel").assertDoesNotExist()
        compose.onNodeWithTag("unsupported_$panel").assertDoesNotExist()
        sample(panel).performScrollTo().assertIsDisplayed()
    }

    private fun assertOriginalContent(
        panel: String,
        title: String,
        content: String,
        clickable: Boolean = true,
    ) {
        listOf(title, content).forEach { text ->
            compose
                .onNode(
                    hasText(text) and hasAnyAncestor(hasTestTag("library_$panel")),
                    useUnmergedTree = true,
                )
                .assertExists()
        }
        if (clickable) {
            sample(panel)
                .assertTextContains(title, substring = true)
                .assertTextContains(content, substring = true)
        }
    }

    private fun assertClickableSample(panel: String) {
        // The pinned Card and Surface overloads expose clicks without adding a default Role.
        sample(panel)
            .assertHasClickAction()
            .assertIsEnabled()
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
    }

    private fun assertPlainSample(panel: String) {
        // Plain provider overloads do not accept enabled, so host state must not invent it.
        sample(panel)
            .assertHasNoClickAction()
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Disabled))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
    }

    private fun assertClickableConfiguration(panel: String, clickable: Boolean) {
        val control = clickableControl(panel)
        control
            .assertContentDescriptionEquals("Clickable container")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
        if (clickable) control.assertIsOn() else control.assertIsOff()
        compose
            .onNodeWithTag("container_overload_$panel")
            .assertTextEquals(
                if (clickable) "Tap the container to increase the counter."
                else "Read-only container. Taps do not change the counter."
            )
        compose.onNodeWithTag("container_note_$panel").assertExists()
    }

    private fun setClickable(panel: String, clickable: Boolean) {
        val control =
            clickableControl(panel).performScrollTo().assertIsDisplayed().assertIsEnabled()
        if (clickable) control.assertIsOff() else control.assertIsOn()
        control.performTouchInput { click() }
        if (clickable) control.assertIsOn() else control.assertIsOff()
    }

    private fun clickableControl(panel: String): SemanticsNodeInteraction =
        compose.onNodeWithTag("container_clickable_$panel")

    private fun setGloballyEnabled(enabled: Boolean) {
        val control = compose.onNodeWithTag("enabled").performScrollTo().assertIsDisplayed()
        if (enabled) control.assertIsOff() else control.assertIsOn()
        control.performTouchInput { click() }
        if (enabled) control.assertIsOn() else control.assertIsOff()
    }

    private fun touchSample(panel: String) {
        sample(panel).performScrollTo().assertIsDisplayed().performTouchInput { click() }
    }

    private fun sample(panel: String): SemanticsNodeInteraction =
        compose.onNodeWithTag("library_$panel")

    private fun count(panel: String, expected: Int, prefix: String = "Clicks: ") {
        compose.onNodeWithTag("status_$panel").assertTextEquals("$prefix$expected")
    }

    private fun resetSamples() {
        compose.onNodeWithTag("reset").performScrollTo().performClick()
    }

    private fun recreateActivity() {
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        keepScreenOn()
    }

    private fun changeLanguage(language: AppLanguage) {
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("language_${language.name}").performScrollTo().performClick()
        compose.waitUntil(10_000) { LanguagePreferences.read(compose.activity) == language }
        compose.waitForIdle()
        recreateActivity()
    }

    private fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private data class ContainerCase(
        val component: LabComponent,
        val label: String,
        val function: String,
    )

    private data class SupportedCase(val container: ContainerCase, val family: DesignFamily)

    private data class LocalizedContainer(
        val language: AppLanguage,
        val clickable: String,
        val countPrefix: String,
        val content: String,
        val titles: List<String>,
    )

    private fun expandDetails(panel: String) {
        val toggle = compose.onNodeWithTag("implementation_details_$panel").performScrollTo()
        if (
            toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] !=
                ToggleableState.On
        )
            toggle.performTouchInput { click() }
    }
}
