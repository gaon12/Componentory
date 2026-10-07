package xyz.gaon.componentory.lab

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import xyz.gaon.componentory.R

@Composable
internal fun SampleScrollViewport(tag: String, content: @Composable () -> Unit) {
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.fillMaxWidth().horizontalScroll(scroll).testTag(tag)) { content() }
        // Original library controls can have a minimum width larger than their host.
        // Keep their text size and expose both edges without relying on an invisible gesture.
        if (scroll.maxValue > 0) {
            Text(
                stringResource(R.string.preview_scroll_hint),
                Modifier.testTag("${tag}_hint"),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(
                    onClick = { scope.launch { scroll.animateScrollTo(0) } },
                    enabled = scroll.canScrollBackward,
                    modifier = Modifier.testTag("${tag}_start"),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    Text(stringResource(R.string.preview_scroll_start))
                }
                TextButton(
                    onClick = { scope.launch { scroll.animateScrollTo(scroll.maxValue) } },
                    enabled = scroll.canScrollForward,
                    modifier = Modifier.testTag("${tag}_end"),
                ) {
                    Text(stringResource(R.string.preview_scroll_end))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                }
            }
        }
    }
}
