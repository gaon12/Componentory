package xyz.gaon.componentory.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.RuntimeEnvironment

@Composable
fun SettingsScreen(
    appearance: AppAppearance,
    onAppearanceChange: (AppAppearance) -> Unit,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val environment = remember(configuration) { RuntimeEnvironment.read(context) }
    BoxWithConstraints(Modifier.widthIn(max = 1100.dp).fillMaxSize()) {
        val expanded = maxWidth >= 840.dp
        Column(
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .testTag("settings_screen")
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                stringResource(R.string.nav_settings),
                style = MaterialTheme.typography.headlineMedium,
            )
            val preferences: @Composable () -> Unit = {
                Column(
                    Modifier.fillMaxWidth().testTag("settings_preferences"),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    SettingsGroup(stringResource(R.string.app_theme)) {
                        Column(Modifier.selectableGroup()) {
                            AppAppearance.entries.forEachIndexed { index, option ->
                                SelectionRow(
                                    stringResource(option.labelRes),
                                    appearance == option,
                                    { onAppearanceChange(option) },
                                    "appearance_${option.name}",
                                )
                                if (index < AppAppearance.entries.lastIndex)
                                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                            }
                        }
                        SettingsNote(stringResource(R.string.appearance_note))
                    }
                    SettingsGroup(stringResource(R.string.app_language)) {
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
                                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                            }
                        }
                        SettingsNote(stringResource(R.string.language_note))
                    }
                }
            }
            val deviceDetails: @Composable () -> Unit = {
                Column(
                    Modifier.fillMaxWidth().testTag("settings_device_details"),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    SettingsGroup(stringResource(R.string.runtime_title)) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                environment.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.testTag("runtime"),
                            )
                            Text(
                                environment.details,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    SettingsGroup(stringResource(R.string.ui_libraries)) {
                        LibraryVersionRow("Compose Material 2", BuildConfig.MATERIAL2_VERSION)
                        LibraryVersionRow("Compose Material 3", BuildConfig.MATERIAL3_VERSION)
                        SettingsNote(stringResource(R.string.platform_note))
                    }
                    SettingsGroup(stringResource(R.string.source_notices)) { SourceNotices() }
                }
            }
            if (expanded) {
                Row(
                    Modifier.fillMaxWidth().testTag("settings_columns"),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Column(Modifier.weight(1f)) { preferences() }
                    Column(Modifier.weight(1f)) { deviceDetails() }
                }
            } else {
                preferences()
                deviceDetails()
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Componentory ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    stringResource(R.string.accuracy_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp),
        )
        Card(Modifier.fillMaxWidth()) { Column(content = content) }
    }
}

@Composable
private fun SelectionRow(label: String, selected: Boolean, onSelect: () -> Unit, tag: String) {
    Row(
        Modifier.fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(selected, onClick = onSelect, role = Role.RadioButton)
            .testTag(tag)
            .padding(horizontal = 18.dp, vertical = 14.dp),
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
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
    }
}

@Composable
private fun SettingsNote(text: String) {
    Text(
        text,
        Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun LibraryVersionRow(label: String, version: String) {
    Row(
        Modifier.fillMaxWidth().padding(18.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(
            version,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
