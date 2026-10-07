package xyz.gaon.componentory.history

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.LabComponent

@Composable
internal fun HistoryDetail(
    entry: HistoricalComponent,
    catalog: AndroidHistory,
    removedAt: Int?,
    onClose: () -> Unit,
    onOpenSample: (LabComponent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sample = entry.currentSample()
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("history_detail"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClose, Modifier.testTag("history_detail_back")) {
            Text(stringResource(R.string.history_back))
        }
        Text(entry.name.substringAfterLast('.'), style = MaterialTheme.typography.headlineSmall)
        Text(
            entry.name,
            Modifier.testTag("history_detail_class"),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            stringResource(R.string.history_snapshot, androidVersionLabel(entry.api)),
            Modifier.testTag("history_detail_snapshot"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            stringResource(R.string.history_since, catalog.introduced(entry.name)),
            style = MaterialTheme.typography.bodyMedium,
        )
        val kind =
            stringResource(
                when (entry.category) {
                    "VIEW" -> R.string.history_kind_view
                    "DIALOG" -> R.string.history_kind_dialog
                    "POPUP" -> R.string.history_kind_popup
                    "PREFERENCE" -> R.string.history_kind_preference
                    else -> R.string.history_kind_controller
                }
            )
        Text(
            if (sample != null) stringResource(sample.descriptionRes)
            else
                stringResource(
                    R.string.history_public_description,
                    kind,
                    androidVersionLabel(entry.api),
                ),
            Modifier.testTag("history_detail_description"),
            style = MaterialTheme.typography.bodyLarge,
        )
        if (sample != null && sample.platformSource != entry.name)
            Text(
                stringResource(R.string.history_child_sample, stringResource(sample.labelRes)),
                Modifier.testTag("history_child_sample"),
                style = MaterialTheme.typography.bodyMedium,
            )
        if (entry.parent != null)
            Text(
                stringResource(R.string.history_parent, entry.parent),
                Modifier.testTag("history_detail_parent"),
                style = MaterialTheme.typography.bodyMedium,
            )
        if (entry.abstract)
            Text(
                stringResource(R.string.history_abstract),
                style = MaterialTheme.typography.bodyMedium,
            )
        if (entry.deprecated)
            Text(
                stringResource(R.string.history_deprecated_state),
                style = MaterialTheme.typography.bodyMedium,
            )
        if (removedAt != null)
            Text(
                stringResource(R.string.history_removed_at, removedAt),
                style = MaterialTheme.typography.bodyMedium,
            )
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.history_capture_missing),
                    Modifier.testTag("history_detail_capture"),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    stringResource(R.string.history_behavior_unverified),
                    Modifier.testTag("history_detail_behavior"),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    stringResource(R.string.history_source, catalog.sourceCommit),
                    Modifier.testTag("history_detail_source"),
                    style = MaterialTheme.typography.bodySmall,
                )
                catalog.identicalSnapshots[entry.api]?.let {
                    Text(
                        stringResource(R.string.history_identical_snapshot, it),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        OutlinedButton(
            {
                try {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(androidReferenceUrl(entry.name)))
                    )
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(
                            context,
                            R.string.history_documentation_unavailable,
                            Toast.LENGTH_SHORT,
                        )
                        .show()
                }
            },
            Modifier.testTag("history_documentation"),
        ) {
            Text(stringResource(R.string.history_documentation))
        }
        if (sample != null) {
            Text(
                stringResource(R.string.runtime_sample_note, android.os.Build.VERSION.RELEASE),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button({ onOpenSample(sample) }, Modifier.testTag("history_open_sample")) {
                Text(stringResource(R.string.history_open_sample))
            }
        } else
            Text(
                stringResource(R.string.history_no_sample),
                Modifier.testTag("history_no_sample"),
                style = MaterialTheme.typography.bodyMedium,
            )
    }
}
