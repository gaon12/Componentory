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
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
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
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences

@RunWith(AndroidJUnit4::class)
class AccessibilityNamesTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private val families = listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
    private val english =
        LocalizedNames(
            AppLanguage.ENGLISH,
            "Enabled",
            "Select me",
            "Enable option",
            "Option A",
            "Option B",
            "Tri-state checkbox",
        )

    @Before
    fun prepareEnglishBaseline() {
        keepScreenOn()
        changeLanguage(AppLanguage.ENGLISH)
    }

    @After
    fun restoreEnglishBaseline() {
        compose.runOnUiThread { LanguagePreferences.apply(compose.activity, AppLanguage.ENGLISH) }
        compose.waitForIdle()
        keepScreenOn()
    }

    @Test
    fun namedGlobalSwitchesDisableRealSamplesInDetailAndComparison() {
        openDetail(LabComponent.CHECKBOX)
        chooseFamily(DesignFamily.MATERIAL2)
        resetSamples()
        global(english).assertIsOn().assertHasClickAction()
        named("library_LEFT", english.checkbox, Role.Checkbox).assertIsOff()

        disableSamples(english)
        named("library_LEFT", english.checkbox, Role.Checkbox)
            .assertIsNotEnabled()
            .performScrollTo()
            .performTouchInput { click() }
            .assertIsOff()
        enableSamples(english)
        named("library_LEFT", english.checkbox, Role.Checkbox)
            .performScrollTo()
            .performClick()
            .assertIsOn()

        compose.onNodeWithTag("detail_compare").performClick()
        chooseFamily(DesignFamily.MATERIAL3, "RIGHT")
        resetSamples()
        global(english).assertIsOn().assertHasClickAction()
        disableSamples(english)
        listOf("LEFT", "RIGHT").forEach { panel ->
            named("library_$panel", english.checkbox, Role.Checkbox)
                .assertIsNotEnabled()
                .performScrollTo()
                .performTouchInput { click() }
                .assertIsOff()
        }
        enableSamples(english)
        named("library_LEFT", english.checkbox, Role.Checkbox)
            .performScrollTo()
            .performClick()
            .assertIsOn()
        named("library_RIGHT", english.checkbox, Role.Checkbox).assertIsOff()
    }

    @Test
    fun bothLibrariesKeepBasicControlNamesRolesAndDisabledSelectionBehavior() {
        compose.onNodeWithTag("nav_compare").performClick()
        families.forEach { family ->
            chooseFamily(family)
            listOf(
                    Triple(LabComponent.CHECKBOX, english.checkbox, Role.Checkbox),
                    Triple(LabComponent.SWITCH, english.switch, Role.Switch),
                )
                .forEach { (component, label, role) ->
                    chooseComponent(component)
                    assertSource(component, family)
                    named("library_LEFT", label, role)
                        .assertIsOff()
                        .assertHasClickAction()
                        .performScrollTo()
                        .performClick()
                        .assertIsOn()
                    disableSamples(english)
                    named("library_LEFT", label, role)
                        .assertIsNotEnabled()
                        .performScrollTo()
                        .performTouchInput { click() }
                        .assertIsOn()
                    enableSamples(english)
                    named("library_LEFT", label, role)
                        .performScrollTo()
                        .performClick()
                        .assertIsOff()
                }

            chooseComponent(LabComponent.RADIO)
            assertSource(LabComponent.RADIO, family)
            radio(1, english).assertIsNotSelected().assertHasClickAction()
            radio(2, english).assertIsNotSelected().assertHasClickAction()
            radio(2, english).performScrollTo().performClick().assertIsSelected()
            radio(1, english).assertIsNotSelected()
            disableSamples(english)
            radio(2, english).assertIsNotEnabled().assertIsSelected()
            radio(1, english)
                .assertIsNotEnabled()
                .performScrollTo()
                .performTouchInput { click() }
                .assertIsNotSelected()
            radio(2, english).assertIsSelected()
            enableSamples(english)
            radio(1, english).performScrollTo().performClick().assertIsSelected()
            radio(2, english).assertIsNotSelected()
        }
    }

    @Test
    fun namedTriStateCheckboxesKeepAllStatesAcrossRecreationAndDisabledTouch() {
        compose.onNodeWithTag("nav_compare").performClick()
        chooseComponent(LabComponent.TRI_STATE_CHECKBOX)
        families.forEach { family ->
            chooseFamily(family)
            resetSamples()
            assertSource(LabComponent.TRI_STATE_CHECKBOX, family)
            named("library_LEFT", english.triState, Role.Checkbox)
                .assertIsOff()
                .assertHasClickAction()
                .performScrollTo()
                .performClick()
                .assertIsOn()
            named("library_LEFT", english.triState, Role.Checkbox).performClick()
            assertIndeterminate(english)

            compose.activityRule.scenario.recreate()
            compose.waitForIdle()
            keepScreenOn()
            assertIndeterminate(english)
            disableSamples(english)
            named("library_LEFT", english.triState, Role.Checkbox)
                .assertIsNotEnabled()
                .performScrollTo()
                .performTouchInput { click() }
            assertIndeterminate(english)
            enableSamples(english)
            named("library_LEFT", english.triState, Role.Checkbox)
                .performScrollTo()
                .performClick()
                .assertIsOff()
        }
    }

    @Test
    fun everySettingsLanguageNamesRealControlsInBothLibrariesAndGlobalPages() {
        val languages =
            listOf(
                english,
                LocalizedNames(
                    AppLanguage.KOREAN,
                    "사용 가능",
                    "선택해 보세요",
                    "옵션 켜기",
                    "옵션 A",
                    "옵션 B",
                    "세 상태 체크박스",
                ),
                LocalizedNames(
                    AppLanguage.JAPANESE,
                    "有効",
                    "選択してください",
                    "オプションを有効にする",
                    "選択肢 A",
                    "選択肢 B",
                    "3 状態チェックボックス",
                ),
                LocalizedNames(
                    AppLanguage.SIMPLIFIED_CHINESE,
                    "启用",
                    "选择此项",
                    "启用选项",
                    "选项 A",
                    "选项 B",
                    "三态复选框",
                ),
                LocalizedNames(
                    AppLanguage.TRADITIONAL_CHINESE,
                    "啟用",
                    "選取此項",
                    "啟用選項",
                    "選項 A",
                    "選項 B",
                    "三態核取方塊",
                ),
            )
        languages.forEach { labels ->
            changeLanguage(labels.language)
            openDetail(LabComponent.CHECKBOX)
            chooseFamily(DesignFamily.MATERIAL2)
            resetSamples()
            global(labels).assertIsOn()
            disableSamples(labels)
            named("library_LEFT", labels.checkbox, Role.Checkbox)
                .assertIsNotEnabled()
                .performScrollTo()
                .performTouchInput { click() }
                .assertIsOff()
            enableSamples(labels)
            named("library_LEFT", labels.checkbox, Role.Checkbox)
                .performScrollTo()
                .performClick()
                .assertIsOn()
            compose.onNodeWithTag("detail_compare").performClick()
            global(labels).assertIsOn()

            families.forEach { family ->
                chooseFamily(family)
                listOf(
                        Triple(LabComponent.CHECKBOX, labels.checkbox, Role.Checkbox),
                        Triple(LabComponent.SWITCH, labels.switch, Role.Switch),
                    )
                    .forEach { (component, label, role) ->
                        chooseComponent(component)
                        assertSource(component, family)
                        named("library_LEFT", label, role)
                            .assertIsOff()
                            .performScrollTo()
                            .performClick()
                            .assertIsOn()
                    }
                chooseComponent(LabComponent.RADIO)
                assertSource(LabComponent.RADIO, family)
                radio(1, labels).assertIsNotSelected()
                radio(2, labels)
                    .assertIsNotSelected()
                    .performScrollTo()
                    .performClick()
                    .assertIsSelected()
                radio(1, labels).assertIsNotSelected()
                chooseComponent(LabComponent.TRI_STATE_CHECKBOX)
                assertSource(LabComponent.TRI_STATE_CHECKBOX, family)
                named("library_LEFT", labels.triState, Role.Checkbox)
                    .assertIsOff()
                    .performScrollTo()
                    .performClick()
                    .assertIsOn()
                named("library_LEFT", labels.triState, Role.Checkbox).performClick()
                assertIndeterminate(labels)
            }
        }
    }

    private fun named(tag: String, label: String, role: Role): SemanticsNodeInteraction =
        compose
            .onNodeWithTag(tag)
            .assertContentDescriptionEquals(label)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, role))

    private fun global(labels: LocalizedNames) = named("enabled", labels.enabled, Role.Switch)

    private fun disableSamples(labels: LocalizedNames) {
        global(labels).assertIsOn().performScrollTo().performClick().assertIsOff()
    }

    private fun enableSamples(labels: LocalizedNames) {
        global(labels).assertIsOff().performScrollTo().performClick().assertIsOn()
    }

    private fun radio(option: Int, labels: LocalizedNames): SemanticsNodeInteraction {
        val sharedGroup =
            SemanticsMatcher.keyIsDefined(SemanticsProperties.SelectableGroup) and
                hasAnyDescendant(hasTestTag("library_LEFT_1")) and
                hasAnyDescendant(hasTestTag("library_LEFT_2"))
        return named(
                "library_LEFT_$option",
                if (option == 1) labels.optionA else labels.optionB,
                Role.RadioButton,
            )
            .assert(hasAnyAncestor(sharedGroup))
    }

    private fun assertIndeterminate(labels: LocalizedNames) {
        named("library_LEFT", labels.triState, Role.Checkbox)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ToggleableState,
                    ToggleableState.Indeterminate,
                )
            )
    }

    private fun assertSource(component: LabComponent, family: DesignFamily) {
        expandDetails("LEFT")
        compose.onNodeWithTag("source_LEFT").assertTextEquals(family.source(component))
    }

    private fun openDetail(component: LabComponent) {
        compose.onNodeWithTag("nav_list").performClick()
        compose.onNodeWithTag("nav_list").performClick()
        compose.onNodeWithTag("catalog_mode_SAMPLES").performClick()
        val search = compose.onNodeWithTag("component_search")
        search.performTextReplacement(component.label)
        search.performImeAction()
        compose.onNodeWithTag("list_${component.name}").performClick()
    }

    private fun chooseFamily(family: DesignFamily, panel: String = "LEFT") {
        compose.onNodeWithTag("family_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performScrollTo().performClick()
    }

    private fun chooseComponent(component: LabComponent) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
        resetSamples()
    }

    private fun resetSamples() {
        compose.onNodeWithTag("reset").performScrollTo().performClick()
    }

    private fun changeLanguage(language: AppLanguage) {
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("language_${language.name}").performScrollTo().performClick()
        compose.waitUntil(10_000) { LanguagePreferences.read(compose.activity) == language }
        compose.waitForIdle()
        keepScreenOn()
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        keepScreenOn()
        compose.onNodeWithTag("language_${language.name}").assertIsSelected()
    }

    private fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private data class LocalizedNames(
        val language: AppLanguage,
        val enabled: String,
        val checkbox: String,
        val switch: String,
        val optionA: String,
        val optionB: String,
        val triState: String,
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
