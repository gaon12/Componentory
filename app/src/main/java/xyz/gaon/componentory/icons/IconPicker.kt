package xyz.gaon.componentory.icons

import android.widget.ImageView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.R

val LocalSampleIcon = staticCompositionLocalOf<CatalogIcon?> { null }

@Composable
fun IconPicker(
    platform: Boolean,
    selected: CatalogIcon,
    panel: String,
    onSelect: (CatalogIcon) -> Unit,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val entries = remember(platform) { IconCatalog.entries(context, platform) }
    OutlinedButton(onClick = { open = true }, modifier = Modifier.testTag("icon_picker_$panel")) {
        Text(stringResource(R.string.choose_icon, selected.name))
    }
    if (!open) return
    var query by rememberSaveable { mutableStateOf("") }
    var style by rememberSaveable { mutableStateOf<IconStyle?>(null) }
    var mirrored by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val filtered =
        remember(entries, query, style, mirrored) {
            entries.filter {
                it.matches(query) &&
                    (style == null || it.style == style) &&
                    (!mirrored || it.autoMirrored)
            }
        }
    Dialog(
        onDismissRequest = { open = false },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(Modifier.widthIn(max = 900.dp).fillMaxWidth().padding(24.dp).testTag("icon_dialog")) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.icons_title),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    TextButton(
                        onClick = {
                            focus.clearFocus()
                            open = false
                        },
                        modifier = Modifier.testTag("icon_close"),
                    ) {
                        Text(stringResource(R.string.close))
                    }
                }
                Text(
                    if (platform) stringResource(R.string.framework_drawables)
                    else
                        "androidx.compose.material:material-icons-extended:${BuildConfig.MATERIAL_ICONS_VERSION}",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (platform)
                    Text(
                        stringResource(R.string.framework_drawables_note),
                        style = MaterialTheme.typography.bodySmall,
                    )
                OutlinedTextField(
                    query,
                    { query = it },
                    modifier = Modifier.fillMaxWidth().testTag("icon_search"),
                    singleLine = true,
                    label = { Text(stringResource(R.string.icon_search_hint)) },
                )
                if (!platform) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                style == null,
                                { style = null },
                                label = { Text(stringResource(R.string.all_styles)) },
                                modifier = Modifier.testTag("icon_style_ALL"),
                            )
                        }
                        items(IconStyle.entries) { option ->
                            FilterChip(
                                style == option,
                                { style = option },
                                label = { Text(stringResource(option.labelRes)) },
                                modifier = Modifier.testTag("icon_style_${option.name}"),
                            )
                        }
                    }
                    FilterChip(
                        mirrored,
                        { mirrored = !mirrored },
                        label = { Text(stringResource(R.string.auto_mirrored)) },
                        modifier = Modifier.testTag("icon_mirrored"),
                    )
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.icon_count, filtered.size, entries.size),
                        Modifier.weight(1f).testTag("icon_count"),
                    )
                    // Filters persist across opens, so a clean sweep needs an explicit reset.
                    TextButton(
                        onClick = {
                            query = ""
                            style = null
                            mirrored = false
                            focus.clearFocus()
                        },
                        enabled = query.isNotEmpty() || style != null || mirrored,
                        modifier = Modifier.testTag("icon_reset"),
                    ) {
                        Text(stringResource(R.string.reset))
                    }
                }
                if (filtered.isEmpty())
                    Text(stringResource(R.string.no_results), Modifier.testTag("icon_empty"))
                LazyVerticalGrid(
                    GridCells.Adaptive(120.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp).testTag("icon_grid"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filtered, key = { it.id }) { icon ->
                        val isSelected = icon.id == selected.id
                        OutlinedCard(
                            onClick = {
                                focus.clearFocus()
                                onSelect(icon)
                                open = false
                            },
                            modifier =
                                Modifier.testTag("icon_entry_${icon.id}").semantics {
                                    if (isSelected) this.selected = true
                                },
                            border =
                                if (isSelected)
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                else CardDefaults.outlinedCardBorder(enabled = icon.available),
                            enabled = icon.available,
                        ) {
                            Column(
                                Modifier.fillMaxWidth().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                if (icon.available) IconPreview(icon, Modifier.size(32.dp))
                                else
                                    Text(
                                        stringResource(R.string.icon_unavailable),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                Text(icon.name, style = MaterialTheme.typography.labelMedium)
                                icon.style?.let {
                                    Text(
                                        stringResource(it.labelRes),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                                if (icon.autoMirrored)
                                    Text(
                                        stringResource(R.string.auto_mirrored),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconPreview(icon: CatalogIcon, modifier: Modifier) {
    if (icon.drawableId != null) {
        AndroidView(
            factory = { ImageView(it) },
            update = {
                it.setImageResource(icon.drawableId)
                it.contentDescription = icon.name
            },
            modifier = modifier,
        )
    } else {
        val vector = remember(icon.id) { icon.vector() }
        Icon(vector, contentDescription = icon.name, modifier = modifier)
    }
}
