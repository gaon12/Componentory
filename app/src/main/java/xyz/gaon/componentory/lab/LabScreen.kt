package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

@Composable
fun LabScreen() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val environment = remember(configuration) { RuntimeEnvironment.read(context) }
    var left by rememberSaveable { mutableStateOf(PlatformFamily.CLASSIC) }
    var right by rememberSaveable { mutableStateOf(PlatformFamily.HOLO) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var reset by rememberSaveable { mutableIntStateOf(0) }
    var showDetails by rememberSaveable { mutableStateOf(false) }

    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { insets ->
        Column(
            Modifier.fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "ANDROID UI LAB",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text("Componentory", style = MaterialTheme.typography.headlineLarge)
                Text(
                    "One component. Different generations.",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "Touch real Android controls and compare their design families.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "LIVE ON THIS DEVICE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(environment.summary, modifier = Modifier.testTag("runtime"))
                    Text(
                        "The selected theme changes the style. The OS stays the same.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    TextButton(onClick = { showDetails = !showDetails }) {
                        Text(if (showDetails) "Hide environment" else "Environment details")
                    }
                    if (showDetails)
                        Text(environment.details, style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Button", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Tap each sample to inspect its response.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TextButton(onClick = { reset++ }, modifier = Modifier.testTag("reset")) {
                    Text("Reset")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                    modifier = Modifier.testTag("enabled"),
                )
                Text(
                    if (enabled) "Samples enabled" else "Samples disabled",
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth >= 600.dp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SamplePanel(
                            "LEFT",
                            left,
                            { left = it },
                            enabled,
                            reset,
                            Modifier.weight(1f),
                        )
                        SamplePanel(
                            "RIGHT",
                            right,
                            { right = it },
                            enabled,
                            reset,
                            Modifier.weight(1f),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SamplePanel("LEFT", left, { left = it }, enabled, reset)
                        SamplePanel("RIGHT", right, { right = it }, enabled, reset)
                    }
                }
            }
            Text(
                "These are live framework widgets using light themes. Family labels identify when the style was introduced; the runtime above identifies where it runs now.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SamplePanel(
    panel: String,
    family: PlatformFamily,
    onFamilyChange: (PlatformFamily) -> Unit,
    enabled: Boolean,
    reset: Int,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var clicks by rememberSaveable(family, reset) { mutableIntStateOf(0) }
    val context = LocalContext.current
    val background =
        remember(family) {
            val value = TypedValue()
            ContextThemeWrapper(context, family.themeId)
                .theme
                .resolveAttribute(android.R.attr.colorBackground, value, true)
            Color(value.data)
        }
    Card(
        modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "$panel SAMPLE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column {
                OutlinedButton(
                    onClick = { menuOpen = true },
                    modifier = Modifier.fillMaxWidth().testTag("family_$panel"),
                ) {
                    Text("${family.label}  ▾")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    PlatformFamily.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                onFamilyChange(option)
                                menuOpen = false
                            },
                            modifier = Modifier.testTag("family_${panel}_${option.name}"),
                        )
                    }
                }
            }
            Text("Introduced with ${family.origin}", style = MaterialTheme.typography.bodySmall)
            Column(
                Modifier.fillMaxWidth()
                    .background(background)
                    .padding(20.dp)
                    .heightIn(min = 120.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                key(family, reset) {
                    PlatformButton(
                        family,
                        if (panel == "LEFT") R.id.sample_left else R.id.sample_right,
                        enabled,
                        { clicks++ },
                        Modifier.fillMaxWidth(),
                    )
                }
            }
            Text(
                "Clicks: $clicks",
                modifier = Modifier.testTag("status_$panel"),
                style = MaterialTheme.typography.titleMedium,
            )
            HorizontalDivider()
            Text("android.widget.Button", style = MaterialTheme.typography.bodySmall)
            Text(
                "android:${family.themeName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
