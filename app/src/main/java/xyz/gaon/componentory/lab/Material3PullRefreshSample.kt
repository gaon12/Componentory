package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import xyz.gaon.componentory.R

private const val REFRESH_DELAY_MILLIS = 900L

// The pull gesture drives a real PullToRefreshBox over a LazyColumn.
// Refreshing is transient and finishes on its own; the refresh count is
// the copied input. The API exposes no enabled parameter, so a disabled
// panel simply never starts a refresh.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Material3PullRefreshSample(
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    var refreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(REFRESH_DELAY_MILLIS)
            refreshing = false
        }
    }
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            if (enabled) {
                refreshing = true
                state.value++
            }
        },
        state = refreshState,
        modifier = modifier,
    ) {
        LazyColumn(
            Modifier.fillMaxWidth().height(240.dp).testTag("library_${panel}_list"),
            verticalArrangement = Arrangement.Top,
        ) {
            items(8) { index ->
                ListItem(headlineContent = { Text(stringResource(R.string.list_item, index + 1)) })
            }
        }
    }
}
