package xyz.gaon.componentory.lab

import android.os.Build
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R
import xyz.gaon.componentory.icons.IconCatalog
import xyz.gaon.componentory.icons.IconPicker
import xyz.gaon.componentory.icons.LocalSampleIcon

@Composable
fun SamplePanel(
    panel: String,
    family: DesignFamily,
    onFamilyChange: (DesignFamily) -> Unit,
    component: LabComponent,
    enabled: Boolean,
    reset: Int,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.sample_title),
) {
    var menuOpen by remember { mutableStateOf(false) }
    val state =
        key(family, component, reset) {
            rememberSaveable(saver = SampleState.Saver) { SampleState(component.initialValue) }
        }
    val context = LocalContext.current
    val platform = family.platform
    val unsupported = family.unsupportedReason(component, Build.VERSION.SDK_INT, context)
    val icon =
        if (component.usesIcon && unsupported == null)
            remember(platform, state.icon) {
                IconCatalog.selected(context, platform != null, state.icon)
            }
        else null
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
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    modifier = Modifier.semantics { testTagsAsResourceId = true },
                ) {
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
            Text(family.origin(context), style = MaterialTheme.typography.bodySmall)
            if (icon != null) IconPicker(platform != null, icon, panel) { state.icon = it.id }
            if (icon != null) {
                Text(
                    if (platform != null) "android.R.drawable.${icon.name}"
                    else
                        "${icon.id} · icons ${xyz.gaon.componentory.BuildConfig.MATERIAL_ICONS_VERSION}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("icon_source_$panel"),
                )
            }
            Column(
                Modifier.fillMaxWidth().background(background).padding(16.dp).heightIn(min = 96.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                if (unsupported != null) {
                    Column(Modifier.testTag("unsupported_$panel")) {
                        Text(
                            stringResource(R.string.unsupported),
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            unsupported,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else
                    CompositionLocalProvider(LocalSampleIcon provides icon) {
                        key(family, component, reset) {
                            if (platform != null) {
                                PlatformSample(
                                    platform,
                                    component,
                                    if (panel == "LEFT") R.id.sample_left else R.id.sample_right,
                                    enabled,
                                    state,
                                    if (
                                        component == LabComponent.RATING ||
                                            component ==
                                                LabComponent.INDETERMINATE_CIRCULAR_PROGRESS
                                    )
                                        Modifier.wrapContentWidth(Alignment.Start)
                                    else Modifier.fillMaxWidth(),
                                )
                            } else if (family == DesignFamily.MATERIAL2) {
                                Material2Sample(component, panel, enabled, state)
                            } else {
                                Material3Sample(component, panel, enabled, state)
                            }
                        }
                    }
            }
            if (
                (component.isDeterminateProgress || component.isCountedBadge) && unsupported == null
            ) {
                val step = if (component.isCountedBadge) 1 else 10
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { state.value = (state.value - step).coerceAtLeast(0) },
                        enabled = enabled,
                        modifier = Modifier.testTag("decrease_$panel"),
                    ) {
                        Text("−$step")
                    }
                    TextButton(
                        onClick = { state.value = (state.value + step).coerceAtMost(100) },
                        enabled = enabled,
                        modifier = Modifier.testTag("increase_$panel"),
                    ) {
                        Text("+$step")
                    }
                    if (component.isCountedBadge) {
                        TextButton(
                            onClick = { state.value = (state.value + 10).coerceAtMost(100) },
                            enabled = enabled,
                            modifier = Modifier.testTag("increase_ten_$panel"),
                        ) {
                            Text("+10")
                        }
                    }
                }
            }
            if (unsupported == null)
                Text(
                    if (component == LabComponent.ICON)
                        stringResource(R.string.icon_status, requireNotNull(icon).name)
                    else component.feedback(context, state.value, state.text, state.rangeEnd),
                    modifier = Modifier.testTag("status_$panel"),
                    style = MaterialTheme.typography.titleMedium,
                )
            HorizontalDivider()
            Text(
                family.source(component, context),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("source_$panel"),
            )
            Text(
                family.implementation(context) +
                    if (platform != null && component.platformSource != null)
                        " · " + stringResource(R.string.widget_api, component.minimumApi)
                    else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("implementation_$panel"),
            )
            if (
                (component.isDeterminateProgress || component.isIndeterminateProgress) &&
                    platform == null &&
                    unsupported == null
            ) {
                Text(
                    stringResource(R.string.progress_note),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (component.isFloatingAction && unsupported == null) {
                Text(stringResource(R.string.fab_note), style = MaterialTheme.typography.bodySmall)
            }
            if ((component.isDivider || component.isBadge) && unsupported == null) {
                Text(
                    stringResource(R.string.preview_note),
                    style = MaterialTheme.typography.bodySmall,
                )
                if (component == LabComponent.LEGACY_DIVIDER) {
                    Text(
                        stringResource(R.string.legacy_divider_note),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (component.isSecureInput && unsupported == null) {
                Text(
                    stringResource(R.string.secure_note),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
