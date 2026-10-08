package xyz.gaon.componentory.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.gaon.componentory.R

internal enum class SourceNotice(val title: String, val filename: String) {
    ATTRIBUTION("Componentory · NOTICE", "NOTICE.txt"),
    MIT("Componentory · MIT License", "Componentory-MIT.txt"),
    APACHE("Apache License 2.0", "Apache-2.0.txt"),
    AOSP_RESOURCES("AOSP resource releases · NOTICE", "aosp-resources-NOTICE.txt"),
    FRAMEWORK("AOSP framework · NOTICE", "aosp-frameworks-base-NOTICE.txt"),
    EASTER_EGGS("Android Easter egg ports · Apache 2.0", "easter-eggs-NOTICE.txt"),
    SDK("Android SDK · NOTICE", "android-sdk-NOTICE.txt"),
    AUTOFILL("AndroidX Autofill 1.3.0 · Apache 2.0", "androidx-autofill-LICENSE.txt"),
}

@Composable
internal fun SourceNotices(selected: SourceNotice?, onSelect: (SourceNotice) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focus = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val changeQuery: (String) -> Unit = {
        query = it
        scope.launch { listState.scrollToItem(0) }
    }
    Column(Modifier.fillMaxSize().testTag("source_notices_page")) {
        if (selected != null) {
            SourceDocumentText(selected.filename, Modifier.fillMaxSize())
        } else {
            OutlinedTextField(
                value = query,
                onValueChange = changeQuery,
                label = { Text(stringResource(R.string.source_notices_search_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                leadingIcon = { Icon(painterResource(R.drawable.ic_search), null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { changeQuery("") },
                            Modifier.testTag("source_notices_clear"),
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_close),
                                stringResource(R.string.clear_search),
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("source_notices_search"),
            )
            val notices =
                SourceNotice.entries.filter {
                    it.title.contains(query.trim(), ignoreCase = true) ||
                        it.filename.contains(query.trim(), ignoreCase = true)
                }
            Surface(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.padding(top = 16.dp).fillMaxWidth().weight(1f),
            ) {
                LazyColumn(state = listState, modifier = Modifier.testTag("source_notices_list")) {
                    item {
                        Text(
                            stringResource(R.string.source_notices_note),
                            Modifier.padding(18.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (notices.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.source_notices_empty),
                                Modifier.padding(18.dp).testTag("source_notices_empty"),
                            )
                        }
                    }
                    items(notices, key = { it.name }) { notice ->
                        SettingsListRow(
                            notice.title,
                            tag = "source_notice_${notice.name}",
                            onClick = { onSelect(notice) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
internal fun SourceDocumentDialog(title: String, filename: String, onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                SourceDocumentText(filename, Modifier.weight(1f, fill = false))
                TextButton(onClick = onClose, modifier = Modifier.testTag("source_notice_close")) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

@Composable
private fun SourceDocumentText(filename: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val body by
        produceState<String?>(null, filename) {
            value =
                withContext(Dispatchers.IO) {
                    try {
                        context.assets.open("legal/$filename").bufferedReader(Charsets.UTF_8).use {
                            it.readText()
                        }
                    } catch (_: IOException) {
                        ""
                    }
                }
        }
    androidx.compose.runtime.key(filename) {
        Text(
            body?.takeIf { it.isNotEmpty() }
                ?: stringResource(
                    if (body == null) R.string.source_notices_loading
                    else R.string.source_notices_unavailable
                ),
            modifier.verticalScroll(rememberScrollState()).testTag("source_notice_body"),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
