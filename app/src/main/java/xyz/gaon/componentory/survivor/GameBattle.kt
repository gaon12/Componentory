package xyz.gaon.componentory.survivor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.sqrt
import xyz.gaon.componentory.R

@Composable
internal fun GameBattle(engine: GameEngine, assets: GameAssets, onFinished: (GameSession) -> Unit) {
    val s = engine.session
    var paused by remember { mutableStateOf(false) }
    var foreground by remember { mutableStateOf(true) }
    var movement by remember { mutableStateOf(Offset.Zero) }
    var requestSkill by remember { mutableStateOf(false) }
    var tick by remember { mutableIntStateOf(s.tick) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                foreground = false
                paused = true
                movement = Offset.Zero
            }
            if (event == Lifecycle.Event.ON_RESUME) foreground = true
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    BackHandler { paused = true }
    Surface(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val landscape = maxWidth > maxHeight
            val playable = landscape && foreground && !paused
            LaunchedEffect(landscape) { if (!landscape) paused = true }
            LaunchedEffect(engine, playable) {
                movement = Offset.Zero
                requestSkill = false
                if (!playable) return@LaunchedEffect
                var previous = 0L
                var accumulated = 0L
                while (s.outcome == RunOutcome.ACTIVE) {
                    val now = withFrameNanos { it }
                    if (previous != 0L) accumulated += (now - previous).coerceIn(0L, 100_000_000L)
                    previous = now
                    var steps = 0
                    while (accumulated >= 16_666_667L && steps++ < 6) {
                        engine.step(GameInput(movement.x, movement.y, requestSkill))
                        requestSkill = false
                        accumulated -= 16_666_667L
                    }
                    tick = s.tick
                }
                onFinished(s)
            }
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        stringResource(
                            R.string.game_health,
                            s.health.coerceAtLeast(0f).toInt(),
                            s.maxHealth.toInt(),
                        )
                    )
                    Text(
                        "%02d:%02d".format(tick / 3600, tick / 60 % 60),
                        Modifier.testTag("game_time"),
                    )
                    Text(stringResource(R.string.game_level, s.level), Modifier.weight(1f))
                    TextButton({ paused = true }, Modifier.testTag("game_pause")) {
                        Text(stringResource(R.string.game_pause))
                    }
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    GameBoard(s, assets, tick, Modifier.fillMaxSize().testTag("game_arena"))
                    Row(
                        Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        GameJoystick(playable) { movement = it }
                        Button(
                            { requestSkill = true },
                            Modifier.heightIn(min = 64.dp).testTag("game_skill"),
                            enabled = playable && s.skillTicks == 0,
                        ) {
                            Text(
                                if (s.skillTicks == 0)
                                    skillName(GameCatalog.skill(s.startingWeapon))
                                else
                                    stringResource(
                                        R.string.game_skill_wait,
                                        (s.skillTicks + 59) / 60,
                                    )
                            )
                        }
                    }
                }
            }
            if (paused || !landscape) {
                AlertDialog(
                    onDismissRequest = { if (landscape && foreground) paused = false },
                    title = {
                        Text(
                            stringResource(
                                if (landscape) R.string.game_paused else R.string.game_rotate
                            )
                        )
                    },
                    text = { Text(stringResource(R.string.game_pause_help)) },
                    confirmButton = {
                        TextButton(
                            { paused = false },
                            Modifier.testTag("game_resume"),
                            enabled = landscape && foreground,
                        ) {
                            Text(stringResource(R.string.game_resume))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            {
                                s.outcome = RunOutcome.ABANDONED
                                onFinished(s)
                            },
                            Modifier.testTag("game_abandon"),
                        ) {
                            Text(stringResource(R.string.game_abandon))
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun GameJoystick(enabled: Boolean, onMove: (Offset) -> Unit) {
    val description = stringResource(R.string.game_move)
    var knob by remember { mutableStateOf(Offset.Zero) }
    val latestMove by rememberUpdatedState(onMove)
    Canvas(
        Modifier.size(112.dp)
            .testTag("game_move")
            .semantics { contentDescription = description }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                fun move(position: Offset) {
                    val delta = position - Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width * 0.35f
                    val length = sqrt(delta.x * delta.x + delta.y * delta.y).coerceAtLeast(radius)
                    knob = delta / length * radius
                    latestMove(delta / length)
                }
                try {
                    detectDragGestures(
                        onDragStart = { move(it) },
                        onDragEnd = {
                            knob = Offset.Zero
                            latestMove(Offset.Zero)
                        },
                        onDragCancel = {
                            knob = Offset.Zero
                            latestMove(Offset.Zero)
                        },
                        onDrag = { change, _ ->
                            move(change.position)
                            change.consume()
                        },
                    )
                } finally {
                    knob = Offset.Zero
                    latestMove(Offset.Zero)
                }
            }
    ) {
        drawCircle(Color(0x773c526a))
        drawCircle(Color(0xccbed2e8), size.width * 0.18f, center + knob)
    }
}

@Composable
internal fun skillName(skill: SkillKind) =
    stringResource(
        when (skill) {
            SkillKind.BURST -> R.string.game_skill_burst
            SkillKind.FREEZE -> R.string.game_skill_freeze
            SkillKind.SHIELD -> R.string.game_skill_shield
            SkillKind.SUMMON -> R.string.game_skill_summon
        }
    )
