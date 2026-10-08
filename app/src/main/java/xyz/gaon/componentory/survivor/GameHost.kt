package xyz.gaon.componentory.survivor

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.gaon.componentory.R

@Composable
internal fun GameHost(assets: GameAssets, onClose: () -> Unit, storage: GameStore? = null) {
    val context = LocalContext.current
    val online = remember(context) { GamePlayClient(context as Activity) }
    var starting by remember { mutableStateOf(false) }
    val store = remember(storage) { storage ?: GameStore(File(context.filesDir, "survivor")) }
    var save by remember(store) { mutableStateOf<GameSave?>(null) }
    var saveFailed by remember { mutableStateOf(false) }
    var engine by remember { mutableStateOf<GameEngine?>(null) }
    var finished by remember { mutableStateOf<GameSession?>(null) }
    var discard by remember { mutableStateOf(false) }
    var recordsOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun failure(error: Exception) {
        Log.e("SurvivorSave", "Cannot update the game save", error)
        saveFailed = true
    }
    fun read() {
        try {
            save = store.read()
            save?.activeJson?.let { json ->
                val pending = GameJson.session(json)
                if (pending.outcome != RunOutcome.ACTIVE) save = store.finish(json)
            }
            saveFailed = false
        } catch (error: Exception) {
            failure(error)
        }
    }
    fun checkpoint(s: GameSession, urgent: Boolean) {
        val snapshot = GameJson.session(s)
        if (urgent) {
            try {
                store.checkpoint(snapshot)
            } catch (error: Exception) {
                failure(error)
            }
            return
        }
        scope.launch {
            try {
                withContext(Dispatchers.IO) { store.checkpoint(snapshot) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                failure(error)
            }
        }
    }
    fun complete(s: GameSession) {
        finished = s
        try {
            save = store.finish(GameJson.session(s))
            saveFailed = false
        } catch (error: Exception) {
            failure(error)
        }
    }
    val screens = rememberSaveableStateHolder()
    val battle = engine
    val result = finished
    LaunchedEffect(store) { read() }
    LaunchedEffect(assets) {
        (GameCatalog.releases.map { it.family.art } +
                listOf(
                    "button",
                    "slider",
                    "switch",
                    "spinner",
                    "progress",
                    "jellybean",
                    "neko",
                    "octopus",
                    "holo",
                ))
            .distinct()
            .forEach { assets.bitmap(it) }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth > maxHeight
        when {
            recordsOpen && save != null ->
                GameRecordScreen(requireNotNull(save).records, assets) { recordsOpen = false }
            result != null -> {
                BackHandler {
                    if (!saveFailed) {
                        engine = null
                        finished = null
                    }
                }
                Surface(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.fillMaxSize()
                            .safeDrawingPadding()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            stringResource(
                                if (result.outcome == RunOutcome.WON) R.string.game_won
                                else if (result.outcome == RunOutcome.ABANDONED)
                                    R.string.game_abandoned
                                else R.string.game_defeated
                            ),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(weaponName(result.startingWeapon))
                        Text(
                            stringResource(
                                R.string.game_result_counts,
                                result.seconds,
                                result.regularKills + result.eliteKills + result.bossKills,
                            )
                        )
                        save
                            ?.records
                            ?.firstOrNull { it.id == result.id }
                            ?.let { record ->
                                Text(
                                    stringResource(
                                        R.string.game_result_reward,
                                        record.score,
                                        record.currency,
                                    ),
                                    Modifier.testTag("game_reward"),
                                )
                            }
                        GameEquipmentSummary(result, assets)
                        Button(
                            {
                                engine = null
                                finished = null
                            },
                            Modifier.testTag("game_return"),
                            enabled = !saveFailed,
                        ) {
                            Text(stringResource(R.string.game_return))
                        }
                    }
                }
            }
            battle != null ->
                GameBattle(
                    battle,
                    assets,
                    onCheckpoint = ::checkpoint,
                    externalPause = saveFailed,
                    onFinished = ::complete,
                )
            else ->
                screens.SaveableStateProvider("lobby") {
                    val current = save
                    GameLobby(
                        assets,
                        onClose,
                        onStart = { weapon, ranked ->
                            if (
                                current != null &&
                                    current.activeId == null &&
                                    !saveFailed &&
                                    !starting
                            ) {
                                starting = true
                                scope.launch {
                                    try {
                                        val profile =
                                            if (ranked && online.enabled) {
                                                try {
                                                    online.profile()?.id
                                                } catch (cancelled: CancellationException) {
                                                    throw cancelled
                                                } catch (offline: Exception) {
                                                    null
                                                }
                                            } else null
                                        val progress = store.read().progress
                                        val created =
                                            GameEngine.create(
                                                weapon,
                                                if (ranked) RunMode.RANKED else RunMode.NORMAL,
                                                progress.permanent,
                                                progress.unlocked,
                                                rankedProfileId = profile,
                                            )
                                        save = store.start(GameJson.session(created.session))
                                        engine = created
                                    } catch (cancelled: CancellationException) {
                                        throw cancelled
                                    } catch (error: Exception) {
                                        failure(error)
                                    } finally {
                                        starting = false
                                    }
                                }
                            }
                        },
                        canStart =
                            current != null && current.activeId == null && !saveFailed && !starting,
                    ) {
                        Text(
                            stringResource(R.string.game_controls),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        OutlinedButton(
                            { recordsOpen = true },
                            Modifier.fillMaxWidth().testTag("game_records"),
                            enabled = current != null,
                        ) {
                            Text(stringResource(R.string.game_records))
                        }
                        if (current == null) Text(stringResource(R.string.game_save_loading))
                        current?.activeJson?.let { json ->
                            val saved = remember(json) { GameJson.session(json) }
                            Text(
                                stringResource(
                                    R.string.game_saved_run,
                                    weaponName(saved.startingWeapon),
                                    saved.seconds,
                                )
                            )
                            if (saved.ruleset != GameCatalog.RULESET)
                                Text(stringResource(R.string.game_old_rules))
                            Button(
                                { engine = GameEngine(GameJson.session(json)) },
                                Modifier.fillMaxWidth().testTag("game_continue"),
                                enabled =
                                    wide && saved.ruleset == GameCatalog.RULESET && !saveFailed,
                            ) {
                                Text(stringResource(R.string.game_continue))
                            }
                            OutlinedButton(
                                { discard = true },
                                Modifier.fillMaxWidth().testTag("game_discard"),
                            ) {
                                Text(stringResource(R.string.game_end_saved))
                            }
                        }
                        current?.let {
                            GameRankingPanel(online, store, it) { updated -> save = updated }
                            GameProgressPanel(
                                it.progress,
                                { upgrade ->
                                    try {
                                        save = store.buy(upgrade)
                                    } catch (error: Exception) {
                                        failure(error)
                                    }
                                },
                                { support ->
                                    try {
                                        save = store.unlock(support)
                                    } catch (error: Exception) {
                                        failure(error)
                                    }
                                },
                            )
                        }
                    }
                }
        }
        if (discard)
            AlertDialog(
                onDismissRequest = { discard = false },
                title = { Text(stringResource(R.string.game_end_saved)) },
                text = { Text(stringResource(R.string.game_end_saved_help)) },
                confirmButton = {
                    TextButton(
                        {
                            discard = false
                            save?.activeJson?.let {
                                val s = GameJson.session(it)
                                s.outcome = RunOutcome.ABANDONED
                                complete(s)
                            }
                        },
                        Modifier.testTag("game_confirm_discard"),
                    ) {
                        Text(stringResource(R.string.game_abandon))
                    }
                },
                dismissButton = {
                    TextButton({ discard = false }) { Text(stringResource(R.string.game_resume)) }
                },
            )
        if (saveFailed)
            AlertDialog(
                onDismissRequest = {},
                title = { Text(stringResource(R.string.game_save_failed)) },
                text = { Text(stringResource(R.string.game_save_failed_help)) },
                confirmButton = {
                    TextButton({
                        val terminal = finished
                        if (terminal != null) complete(terminal)
                        else {
                            val active = engine?.session
                            if (active != null) {
                                try {
                                    save = store.checkpoint(GameJson.session(active))
                                    saveFailed = false
                                } catch (error: Exception) {
                                    failure(error)
                                }
                            } else read()
                        }
                    }) {
                        Text(stringResource(R.string.game_retry))
                    }
                },
                dismissButton = { TextButton(onClose) { Text(stringResource(R.string.close)) } },
            )
    }
}
