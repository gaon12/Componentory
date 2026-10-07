package xyz.gaon.componentory.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.RuntimeEnvironment

private enum class SettingsPage(
    val title: Int,
    val summary: Int,
    val icon: ImageVector,
    val color: Color,
) {
    APPEARANCE(
        R.string.app_theme,
        R.string.settings_appearance_summary,
        Icons.Default.Palette,
        Color(0xFF3182F6),
    ),
    LANGUAGE(
        R.string.app_language,
        R.string.settings_language_summary,
        Icons.Default.Language,
        Color(0xFF009D8B),
    ),
    DEVICE(
        R.string.runtime_title,
        R.string.settings_device_summary,
        Icons.Default.PhoneAndroid,
        Color(0xFF7659DF),
    ),
    LIBRARIES(
        R.string.ui_libraries,
        R.string.settings_libraries_summary,
        Icons.Default.Widgets,
        Color(0xFF5478CF),
    ),
    LICENSES(
        R.string.source_notices,
        R.string.settings_licenses_summary,
        Icons.Default.Description,
        Color(0xFFE07825),
    ),
    ABOUT(
        R.string.settings_about,
        R.string.settings_about_summary,
        Icons.Default.Info,
        Color(0xFF637282),
    ),
    PRIVACY(
        R.string.privacy_policy,
        R.string.privacy_policy_category_summary,
        Icons.Default.PrivacyTip,
        Color(0xFF009D8B),
    ),
}

