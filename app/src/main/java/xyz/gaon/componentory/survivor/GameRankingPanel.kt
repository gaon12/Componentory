package xyz.gaon.componentory.survivor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import xyz.gaon.componentory.R

@Composable
internal fun GameRankingPanel(
    client: GamePlayClient,
    store: GameStore,
    save: GameSave,
    onSave: (GameSave) -> Unit,
) {
    if (!client.enabled) {
        Text(
            stringResource(R.string.game_online_unavailable),
            Modifier.testTag("game_online_unavailable"),
            style = MaterialTheme.typography.bodySmall,
        )
        return
    }
    var profile by remember { mutableStateOf<GameProfile?>(null) }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val operations = remember { Mutex() }
    val owner = LocalLifecycleOwner.current
    var foreground by remember {
        mutableStateOf(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    val publish by rememberUpdatedState(onSave)
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refresh++
        }
    suspend fun synchronize(force: Boolean = false) {
        profile = client.profile()
        val updated =
            withContext(Dispatchers.IO) {
                val failures =
                    GameRanking.retry(
                        store.read().submissions,
                        client,
                        client.leaderboard,
                        GameCatalog.RULESET,
                        System.currentTimeMillis(),
                        force,
                    ) { update ->
                        store.updateSubmission(update.runId) { before ->
                            if (before.status == SubmissionStatus.SENT) before else update
                        }
                    }
                store.read() to failures
            }
        publish(updated.first)
        failed = updated.second > 0
    }
    fun action(block: suspend () -> Unit) {
        if (busy) return
        busy = true
        scope.launch {
            try {
                operations.withLock {
                    failed = false
                    block()
                }
            } catch (timeout: TimeoutCancellationException) {
                failed = true
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                failed = true
            } finally {
                busy = false
            }
        }
    }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                foreground = true
                refresh++
            }
            if (event == Lifecycle.Event.ON_PAUSE) foreground = false
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(client, refresh, foreground, save.records.size) {
        if (!foreground) return@LaunchedEffect
        while (isActive) {
            if (!busy) {
                busy = true
                try {
                    operations.withLock {
                        failed = false
                        synchronize()
                    }
                } catch (timeout: TimeoutCancellationException) {
                    failed = true
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    failed = true
                } finally {
                    busy = false
                }
            }
            delay(60000)
        }
    }
    Text(stringResource(R.string.game_online_title), style = MaterialTheme.typography.titleMedium)
    val current = profile
    if (current == null) Text(stringResource(R.string.game_online_disconnected))
    else Text(stringResource(R.string.game_online_profile, current.name))
    if (failed) Text(stringResource(R.string.game_online_failed))
    val queued = save.submissions.filter { it.status == SubmissionStatus.QUEUED }
    if (queued.any { it.ruleset != GameCatalog.RULESET })
        Text(
            stringResource(R.string.game_online_old_rules),
            style = MaterialTheme.typography.bodySmall,
        )
    Text(stringResource(R.string.game_online_pending, queued.size))
    if (queued.any { it.profileId != null && it.profileId != current?.id })
        Text(
            stringResource(R.string.game_online_other_profile),
            style = MaterialTheme.typography.bodySmall,
        )
    Button(
        {
            action {
                profile = client.connect()
                synchronize(true)
            }
        },
        Modifier.fillMaxWidth().testTag("game_online_connect"),
        enabled = !busy,
    ) {
        Text(stringResource(R.string.game_online_connect))
    }
    OutlinedButton(
        { action { synchronize(true) } },
        Modifier.fillMaxWidth().testTag("game_online_retry"),
        enabled = !busy && current != null,
    ) {
        Text(stringResource(R.string.game_online_retry))
    }
    if (
        queued.any { it.profileId == null && it.ruleset == GameCatalog.RULESET } && current != null
    ) {
        OutlinedButton(
            {
                action {
                    if (client.profile()?.id != current.id) throw GameProfileChanged()
                    withContext(Dispatchers.IO) {
                        store.bindUnowned(current.id, GameCatalog.RULESET)
                    }
                    synchronize(true)
                }
            },
            Modifier.fillMaxWidth().testTag("game_online_bind"),
            enabled = !busy,
        ) {
            Text(stringResource(R.string.game_online_bind, current.name))
        }
    }
    for ((period, label) in
        listOf(
            LeaderboardVariant.TIME_SPAN_DAILY to R.string.game_online_daily,
            LeaderboardVariant.TIME_SPAN_WEEKLY to R.string.game_online_weekly,
            LeaderboardVariant.TIME_SPAN_ALL_TIME to R.string.game_online_alltime,
        )) {
        OutlinedButton(
            { action { launcher.launch(client.intent(period)) } },
            Modifier.fillMaxWidth(),
            enabled = !busy && current != null,
        ) {
            Text(stringResource(label))
        }
    }
}
