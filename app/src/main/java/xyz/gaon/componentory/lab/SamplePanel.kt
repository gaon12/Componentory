package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

@Composable
fun SamplePanel(
    panel: String,
    family: DesignFamily,
    onFamilyChange: (DesignFamily) -> Unit,
    component: LabComponent,
    enabled: Boolean,
    reset: Int,
    modifier: Modifier = Modifier,
    title: String = "$panel SAMPLE",
) {
    var menuOpen by remember { mutableStateOf(false) }
    val state =
        key(family, component, reset) {
            rememberSaveable(saver = SampleState.Saver) { SampleState(component.initialValue) }
        }
    val context = LocalContext.current
    val platform = family.platform
    val background =
        remember(family) {
            val color = TypedValue()
            if (platform != null) {
                ContextThemeWrapper(context, platform.themeId)
                    .theme
                    .resolveAttribute(android.R.attr.colorBackground, color, true)
                Color(color.data)
            } else Color.White
        }
    Card(
        modifier.fillMaxWidth().testTag("panel_$panel"),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column {
                OutlinedButton(
                    onClick = { menuOpen = true },
                    modifier = Modifier.fillMaxWidth().testTag("family_$panel"),
                ) {
                    Text("${family.selectionLabel}  ▾")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DesignFamily.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.selectionLabel) },
                            onClick = {
                                onFamilyChange(option)
                                menuOpen = false
                            },
                            modifier = Modifier.testTag("family_${panel}_${option.name}"),
                        )
                    }
                }
            }
            Text(family.origin, style = MaterialTheme.typography.bodySmall)
            Column(
                Modifier.fillMaxWidth().background(background).padding(16.dp).heightIn(min = 96.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                key(family, component, reset) {
                    if (platform != null) {
                        PlatformSample(
                            platform,
                            component,
                            if (panel == "LEFT") R.id.sample_left else R.id.sample_right,
                            enabled,
                            state,
                            Modifier.fillMaxWidth(),
                        )
                    } else if (family == DesignFamily.MATERIAL2) {
                        Material2Sample(component, panel, enabled, state)
                    } else {
                        Material3Sample(component, panel, enabled, state)
                    }
                }
            }
            if (component == LabComponent.PROGRESS) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { state.value = (state.value - 10).coerceAtLeast(0) },
                        enabled = enabled,
                        modifier = Modifier.testTag("decrease_$panel"),
                    ) {
                        Text("−10")
                    }
                    TextButton(
                        onClick = { state.value = (state.value + 10).coerceAtMost(100) },
                        enabled = enabled,
                        modifier = Modifier.testTag("increase_$panel"),
                    ) {
                        Text("+10")
                    }
                }
            }
            Text(
                component.feedback(state.value, state.text),
                modifier = Modifier.testTag("status_$panel"),
                style = MaterialTheme.typography.titleMedium,
            )
            HorizontalDivider()
            Text(
                family.source(component),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("source_$panel"),
            )
            Text(
                family.implementation +
                    if (platform != null) " · widget API ${component.minimumApi}+" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("implementation_$panel"),
            )
            if (component == LabComponent.PROGRESS && platform == null) {
                Text(
                    "Read-only indicator; this library provides no disabled appearance.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
