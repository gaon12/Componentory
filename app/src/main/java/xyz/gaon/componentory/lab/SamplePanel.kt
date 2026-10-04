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
    family: PlatformFamily,
    onFamilyChange: (PlatformFamily) -> Unit,
    component: LabComponent,
    enabled: Boolean,
    reset: Int,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val state =
        rememberSaveable(family, component, reset, saver = SampleState.Saver) {
            SampleState(component.initialValue)
        }
    val context = LocalContext.current
    val background =
        remember(family) {
            val color = TypedValue()
            ContextThemeWrapper(context, family.themeId)
                .theme
                .resolveAttribute(android.R.attr.colorBackground, color, true)
            Color(color.data)
        }
    Card(
        modifier.fillMaxWidth().testTag("panel_$panel"),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            Text("Theme from ${family.origin}", style = MaterialTheme.typography.bodySmall)
            Column(
                Modifier.fillMaxWidth().background(background).padding(16.dp).heightIn(min = 96.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                key(family, component, reset) {
                    PlatformSample(
                        family,
                        component,
                        if (panel == "LEFT") R.id.sample_left else R.id.sample_right,
                        enabled,
                        state,
                        Modifier.fillMaxWidth(),
                    )
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
            Text(component.source, style = MaterialTheme.typography.bodySmall)
            Text(
                "android:${family.themeName} · widget API ${component.minimumApi}+",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
