package xyz.gaon.componentory.catalog

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.lab.SamplePanel

@Composable
fun ComponentDetailScreen(
    component: LabComponent,
    family: DesignFamily,
    onFamilyChange: (DesignFamily) -> Unit,
) {
    var enabled by rememberSaveable(component) { mutableStateOf(true) }
    var reset by rememberSaveable(component) { mutableIntStateOf(0) }
    Column(
        Modifier.widthIn(max = 760.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("detail_screen")
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(component.description, style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = enabled,
                onCheckedChange = { enabled = it },
                modifier = Modifier.testTag("enabled"),
            )
            Text("사용 가능", modifier = Modifier.weight(1f).padding(start = 12.dp))
            TextButton(onClick = { reset++ }, modifier = Modifier.testTag("reset")) { Text("초기화") }
        }
        SamplePanel("LEFT", family, onFamilyChange, component, enabled, reset, title = "UI 버전 선택")
        Text(
            "현재 Android ${Build.VERSION.RELEASE}에서 실행 중 · 모든 샘플은 밝은 테마",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
