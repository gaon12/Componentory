package xyz.gaon.componentory.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

@Composable
internal fun PrivacyPolicy() {
    var opened by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.padding(18.dp)) {
        Text(
            stringResource(R.string.privacy_policy_summary),
            style = MaterialTheme.typography.bodyMedium,
        )
        TextButton(
            onClick = { opened = true },
            modifier = Modifier.testTag("privacy_policy_open"),
        ) {
            Text(stringResource(R.string.privacy_policy_read))
        }
    }
    if (opened) {
        SourceDocumentDialog(stringResource(R.string.privacy_policy), "PrivacyPolicy.txt") {
            opened = false
        }
    }
}
