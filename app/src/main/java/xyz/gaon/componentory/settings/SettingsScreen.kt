package xyz.gaon.componentory.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
    Column(
        Modifier.widthIn(max = 900.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen")
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(stringResource(R.string.nav_settings), style = MaterialTheme.typography.headlineMedium)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.app_language),
                    style = MaterialTheme.typography.titleMedium,
                )
                Column(Modifier.selectableGroup()) {
                    AppLanguage.entries.forEach { option ->
                        Row(
                            Modifier.fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .selectable(
                                    selected = language == option,
                                    onClick = { onLanguageChange(option) },
                                    role = Role.RadioButton,
                                )
                                .testTag("language_${option.name}"),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = language == option, onClick = null)
                            Text(
                                if (option == AppLanguage.SYSTEM)
                                    stringResource(R.string.language_system)
                                else option.nativeName,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                    }
                }
                Text(
                    stringResource(R.string.language_note),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.app_theme),
                    style = MaterialTheme.typography.titleMedium,
                )
                Column(Modifier.selectableGroup()) {
                    AppAppearance.entries.forEach { option ->
                        Row(
                            Modifier.fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .selectable(
                                    selected = appearance == option,
                                    onClick = { onAppearanceChange(option) },
                                    role = Role.RadioButton,
                                )
                                .testTag("appearance_${option.name}"),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = appearance == option, onClick = null)
                            Text(
                                stringResource(option.labelRes),
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                    }
                }
                Text(
                    stringResource(R.string.appearance_note),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.runtime_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(environment.summary, modifier = Modifier.testTag("runtime"))
                Text(environment.details, style = MaterialTheme.typography.bodySmall)
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.ui_libraries),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text("Compose Material 2 · ${BuildConfig.MATERIAL2_VERSION}")
                Text("Compose Material 3 · ${BuildConfig.MATERIAL3_VERSION}")
                Text(
                    stringResource(R.string.platform_note),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
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
