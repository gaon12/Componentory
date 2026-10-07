package xyz.gaon.componentory.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import xyz.gaon.componentory.R

@Composable
fun IntroductionDialog(onFinish: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val pages =
        listOf(
            R.string.introduction_browse_title to R.string.introduction_browse_body,
            R.string.introduction_compare_title to R.string.introduction_compare_body,
            R.string.introduction_accuracy_title to R.string.introduction_accuracy_body,
        )
    Dialog(
        onDismissRequest = onFinish,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(24.dp).testTag("introduction"),
            shape = RoundedCornerShape(28.dp),
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(
                    Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Image(
                        painterResource(R.drawable.ic_launcher_legacy),
                        null,
                        Modifier.size(80.dp),
                    )
                    Text(
                        stringResource(pages[page].first),
                        Modifier.testTag("introduction_title"),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        stringResource(pages[page].second),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        stringResource(R.string.introduction_page, page + 1, pages.size),
                        Modifier.testTag("introduction_page"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(
                        onClick = onFinish,
                        modifier = Modifier.weight(1f).testTag("introduction_skip"),
                    ) {
                        Text(stringResource(R.string.introduction_skip))
                    }
                    if (page > 0)
                        TextButton(
                            onClick = { page-- },
                            modifier = Modifier.testTag("introduction_back"),
                        ) {
                            Text(stringResource(R.string.introduction_back))
                        }
                    Button(
                        onClick = { if (page == pages.lastIndex) onFinish() else page++ },
                        modifier = Modifier.weight(1f).testTag("introduction_next"),
                    ) {
                        Text(
                            stringResource(
                                if (page == pages.lastIndex) R.string.introduction_start
                                else R.string.introduction_next
                            )
                        )
                    }
                }
            }
        }
    }
}
