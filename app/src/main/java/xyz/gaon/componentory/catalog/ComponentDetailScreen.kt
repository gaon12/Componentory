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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.lab.SamplePanel
import xyz.gaon.componentory.lab.rememberSampleState

@Composable
fun ComponentDetailScreen(
    component: LabComponent,
    family: DesignFamily,
    onFamilyChange: (DesignFamily) -> Unit,
) {
    var enabled by rememberSaveable(component) { mutableStateOf(true) }
    var reset by rememberSaveable(component) { mutableIntStateOf(0) }
    val state = rememberSampleState("DETAIL", family, component, reset)
    Column(
        Modifier.widthIn(max = 760.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("detail_screen")
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(component.descriptionRes), style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = enabled,
                onCheckedChange = { enabled = it },
                modifier = Modifier.testTag("enabled"),
            )
            Text(
                stringResource(R.string.enabled),
                modifier = Modifier.weight(1f).padding(start = 12.dp),
            )
            TextButton(onClick = { reset++ }, modifier = Modifier.testTag("reset")) {
                Text(stringResource(R.string.reset))
            }
        }
        SamplePanel(
            "LEFT",
            family,
            onFamilyChange,
            component,
            enabled,
            reset,
            state,
            title = stringResource(R.string.choose_ui_version),
        )
        Text(
            stringResource(R.string.runtime_sample_note, Build.VERSION.RELEASE),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
