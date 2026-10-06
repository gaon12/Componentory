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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
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
    state: SampleState,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.sample_title),
) {
    var menuOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
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
                        val available =
                            option.unsupportedReason(component, Build.VERSION.SDK_INT) == null
                        DropdownMenuItem(
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(option.selectionLabel)
                                    Text(
                                        stringResource(
                                            if (available) R.string.sample_available
                                            else R.string.unsupported
                                        ),
                                        modifier =
                                            Modifier.testTag(
                                                "provider_availability_${panel}_${option.name}"
                                            ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                            onClick = {
                                onFamilyChange(option)
                                menuOpen = false
                            },
                            modifier =
                                Modifier.testTag("family_${panel}_${option.name}").semantics {
                                    selected = option == family
                                },
                        )
                    }
                }
            }
            Text(family.origin(context), style = MaterialTheme.typography.bodySmall)
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
                        key(family, component, reset, state) {
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
                (component == LabComponent.TIME_PICKER_DIALOG ||
                    component.isInlineTime ||
                    component == LabComponent.TEXT_CLOCK) && unsupported == null
            ) {
                val label = stringResource(R.string.time_24_hour)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = state.time24Hour,
                        onCheckedChange = { state.time24Hour = it },
                        enabled = component.isInlineTime || enabled,
                        modifier =
                            Modifier.testTag("time_24_hour_$panel").semantics {
                                contentDescription = label
                            },
                    )
                    Text(label, modifier = Modifier.clearAndSetSemantics {})
                }
            }
            if (component.isContainer && unsupported == null) {
                ContainerSampleConfiguration(panel, enabled, state)
            }
            if (component == LabComponent.CHECKED_TEXT_VIEW && unsupported == null) {
                val label = stringResource(R.string.checked_text_config_label)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = state.value == 1,
                        onCheckedChange = { state.value = if (it) 1 else 0 },
                        modifier =
                            Modifier.testTag("checked_text_config_$panel").semantics {
                                contentDescription = label
                            },
                    )
                    Text(label, modifier = Modifier.clearAndSetSemantics {})
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
                    else if (
                        component in listOf(LabComponent.DATE_PICKER, LabComponent.CALENDAR_VIEW)
                    )
                        stringResource(
                            R.string.status_inline_date,
                            state.inlineDateUtcMillis?.let { SampleDates.format(it, locale) }
                                ?: stringResource(R.string.date_no_selection),
                        )
                    else if (component == LabComponent.DATE_RANGE_PICKER)
                        stringResource(
                            R.string.status_date_range,
                            state.dateRangeStartUtcMillis?.let { SampleDates.format(it, locale) }
                                ?: stringResource(R.string.date_no_selection),
                            state.dateRangeEndUtcMillis?.let { SampleDates.format(it, locale) }
                                ?: stringResource(R.string.date_no_selection),
                        )
                    else
                        component.feedback(
                            context,
                            state.value,
                            state.text,
                            state.rangeEnd,
                            state.dateUtcMillis,
                            state.timeMinutes,
                            state.time24Hour,
                            state.chronometerBaseMillis,
                        ),
                    modifier = Modifier.testTag("status_$panel"),
                    style = MaterialTheme.typography.titleMedium,
                )
            if (
                unsupported == null &&
                    component in
                        listOf(
                            LabComponent.DATE_PICKER,
                            LabComponent.CALENDAR_VIEW,
                            LabComponent.DATE_RANGE_PICKER,
                        )
            ) {
                Text(
                    stringResource(R.string.inline_date_note),
                    style = MaterialTheme.typography.bodySmall,
                )
                if (component == LabComponent.CALENDAR_VIEW || family == DesignFamily.MATERIAL3) {
                    Text(
                        stringResource(R.string.inline_date_enabled_note),
                        modifier = Modifier.testTag("date_enabled_note_$panel"),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (component == LabComponent.TEXT_CLOCK && unsupported == null) {
                Text(
                    stringResource(R.string.text_clock_note),
                    modifier = Modifier.testTag("clock_note_$panel"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (component.deprecatedApi != null && unsupported == null) {
                Text(
                    stringResource(
                        R.string.clock_deprecated_note,
                        requireNotNull(component.deprecatedApi),
                    ),
                    modifier = Modifier.testTag("clock_note_$panel"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (component == LabComponent.CHRONOMETER && unsupported == null) {
                Text(
                    stringResource(R.string.chronometer_note),
                    modifier = Modifier.testTag("clock_note_$panel"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (component.isScrollContainer && unsupported == null) {
                Text(
                    stringResource(R.string.scroll_view_note),
                    modifier = Modifier.testTag("scroll_note_$panel"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if ((component.isViewSwitcher || component.isAdapterAnimator) && unsupported == null) {
                Text(
                    stringResource(R.string.switcher_note),
                    modifier = Modifier.testTag("switcher_note_$panel"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (component.isFrameworkLayout && unsupported == null) {
                Text(
                    stringResource(R.string.layout_note),
                    modifier = Modifier.testTag("layout_note_$panel"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (component.isZoomControl && unsupported == null) {
                Text(
                    stringResource(R.string.zoom_note),
                    modifier = Modifier.testTag("zoom_note_$panel"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (component == LabComponent.TAB_HOST && unsupported == null) {
                Text(
                    stringResource(R.string.tab_note),
                    modifier = Modifier.testTag("tab_note_$panel"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (component.isLegacyContainer && unsupported == null) {
                Text(
                    stringResource(R.string.legacy_container_note),
                    modifier = Modifier.testTag("legacy_note_$panel"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (component.isTransientWindow && unsupported == null) {
                Text(
                    stringResource(R.string.transient_note),
                    modifier = Modifier.testTag("transient_note_$panel"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (component.isInlineTime && unsupported == null) {
                Text(
                    stringResource(R.string.inline_time_note),
                    modifier = Modifier.testTag("time_configuration_$panel"),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    stringResource(
                        if (platform == null) R.string.inline_time_enabled_note
                        else R.string.native_time_enabled_note
                    ),
                    modifier = Modifier.testTag("time_enabled_note_$panel"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (component == LabComponent.TEXT && platform == null && unsupported == null) {
                Text(
                    stringResource(R.string.text_enabled_note),
                    modifier = Modifier.testTag("text_enabled_note_$panel"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (component == LabComponent.CHECKED_TEXT_VIEW && unsupported == null) {
                Text(
                    stringResource(R.string.checked_text_configuration_note),
                    modifier = Modifier.testTag("checked_text_configuration_$panel"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
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
            HorizontalDivider()
            Text(
                family.source(component, context),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("source_$panel"),
            )
            if (component == LabComponent.POPUP_MENU && platform == null && unsupported == null) {
                Text(
                    if (family == DesignFamily.MATERIAL2)
                        "androidx.compose.material.DropdownMenuItem"
                    else "androidx.compose.material3.DropdownMenuItem",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("menu_item_source_$panel"),
                )
            }
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
            if (component == LabComponent.DATE_PICKER_DIALOG && unsupported == null) {
                Text(
                    stringResource(
                        R.string.date_picker_configuration,
                        SampleDates.format(SampleDates.INITIAL_UTC_MILLIS, locale),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (component == LabComponent.TIME_PICKER_DIALOG && unsupported == null) {
                Text(
                    stringResource(R.string.time_picker_configuration),
                    style = MaterialTheme.typography.bodySmall,
                )
                if (family == DesignFamily.MATERIAL3 && !timePickerClockFitsWindow()) {
                    Text(
                        stringResource(R.string.time_small_window_note),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (component == LabComponent.POPUP_MENU && unsupported == null) {
                Text(
                    stringResource(R.string.menu_sample_note),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
