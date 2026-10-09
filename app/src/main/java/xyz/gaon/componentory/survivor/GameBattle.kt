package xyz.gaon.componentory.survivor

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.sqrt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import xyz.gaon.componentory.R

@Composable
internal fun GameBattle(
    engine: GameEngine,
    assets: GameAssets,
    onCheckpoint: (GameSession, Boolean) -> Unit = { _, _ -> },
    externalPause: Boolean = false,
    onFinished: (GameSession) -> Unit,
) {
    val s = engine.session
    val checkpoint by rememberUpdatedState(onCheckpoint)
    LaunchedEffect(engine) {
        while (isActive) {
            delay(5000)
            checkpoint(s, false)
        }
    }
    DisposableEffect(engine) {
        onDispose { if (s.outcome == RunOutcome.ACTIVE) checkpoint(s, true) }
    }
    val window = (LocalContext.current as? Activity)?.window
    var paused by remember { mutableStateOf(false) }
    var foreground by remember { mutableStateOf(true) }
    var movement by remember { mutableStateOf(Offset.Zero) }
    var requestSkill by remember { mutableStateOf(false) }
    var level by remember { mutableIntStateOf(s.level) }
    var tick by remember { mutableIntStateOf(s.tick) }
    var choices by remember { mutableStateOf(s.choices.toList()) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                foreground = false
                checkpoint(s, true)
                paused = true
                movement = Offset.Zero
            }
            if (event == Lifecycle.Event.ON_RESUME) foreground = true
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    BackHandler { paused = true }
    Box(
        Modifier.fillMaxSize().background(GameColors.Night).semantics {
            testTagsAsResourceId = true
        }
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val landscape = maxWidth > maxHeight
            val playable = landscape && foreground && !paused && !externalPause && choices.isEmpty()
            DisposableEffect(window, playable) {
                if (playable) window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
            }
            LaunchedEffect(landscape, externalPause) {
                if (!landscape || externalPause) paused = true
            }
            LaunchedEffect(engine, playable) {
                movement = Offset.Zero
                requestSkill = false
                if (!playable) return@LaunchedEffect
                var previous = 0L
                var accumulated = 0L
                while (
                    s.outcome == RunOutcome.ACTIVE &&
                        s.choices.isEmpty() &&
                        foreground &&
                        !paused &&
                        !externalPause
                ) {
                    val now = withFrameNanos { it }
                    if (!foreground || paused || externalPause) break
                    if (previous != 0L) accumulated += (now - previous).coerceIn(0L, 100_000_000L)
                    previous = now
                    var steps = 0
                    while (accumulated >= 16_666_667L && steps++ < 6) {
                        engine.step(GameInput(movement.x, movement.y, requestSkill))
                        requestSkill = false
                        accumulated -= 16_666_667L
                    }
                    tick = s.tick
                    choices = s.choices.toList()
                }
                if (s.outcome != RunOutcome.ACTIVE) onFinished(s)
            }
            // The arena fills the whole screen; only the HUD keeps clear of cutouts.
            GameBoard(s, assets, tick, Modifier.fillMaxSize().testTag("game_arena"))
            Box(Modifier.fillMaxSize().safeDrawingPadding().padding(12.dp)) {
                GameHud(s, level, tick, onPause = { paused = true })
                GameJoystick(playable, Modifier.align(Alignment.BottomStart)) { movement = it }
                GameSkillButton(s, playable, Modifier.align(Alignment.BottomEnd)) {
                    requestSkill = true
                }
            }
            if (choices.isNotEmpty() && !paused && landscape) {
                GameUpgradePanel(s, assets, choices) { choice ->
                    if (GameGrowth.choose(s, choice)) {
                        choices = s.choices.toList()
                        level = s.level
                        tick = s.tick
                    }
                }
            }
            if (paused || !landscape) {
                // An in-window menu instead of a dialog window keeps fullscreen and test
                // lookups on the game window across background and foreground changes.
                GameScrim {
                    GamePanel(
                        Modifier.widthIn(max = 560.dp).fillMaxWidth(0.85f),
                        accent = GameColors.Sky,
                    ) {
                        Text(
                            stringResource(
                                    if (landscape) R.string.game_paused else R.string.game_rotate
                                )
                                .uppercase(),
                            style = GameHeadingStyle.copy(fontSize = 30.sp, letterSpacing = 4.sp),
                        )
                        Column(
                            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(stringResource(R.string.game_pause_help), style = GameSmallStyle)
                            GameEquipmentSummary(s, assets)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            GameButton(
                                stringResource(R.string.game_resume),
                                { paused = false },
                                Modifier.weight(1f).testTag("game_resume"),
                                enabled = landscape && foreground,
                            )
                            GameButton(
                                stringResource(R.string.game_abandon),
                                {
                                    s.outcome = RunOutcome.ABANDONED
                                    onFinished(s)
                                },
                                Modifier.weight(1f).testTag("game_abandon"),
                                kind = GameButtonKind.DANGER,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Health, experience, time, kills, and the boss bar, drawn over the arena. */
@Composable
private fun BoxScope.GameHud(s: GameSession, level: Int, tick: Int, onPause: () -> Unit) {
    val levelLabel = stringResource(R.string.game_level, level)
    val pauseLabel = stringResource(R.string.game_pause)
    Row(Modifier.align(Alignment.TopStart).fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier.size(44.dp)
                    .clip(CircleShape)
                    .background(GameColors.Night)
                    .border(2.dp, GameColors.Android, CircleShape)
                    .semantics { contentDescription = levelLabel },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    level.toString(),
                    color = GameColors.Android,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                GameBar(
                    s.health.coerceAtLeast(0f) / s.maxHealth,
                    GameColors.Danger,
                    Modifier.width(180.dp).height(18.dp),
                    stringResource(
                        R.string.game_health,
                        s.health.coerceAtLeast(0f).toInt(),
                        s.maxHealth.toInt(),
                    ),
                )
                GameBar(
                    s.experience.toFloat() / s.requiredExperience,
                    GameColors.Sky,
                    Modifier.width(180.dp).height(8.dp),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "%02d:%02d".format(tick / 3600, tick / 60 % 60),
                Modifier.clip(RoundedCornerShape(12.dp))
                    .background(Color(0x88000000))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .testTag("game_time"),
                color = GameColors.Text,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace,
            )
            val boss = s.enemies.firstOrNull { it.kind == EnemyKind.BOSS }
            if (boss != null)
                GameBar(
                    boss.health / boss.maxHealth,
                    GameColors.Danger,
                    Modifier.padding(top = 6.dp).width(260.dp).height(12.dp),
                )
        }
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier.clip(RoundedCornerShape(12.dp))
                    .background(Color(0x88000000))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(GameColors.Danger))
                Text(
                    (s.regularKills + s.eliteKills + s.bossKills).toString(),
                    color = GameColors.Text,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                Modifier.size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x88000000))
                    .border(2.dp, Color(0x55ffffff), CircleShape)
                    .clickable(role = Role.Button, onClick = onPause)
                    .semantics { contentDescription = pauseLabel }
                    .testTag("game_pause"),
                contentAlignment = Alignment.Center,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(2) {
                        Box(
                            Modifier.size(width = 5.dp, height = 16.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(GameColors.Text)
                        )
                    }
                }
            }
        }
    }
}

/** A round skill key whose ring fills back up during the twenty-second cooldown. */
@Composable
private fun GameSkillButton(
    s: GameSession,
    playable: Boolean,
    modifier: Modifier,
    onSkill: () -> Unit,
) {
    val ready = s.skillTicks == 0
    val cooldown = s.skillTicks / (20f * GameEngine.TICKS_PER_SECOND)
    Box(
        modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(if (ready) GameColors.Android.copy(alpha = 0.9f) else Color(0xaa16223a))
            .clickable(enabled = playable && ready, role = Role.Button, onClick = onSkill)
            .drawWithContent {
                drawContent()
                val stroke = 5.dp.toPx()
                drawArc(
                    if (ready) Color.White.copy(alpha = 0.7f) else GameColors.Android,
                    -90f,
                    360f * (1f - cooldown),
                    false,
                    Offset(stroke / 2, stroke / 2),
                    Size(size.width - stroke, size.height - stroke),
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
            .testTag("game_skill"),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (ready) skillName(GameCatalog.skill(s.startingWeapon))
            else stringResource(R.string.game_skill_wait, (s.skillTicks + 59) / 60),
            Modifier.padding(8.dp),
            color = if (ready) GameColors.Night else GameColors.Text,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (ready) 16.sp else 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun GameJoystick(enabled: Boolean, modifier: Modifier, onMove: (Offset) -> Unit) {
    val description = stringResource(R.string.game_move)
    var knob by remember { mutableStateOf(Offset.Zero) }
    val latestMove by rememberUpdatedState(onMove)
    Canvas(
        modifier
            .size(128.dp)
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
        drawCircle(Color(0x66000000))
        drawCircle(Color(0x553ddc84), style = Stroke(3.dp.toPx()))
        drawCircle(Color(0x223ddc84), size.width * 0.35f)
        drawCircle(Color(0x66000000), size.width * 0.2f, center + knob + Offset(0f, 3.dp.toPx()))
        drawCircle(Color(0xffdfe8f5), size.width * 0.19f, center + knob)
        drawCircle(
            GameColors.Android,
            size.width * 0.19f,
            center + knob,
            style = Stroke(3.dp.toPx()),
        )
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
