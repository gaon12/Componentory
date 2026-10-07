package xyz.gaon.componentory.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.semantics.Role
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
    FRAMEWORK("AOSP framework · NOTICE", "aosp-frameworks-base-NOTICE.txt"),
    SDK("Android SDK · NOTICE", "android-sdk-NOTICE.txt"),
    AUTOFILL("AndroidX Autofill 1.3.0 · Apache 2.0", "androidx-autofill-LICENSE.txt"),
}

@Composable
internal fun SourceNotices() {
    var selected by rememberSaveable { mutableStateOf<SourceNotice?>(null) }
    Column {
        Text(
            stringResource(R.string.source_notices_note),
            Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SourceNotice.entries.forEach { notice ->
            Row(
                Modifier.fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable(role = Role.Button) { selected = notice }
                    .testTag("source_notice_${notice.name}")
                    .padding(18.dp)
            ) {
                Text(notice.title, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    selected?.let { notice -> SourceNoticeDialog(notice) { selected = null } }
}

@Composable
private fun SourceNoticeDialog(notice: SourceNotice, onClose: () -> Unit) {
    val context = LocalContext.current
    val body by
        produceState<String?>(null, notice) {
            value =
                withContext(Dispatchers.IO) {
                    try {
                        context.assets
                            .open("legal/${notice.filename}")
                            .bufferedReader(Charsets.UTF_8)
                            .use { it.readText() }
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
                Text(notice.title, style = MaterialTheme.typography.titleLarge)
                androidx.compose.runtime.key(notice) {
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
