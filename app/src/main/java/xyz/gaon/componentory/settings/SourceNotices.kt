package xyz.gaon.componentory.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.gaon.componentory.R

internal enum class SourceNotice(val title: String, val filename: String) {
    ATTRIBUTION("Componentory · NOTICE", "NOTICE.txt"),
    MIT("Componentory · MIT License", "Componentory-MIT.txt"),
    APACHE("Apache License 2.0", "Apache-2.0.txt"),
    AOSP_RESOURCES("AOSP resource releases · NOTICE", "aosp-resources-NOTICE.txt"),
    FRAMEWORK("AOSP framework · NOTICE", "aosp-frameworks-base-NOTICE.txt"),
    SDK("Android SDK · NOTICE", "android-sdk-NOTICE.txt"),
    AUTOFILL("AndroidX Autofill 1.3.0 · Apache 2.0", "androidx-autofill-LICENSE.txt"),
}

@Composable
internal fun SourceNotices() {
    var selected by rememberSaveable { mutableStateOf<SourceNotice?>(null) }
    SourceNoticeRows { selected = it }
    selected?.let { notice ->
        SourceDocumentDialog(notice.title, notice.filename) { selected = null }
    }
}

@Composable
internal fun SourceNoticesDialog(onClose: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf<SourceNotice?>(null) }
    val notice = selected
    if (notice != null) {
        SourceDocumentDialog(notice.title, notice.filename) { selected = null }
    } else {
        Dialog(onDismissRequest = onClose) {
            Surface(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
            ) {
                Column(
                    Modifier.padding(24.dp).testTag("source_notices_dialog"),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        stringResource(R.string.source_notices),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Column(
                        Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
                    ) {
                        SourceNoticeRows { selected = it }
                    }
                    TextButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("source_notices_dismiss"),
                    ) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceNoticeRows(onSelect: (SourceNotice) -> Unit) {
    Column {
        Text(
            stringResource(R.string.source_notices_note),
            Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SourceNotice.entries.forEachIndexed { index, notice ->
            SettingsListRow(
                notice.title,
                tag = "source_notice_${notice.name}",
                onClick = { onSelect(notice) },
            )
            if (index < SourceNotice.entries.lastIndex) HorizontalDivider()
        }
    }
}

@Composable
internal fun SourceDocumentDialog(title: String, filename: String, onClose: () -> Unit) {
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
    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                androidx.compose.runtime.key(filename) {
                    Text(
                        body?.takeIf { it.isNotEmpty() }
                            ?: stringResource(
                                if (body == null) R.string.source_notices_loading
                                else R.string.source_notices_unavailable
                            ),
                        Modifier.weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .testTag("source_notice_body"),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TextButton(onClick = onClose, modifier = Modifier.testTag("source_notice_close")) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}
