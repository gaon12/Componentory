package xyz.gaon.componentory.compare

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.lab.SamplePanel
import xyz.gaon.componentory.lab.rememberSampleState

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
    // Layout changes must move the same experiment, not create new panel values.
    val leftState = rememberSampleState("LEFT", left, component, reset)
    val rightState = rememberSampleState("RIGHT", right, component, reset)
    val enabledLabel = stringResource(R.string.enabled)
    Column(
        Modifier.widthIn(max = 1100.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("compare_screen")
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.nav_compare), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.compare_intro, Build.VERSION.RELEASE),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ComponentPicker(component, onComponentChange)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = enabled,
                onCheckedChange = { enabled = it },
                modifier =
                    Modifier.testTag("enabled").semantics { contentDescription = enabledLabel },
            )
            Text(
                enabledLabel,
                modifier = Modifier.padding(horizontal = 12.dp).clearAndSetSemantics {},
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { reset++ }, modifier = Modifier.testTag("reset")) {
                Text(stringResource(R.string.reset))
            }
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
                        leftState,
                        Modifier.weight(1f),
                        title = stringResource(R.string.left_ui),
                    )
                    SamplePanel(
                        "RIGHT",
                        right,
                        onRightChange,
                        component,
                        enabled,
                        reset,
                        rightState,
                        Modifier.weight(1f),
                        title = stringResource(R.string.right_ui),
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
                        leftState,
                        title = stringResource(R.string.left_ui),
                    )
                    SamplePanel(
                        "RIGHT",
                        right,
                        onRightChange,
                        component,
                        enabled,
                        reset,
                        rightState,
                        title = stringResource(R.string.right_ui),
                    )
                }
            }
        }
    }
}
