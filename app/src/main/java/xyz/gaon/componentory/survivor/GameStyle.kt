package xyz.gaon.componentory.survivor

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

/**
 * The game keeps one dark arcade look in both app themes. The rest of Componentory follows the
 * user's light or dark setting, but a game arena reads better with a fixed night palette.
 */
internal object GameColors {
    val Night = Color(0xff070b14)
    val Deep = Color(0xff0e1626)
    val Panel = Color(0xf0141e33)
    val PanelEdge = Color(0xff2b3d5f)
    val Android = Color(0xff3ddc84)
    val AndroidDark = Color(0xff1e9e5a)
    val Gold = Color(0xffffc94d)
    val Danger = Color(0xffff5d6c)
    val Sky = Color(0xff59c7ff)
    val Text = Color(0xffeef4ff)
    val Muted = Color(0xff97a6c3)
}

/** Material parts inside the game, such as records and rankings, use the same night palette. */
internal val GameColorScheme =
    darkColorScheme(
        primary = GameColors.Android,
        onPrimary = GameColors.Night,
        primaryContainer = Color(0xff183a2c),
        onPrimaryContainer = GameColors.Text,
        secondary = GameColors.Sky,
        tertiary = GameColors.Gold,
        error = GameColors.Danger,
        background = GameColors.Night,
        onBackground = GameColors.Text,
        surface = GameColors.Deep,
        onSurface = GameColors.Text,
        onSurfaceVariant = GameColors.Muted,
        surfaceContainer = Color(0xff141e33),
        surfaceContainerHigh = Color(0xff1a2640),
        outline = GameColors.PanelEdge,
    )

internal val GameTitleStyle =
    TextStyle(
        color = GameColors.Text,
        fontWeight = FontWeight.Black,
        fontSize = 44.sp,
        letterSpacing = 2.sp,
        shadow = Shadow(GameColors.Android.copy(alpha = 0.6f), Offset.Zero, 24f),
    )

internal val GameHeadingStyle =
    TextStyle(
        color = GameColors.Text,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        letterSpacing = 1.sp,
    )

internal val GameBodyStyle = TextStyle(color = GameColors.Text, fontSize = 14.sp)

internal val GameSmallStyle = TextStyle(color = GameColors.Muted, fontSize = 12.sp)

internal enum class GameButtonKind {
    PRIMARY,
    SECONDARY,
    DANGER,
}

/** A chunky button with a raised edge that sinks when pressed, like an arcade key. */
@Composable
internal fun GameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    kind: GameButtonKind = GameButtonKind.PRIMARY,
    large: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val (face, edge, ink) =
        when (kind) {
            GameButtonKind.PRIMARY ->
                Triple(GameColors.Android, GameColors.AndroidDark, GameColors.Night)
            GameButtonKind.SECONDARY ->
                Triple(Color(0xff26385a), Color(0xff16223a), GameColors.Text)
            GameButtonKind.DANGER -> Triple(GameColors.Danger, Color(0xffa8313d), GameColors.Night)
        }
    val depth = if (large) 6.dp else 4.dp
    val shape = RoundedCornerShape(if (large) 18.dp else 12.dp)
    Box(
        modifier
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(edge)
            .clickable(source, null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(bottom = if (pressed) 1.dp else depth)
            .offset { IntOffset(0, if (pressed) (depth - 1.dp).roundToPx() else 0) }
            .clip(shape)
            .background(Brush.verticalGradient(listOf(face, face.copy(alpha = 0.85f))))
            .padding(
                horizontal = if (large) 32.dp else 16.dp,
                vertical = if (large) 14.dp else 10.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            leading?.invoke()
            Text(
                text,
                color = ink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (large) 26.sp else 15.sp,
                letterSpacing = if (large) 3.sp else 0.5.sp,
                maxLines = 1,
            )
        }
    }
}

/** A translucent framed panel used by menus that float over the arena or title backdrop. */
@Composable
internal fun GamePanel(
    modifier: Modifier = Modifier,
    accent: Color = GameColors.PanelEdge,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(GameColors.Panel)
            .border(BorderStroke(2.dp, accent), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Material text inside the panel, such as the ranking status, must stay readable.
        CompositionLocalProvider(LocalContentColor provides GameColors.Text) { content() }
    }
}

