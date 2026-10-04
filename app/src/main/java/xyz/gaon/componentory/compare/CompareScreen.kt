package xyz.gaon.componentory.compare

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
fun CompareScreen(
    component: LabComponent,
    onComponentChange: (LabComponent) -> Unit,
    left: DesignFamily,
    onLeftChange: (DesignFamily) -> Unit,
    right: DesignFamily,
    onRightChange: (DesignFamily) -> Unit,
) {
    var enabled by rememberSaveable { mutableStateOf(true) }
    var reset by rememberSaveable { mutableIntStateOf(0) }
    Column(
        Modifier.widthIn(max = 1100.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("compare_screen")
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("비교", style = MaterialTheme.typography.headlineMedium)
        Text(
            "같은 컴포넌트, 다른 UI. Android ${Build.VERSION.RELEASE}에서 직접 비교하세요.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ComponentPicker(component, onComponentChange)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                component.label,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = enabled,
                onCheckedChange = { enabled = it },
                modifier = Modifier.testTag("enabled"),
            )
            Text("사용 가능", modifier = Modifier.padding(horizontal = 12.dp))
            TextButton(onClick = { reset++ }, modifier = Modifier.testTag("reset")) { Text("초기화") }
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 600.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SamplePanel(
                        "LEFT",
                        left,
                        onLeftChange,
                        component,
                        enabled,
                        reset,
                        Modifier.weight(1f),
                        title = "왼쪽 UI",
                    )
                    SamplePanel(
                        "RIGHT",
                        right,
                        onRightChange,
                        component,
                        enabled,
                        reset,
                        Modifier.weight(1f),
                        title = "오른쪽 UI",
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SamplePanel(
                        "LEFT",
                        left,
                        onLeftChange,
                        component,
                        enabled,
                        reset,
                        title = "왼쪽 UI",
                    )
                    SamplePanel(
                        "RIGHT",
                        right,
                        onRightChange,
                        component,
                        enabled,
                        reset,
                        title = "오른쪽 UI",
                    )
                }
            }
        }
    }
}