@Composable
fun SettingsScreen(
    appearance: AppAppearance,
    onAppearanceChange: (AppAppearance) -> Unit,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onShowIntroduction: () -> Unit = {},
) {
    var selected by rememberSaveable { mutableStateOf<SettingsPage?>(null) }
    BackHandler(enabled = selected != null) { selected = null }
    BoxWithConstraints(Modifier.widthIn(max = 1100.dp).fillMaxSize().testTag("settings_screen")) {
        val expanded = maxWidth >= 840.dp
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (expanded || selected == null) {
                Text(
                    stringResource(R.string.nav_settings),
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
            if (expanded) {
                Row(
                    Modifier.fillMaxWidth().weight(1f).testTag("settings_columns"),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                ) {
                    Box(Modifier.width(300.dp).testTag("settings_categories")) {
                        SettingsCategories(selected) { selected = it }
                    }
                    Column(Modifier.weight(1f).testTag("settings_detail")) {
                        val page = selected
                        if (page == null) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    stringResource(R.string.settings_choose_category),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else
                            SettingsDetail(
                                page,
                                appearance,
                                onAppearanceChange,
                                language,
                                onLanguageChange,
                                onShowIntroduction,
                            )
                    }
                }
            } else {
                val page = selected
                if (page == null) SettingsCategories(null) { selected = it }
                else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        IconButton(
                            onClick = { selected = null },
                            modifier = Modifier.testTag("settings_back"),
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_back),
                                stringResource(R.string.nav_settings),
                            )
                        }
                        Text(
                            stringResource(page.title),
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    SettingsDetail(
                        page,
                        appearance,
                        onAppearanceChange,
                        language,
                        onLanguageChange,
                        onShowIntroduction = onShowIntroduction,
                        showTitle = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCategories(selected: SettingsPage?, onSelect: (SettingsPage) -> Unit) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        listOf(
                listOf(SettingsPage.APPEARANCE, SettingsPage.LANGUAGE),
                listOf(SettingsPage.DEVICE, SettingsPage.LIBRARIES),
                listOf(SettingsPage.LICENSES, SettingsPage.PRIVACY),
                listOf(SettingsPage.ABOUT),
            )
            .forEach { group ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    group.forEachIndexed { index, page ->
                        Row(
                            Modifier.fillMaxWidth()
                                .heightIn(min = 80.dp)
                                .clickable(role = Role.Button) { onSelect(page) }
                                .testTag("settings_category_${page.name}")
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                Modifier.size(40.dp).background(page.color, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(page.icon, null, Modifier.size(22.dp), tint = Color.White)
                            }
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    stringResource(page.title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color =
                                        if (selected == page) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    stringResource(page.summary),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                null,
                                Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (index < group.lastIndex)
                            HorizontalDivider(Modifier.padding(start = 72.dp, end = 18.dp))
                    }
                }
            }
        Text(
            "Componentory ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsDetail(
    page: SettingsPage,
    appearance: AppAppearance,
    onAppearanceChange: (AppAppearance) -> Unit,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onShowIntroduction: () -> Unit,
    showTitle: Boolean = true,
) {
    // Each page owns its scroll position; changing categories starts at its heading.
    key(page) {
        Column(
            Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .testTag("settings_page_${page.name}"),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (showTitle)
                Text(stringResource(page.title), style = MaterialTheme.typography.headlineSmall)
            when (page) {
                SettingsPage.APPEARANCE ->
                    SettingsGroup {
                        Column(Modifier.selectableGroup()) {
                            AppAppearance.entries.forEachIndexed { index, option ->
                                SelectionRow(
                                    stringResource(option.labelRes),
                                    appearance == option,
                                    { onAppearanceChange(option) },
                                    "appearance_${option.name}",
                                )
                                if (index < AppAppearance.entries.lastIndex)
                                    HorizontalDivider(Modifier.padding(horizontal = 18.dp))
                            }
                        }
                        SettingsNote(stringResource(R.string.appearance_note))
                    }
                SettingsPage.LANGUAGE ->
                    SettingsGroup {
                        Column(Modifier.selectableGroup()) {
                            AppLanguage.entries.forEachIndexed { index, option ->
                                SelectionRow(
                                    if (option == AppLanguage.SYSTEM)
                                        stringResource(R.string.language_system)
                                    else option.nativeName,
                                    language == option,
                                    { onLanguageChange(option) },
                                    "language_${option.name}",
                                )
                                if (index < AppLanguage.entries.lastIndex)
                                    HorizontalDivider(Modifier.padding(horizontal = 18.dp))
                            }
                        }
                        SettingsNote(stringResource(R.string.language_note))
                    }
                SettingsPage.DEVICE -> {
                    val context = LocalContext.current
                    val configuration = LocalConfiguration.current
                    val environment = remember(configuration) { RuntimeEnvironment.read(context) }
                    SettingsGroup {
                        Column(
                            Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                environment.summary,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.testTag("runtime"),
                            )
                            Text(
                                environment.details,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                SettingsPage.LIBRARIES ->
                    SettingsGroup {
                        LibraryVersionRow("Compose Material 2", BuildConfig.MATERIAL2_VERSION)
                        LibraryVersionRow("Compose Material 3", BuildConfig.MATERIAL3_VERSION)
                        Text(
                            stringResource(R.string.material3_experimental_note),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        LibraryVersionRow(
                            "AndroidX Autofill · inline UI v1",
                            BuildConfig.AUTOFILL_VERSION,
                        )
                        SettingsNote(stringResource(R.string.platform_note))
                    }
                SettingsPage.LICENSES -> SettingsGroup { SourceNotices() }
                SettingsPage.PRIVACY -> SettingsGroup { PrivacyPolicy() }
                SettingsPage.ABOUT ->
                    SettingsGroup {
                        TextButton(
                            onClick = onShowIntroduction,
                            modifier = Modifier.padding(8.dp).testTag("show_introduction"),
                        ) {
                            Text(stringResource(R.string.introduction_replay))
                        }
                        SettingsNote("Componentory ${BuildConfig.VERSION_NAME}")
                        ProjectLinks()
                        SettingsNote(stringResource(R.string.accuracy_note))
                        Text(
                            stringResource(R.string.android_trademark),
                            Modifier.padding(18.dp).testTag("android_trademark"),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
            }
        }
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) { Column(content = content) }
}

@Composable
private fun SelectionRow(label: String, selected: Boolean, onSelect: () -> Unit, tag: String) {
    Row(
        Modifier.fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(selected, onClick = onSelect, role = Role.RadioButton)
            .testTag(tag)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            label,
            Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color =
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
        )
        if (selected)
            Icon(
                Icons.Default.Check,
                null,
                Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
    }
}

@Composable
private fun SettingsNote(text: String) {
    Text(
        text,
        Modifier.padding(18.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun LibraryVersionRow(label: String, version: String) {
    Column(
        Modifier.fillMaxWidth().padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            version,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