/** Dims the screen behind a menu and keeps taps from reaching the controls underneath. */
@Composable
internal fun GameScrim(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(Color(0xcc03060c))
            // Swallow taps without semantics, so the panel's own nodes stay separate.
            .pointerInput(Unit) { detectTapGestures {} }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/** A rounded progress bar with a bright fill, used for health and experience. */
@Composable
internal fun GameBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val shown by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(160), label = "bar")
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xaa000000))
            .border(1.5.dp, Color(0x55ffffff), RoundedCornerShape(50)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.matchParentSize()
                .padding(2.dp)
                .fillMaxWidth(shown)
                .clip(RoundedCornerShape(50))
                .background(Brush.verticalGradient(listOf(color, color.copy(alpha = 0.7f))))
                .align(Alignment.CenterStart)
        )
        if (label != null)
            Text(
                label,
                color = GameColors.Text,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                style = TextStyle(shadow = Shadow(Color.Black, Offset(0f, 1f), 3f)),
            )
    }
}

/**
 * The animated title background. A slowly scrolling grid and drifting enemy artwork suggest the
 * arena without starting a simulation. The sprites use the same cached bitmaps as combat.
 */
@Composable
internal fun GameBackdrop(assets: GameAssets, modifier: Modifier = Modifier) {
    val drifting =
        remember(assets) {
            listOf("gingerbread", "honeycomb", "kitkat", "lollipop", "marshmallow", "pie", "q", "r")
                .map { assets.bitmap(it).asImageBitmap() }
        }
    val time by
        rememberInfiniteTransition(label = "backdrop")
            .animateFloat(
                0f,
                1f,
                infiniteRepeatable(tween(24000, easing = LinearEasing)),
                label = "time",
            )
    Canvas(modifier.fillMaxSize()) {
        drawRect(
            Brush.radialGradient(
                listOf(Color(0xff142542), GameColors.Night),
                center = Offset(size.width * 0.3f, size.height * 0.4f),
                radius = size.maxDimension * 0.8f,
            )
        )
        val cell = 64.dp.toPx()
        val shift = time * cell * 8 % cell
        var x = -shift
        while (x < size.width) {
            drawLine(Color(0x1659c7ff), Offset(x, 0f), Offset(x, size.height), 1.5f)
            x += cell
        }
        var y = shift
        while (y < size.height + cell) {
            drawLine(Color(0x1659c7ff), Offset(0f, y - cell), Offset(size.width, y - cell), 1.5f)
            y += cell
        }
        val sprite = 56.dp.toPx()
        drifting.forEachIndexed { index, image ->
            val lane = (index + 0.5f) / drifting.size
            val phase = (time * (0.6f + index % 3 * 0.25f) + index * 0.37f) % 1f
            val px = size.width * (1.1f - phase * 1.2f)
            val py = size.height * lane + sin((phase + index) * 6.283f) * sprite * 0.4f
            drawImage(
                image,
                dstOffset = IntOffset((px - sprite / 2).toInt(), (py - sprite / 2).toInt()),
                dstSize = IntSize(sprite.toInt(), sprite.toInt()),
                alpha = 0.22f,
            )
        }
        drawRect(
            Brush.verticalGradient(
                listOf(Color.Transparent, Color(0xcc03060c)),
                startY = size.height * 0.55f,
            )
        )
        drawCircle(
            GameColors.Android.copy(alpha = 0.10f),
            size.minDimension * 0.42f,
            Offset(size.width * 0.27f, size.height * 0.45f),
            style = Stroke(2.dp.toPx()),
        )
    }
}

/** A gentle repeating pulse for the main call to action. */
@Composable
internal fun Modifier.gamePulse(enabled: Boolean): Modifier {
    if (!enabled) return this
    val pulse by
        rememberInfiniteTransition(label = "pulse")
            .animateFloat(
                1f,
                1.05f,
                infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "scale",
            )
    return scale(pulse)
}

/** A gentle bob for sprites shown on menus. */
@Composable
internal fun Modifier.gameBob(): Modifier {
    val bob by
        rememberInfiniteTransition(label = "bob")
            .animateFloat(
                -4f,
                4f,
                infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "offset",
            )
    return offset { IntOffset(0, bob.dp.roundToPx()) }
}
