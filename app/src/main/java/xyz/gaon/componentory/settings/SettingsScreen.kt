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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.lab.RuntimeEnvironment

@Composable
fun SettingsScreen(appearance: AppAppearance, onAppearanceChange: (AppAppearance) -> Unit) {
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
        Text("설정", style = MaterialTheme.typography.headlineMedium)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("앱 테마", style = MaterialTheme.typography.titleMedium)
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
                            Text(option.label, modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
                Text(
                    "탐색 화면에 적용됩니다. 컴포넌트 샘플은 선택한 UI의 밝은 테마를 유지합니다.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("실행 환경", style = MaterialTheme.typography.titleMedium)
                Text(environment.summary, modifier = Modifier.testTag("runtime"))
                Text(environment.details, style = MaterialTheme.typography.bodySmall)
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("UI 라이브러리", style = MaterialTheme.typography.titleMedium)
                Text("Compose Material 2 · ${BuildConfig.MATERIAL2_VERSION}")
                Text("Compose Material 3 · ${BuildConfig.MATERIAL3_VERSION}")
                Text(
                    "플랫폼 샘플은 현재 OS의 Android 프레임워크 위젯을 사용합니다.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Text(
            "Componentory ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            "UI 버전 선택은 테마 또는 라이브러리를 바꿉니다. 기기의 Android OS 버전은 그대로입니다. 화면에 표시되는 조작 결과는 자동 테스트 결과와 구분됩니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
