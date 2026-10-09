package xyz.gaon.componentory.survivor

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
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
    fun startRun(weapon: WeaponId, ranked: Boolean) {
        val current = save
        if (current != null && current.activeId == null && !saveFailed && !starting) {
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
    MaterialTheme(colorScheme = GameColorScheme) {
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
                    GameResultScreen(
                        result,
                        save?.records?.firstOrNull { it.id == result.id },
                        assets,
                        canLeave = !saveFailed,
                        onRetry =
                            if (wide) {
                                {
                                    engine = null
                                    finished = null
                                    startRun(result.startingWeapon, result.mode == RunMode.RANKED)
                                }
                            } else null,
                    ) {
                        engine = null
                        finished = null
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
                        val saved =
                            current?.activeJson?.let { json ->
                                remember(json) { GameJson.session(json) }
                            }
                        GameLobby(
                            assets,
                            onClose,
                            onStart = ::startRun,
                            canStart =
                                current != null &&
                                    current.activeId == null &&
                                    !saveFailed &&
                                    !starting,
                            currency = current?.progress?.currency,
                            saved = saved,
                            canContinue =
                                wide && saved?.ruleset == GameCatalog.RULESET && !saveFailed,
                            onContinue = {
                                current?.activeJson?.let {
                                    engine = GameEngine(GameJson.session(it))
                                }
                            },
                            onDiscard = { discard = true },
                            onRecords = current?.let { { recordsOpen = true } },
                            loading = current == null,
                            shop =
                                current?.let {
                                    {
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
                                            assets,
                                        )
                                    }
                                },
                            ranking =
                                current?.let {
                                    {
                                        GameRankingPanel(online, store, it) { updated ->
                                            save = updated
                                        }
                                    }
                                },
                        )
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
                        TextButton({ discard = false }) {
                            Text(stringResource(R.string.game_resume))
                        }
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
}
