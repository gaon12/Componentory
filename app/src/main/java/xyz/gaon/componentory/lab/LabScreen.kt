package xyz.gaon.componentory.lab

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun LabScreen() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val environment = remember(configuration) { RuntimeEnvironment.read(context) }
    var left by rememberSaveable { mutableStateOf(PlatformFamily.CLASSIC) }
    var right by rememberSaveable { mutableStateOf(PlatformFamily.HOLO) }
    var component by rememberSaveable { mutableStateOf(LabComponent.BUTTON) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var reset by rememberSaveable { mutableIntStateOf(0) }
    var showDetails by rememberSaveable { mutableStateOf(false) }

    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { insets ->
        Column(
            Modifier.fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Componentory", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Explore Android UI generations. Touch, compare, experiment.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Card(
                Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "LIVE ON THIS DEVICE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                environment.summary,
                                modifier = Modifier.testTag("runtime"),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        TextButton(
                            onClick = { showDetails = !showDetails },
                            modifier = Modifier.testTag("environment_details"),
                        ) {
                            Text(if (showDetails) "Hide details" else "Details")
                        }
                    }
                    if (showDetails)
                        Text(environment.details, style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LabComponent.entries.forEach { option ->
                    FilterChip(
                        selected = component == option,
                        onClick = { component = option },
                        label = { Text(option.label) },
                        modifier = Modifier.testTag("component_${option.name}"),
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(component.label, style = MaterialTheme.typography.titleLarge)
                    Text(component.instruction, style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { reset++ }, modifier = Modifier.testTag("reset")) {
                    Text("Reset")
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                    modifier = Modifier.testTag("enabled"),
                )
                Text(if (enabled) "Samples enabled" else "Samples disabled")
            }
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth >= 600.dp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SamplePanel(
                            "LEFT",
                            left,
                            { left = it },
                            component,
                            enabled,
                            reset,
                            Modifier.weight(1f),
                        )
                        SamplePanel(
                            "RIGHT",
                            right,
                            { right = it },
                            component,
                            enabled,
                            reset,
                            Modifier.weight(1f),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SamplePanel("LEFT", left, { left = it }, component, enabled, reset)
                        SamplePanel("RIGHT", right, { right = it }, component, enabled, reset)
                    }
                }
            }
            Text(
                "Live platform widgets on the OS shown above. A theme changes the design family, not the Android version. Interaction feedback is separate from automated test results.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
