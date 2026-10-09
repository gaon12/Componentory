package xyz.gaon.componentory.survivor

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import kotlin.math.floor
import kotlin.math.min

@Composable
internal fun GameBoard(
    session: GameSession,
    assets: GameAssets,
    frame: () -> Int,
    modifier: Modifier,
) {
    val painter = remember(assets) { BoardPainter(assets) }
    // Reading the frame here, in the draw phase, redraws only this canvas on each tick. Its own
    // layer keeps that redraw from re-recording the HUD and controls drawn above it.
    Canvas(modifier.graphicsLayer()) {
        val tick = frame()
        drawIntoCanvas { painter.draw(it.nativeCanvas, session, tick, size.width, size.height) }
    }
}

/**
 * Draws one frame with preallocated paints and shaders only, so a crowded frame does not allocate.
 * Sprite sizes and glows are visual; collisions stay in the engine.
 */
private class BoardPainter(private val assets: GameAssets) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val rect = RectF()
    private var spriteAlpha = 255

    // Unit glows are drawn at radius 100 and scaled, so one shader serves every size.
    private val greenGlow = glow(0x663ddc84.toInt())
    private val goldGlow = glow(0x77ffc94d.toInt())
    private val redGlow = glow(0x88ff5d6c.toInt())
    private val orangeGlow = glow(0xccffa94d.toInt())
    private val beanGlow = glow(0x66ff6b6b.toInt())
    private var vignette: RadialGradient? = null
    private var hurtEdge: RadialGradient? = null
    private var vignetteWidth = 0f
    private var vignetteHeight = 0f

    fun draw(canvas: Canvas, s: GameSession, tick: Int, width: Float, height: Float) {
        canvas.save()
        canvas.clipRect(0f, 0f, width, height)
        canvas.drawColor(0xff080d17.toInt())
        val scale = min(width / GameEngine.WIDTH, height / GameEngine.HEIGHT)
        val left = (width - GameEngine.WIDTH * scale) / 2
        val top = (height - GameEngine.HEIGHT * scale) / 2
        canvas.save()
        canvas.translate(left, top)
        canvas.scale(scale, scale)
        floor(canvas, -left / scale, -top / scale, (width - left) / scale, (height - top) / scale)
        for (drop in s.drops) {
            glow(canvas, beanGlow, drop.x, drop.y, 26f)
            sprite(canvas, "jellybean", drop.x, drop.y, 26f, tick * 2f + drop.x)
        }
        for (enemy in s.enemies) enemy(canvas, enemy, tick)
        for (shot in s.shots) {
            if (shot.delay > 0) continue
            if (shot.hostile) {
                glow(canvas, orangeGlow, shot.x, shot.y, 34f)
                fill.color = 0xffffe0b0.toInt()
                canvas.drawCircle(shot.x, shot.y, 9f, fill)
            }
            sprite(
                canvas,
                shot.art,
                shot.x,
                shot.y,
                if (shot.radius > 100) shot.radius * 2 else if (shot.orbit) 52f else 30f,
                shot.angle * 57.29578f,
            )
        }
        player(canvas, s, tick)
        canvas.restore()
        overlay(canvas, s, width, height)
        canvas.restore()
    }

    /** A two-tone tiled floor that continues past the arena, with a bright arena edge. */
    private fun floor(canvas: Canvas, x0: Float, y0: Float, x1: Float, y1: Float) {
        val cell = 100f
        // Outside the arena stays the cleared background. Inside, one base rectangle plus
        // every other cell gives the checker pattern with about 70 draw calls.
        fill.color = 0xff0f1826.toInt()
        canvas.drawRect(0f, 0f, GameEngine.WIDTH, GameEngine.HEIGHT, fill)
        fill.color = 0xff111b2b.toInt()
        for (row in 0 until (GameEngine.HEIGHT / cell).toInt()) for (column in
            row % 2 until (GameEngine.WIDTH / cell).toInt() step 2) canvas.drawRect(
            column * cell,
            row * cell,
            (column + 1) * cell,
            (row + 1) * cell,
            fill,
        )
        line.color = 0x14ffffff
        line.strokeWidth = 1.5f
        var x = floor(x0 / cell) * cell
        while (x < x1) {
            canvas.drawLine(x, y0, x, y1, line)
            x += cell
        }
        var y = floor(y0 / cell) * cell
        while (y < y1) {
            canvas.drawLine(x0, y, x1, y, line)
            y += cell
        }
        line.color = 0x553ddc84
        line.strokeWidth = 6f
        canvas.drawRect(0f, 0f, GameEngine.WIDTH, GameEngine.HEIGHT, line)
        line.color = 0xaa3ddc84.toInt()
        line.strokeWidth = 2f
        canvas.drawRect(0f, 0f, GameEngine.WIDTH, GameEngine.HEIGHT, line)
    }

    private fun enemy(canvas: Canvas, enemy: GameEnemy, tick: Int) {
        val size =
            when (enemy.kind) {
                EnemyKind.NORMAL -> 52f
                EnemyKind.ELITE -> 76f
                EnemyKind.BOSS -> 128f
            }
        shadow(canvas, enemy.x, enemy.y + size * 0.42f, size * 0.36f)
        when (enemy.kind) {
            EnemyKind.ELITE -> glow(canvas, goldGlow, enemy.x, enemy.y, size * 0.75f)
            EnemyKind.BOSS ->
                glow(canvas, redGlow, enemy.x, enemy.y, size * (0.8f + (tick % 60) / 300f))
            EnemyKind.NORMAL -> Unit
        }
        sprite(canvas, enemy.source.art, enemy.x, enemy.y, size)
        val fraction = (enemy.health / enemy.maxHealth).coerceIn(0f, 1f)
        if (enemy.kind == EnemyKind.BOSS || fraction < 1f) {
            val top = enemy.y - size / 2 - 12
            fill.color = 0xaa000000.toInt()
            rect.set(enemy.x - size / 2, top, enemy.x + size / 2, top + 7)
            canvas.drawRoundRect(rect, 4f, 4f, fill)
            fill.color =
                if (enemy.kind == EnemyKind.BOSS) 0xffff5d6c.toInt() else 0xffff8a80.toInt()
            rect.set(
                enemy.x - size / 2 + 1,
                top + 1,
                enemy.x - size / 2 + 1 + (size - 2) * fraction,
                top + 6,
            )
            canvas.drawRoundRect(rect, 3f, 3f, fill)
        }
    }

    private fun player(canvas: Canvas, s: GameSession, tick: Int) {
        shadow(canvas, s.x, s.y + 32f, 28f)
        glow(canvas, greenGlow, s.x, s.y, 64f)
        if (s.shieldTicks > 0) {
            line.color = 0x8859c7ff.toInt()
            line.strokeWidth = 4f
            canvas.drawCircle(s.x, s.y, 62f, line)
            sprite(canvas, "switch", s.x, s.y, 120f, tick * 0.2f)
        }
        // Blink while the engine's post-hit invulnerability runs.
        val blink = s.hurtTicks > 0 && (tick / 4) % 2 == 0
        spriteAlpha = if (blink) 90 else 255
        sprite(canvas, GameCatalog.PLAYER_ART, s.x, s.y, 76f)
        spriteAlpha = 255
    }

    /** Screen-space effects: freeze tint, a red flash after damage, and an edge vignette. */
    private fun overlay(canvas: Canvas, s: GameSession, width: Float, height: Float) {
        if (s.freezeTicks > 0) {
            fill.color = 0x2a66ccff
            canvas.drawRect(0f, 0f, width, height, fill)
        }
        val radius = maxOf(width, height) * 0.75f
        if (vignette == null || vignetteWidth != width || vignetteHeight != height) {
            vignette = edge(width, height, radius, 0xaa000000.toInt())
            hurtEdge = edge(width, height, radius, 0xffff2030.toInt())
            vignetteWidth = width
            vignetteHeight = height
        }
        // A red edge, not a full tint, so a frame paused just after a hit stays readable.
        if (s.hurtTicks > 25) {
            fill.shader = hurtEdge
            fill.alpha = (s.hurtTicks - 25) * 10
            canvas.drawRect(0f, 0f, width, height, fill)
            fill.alpha = 255
        }
        fill.shader = vignette
        canvas.drawRect(0f, 0f, width, height, fill)
        fill.shader = null
    }

    private fun shadow(canvas: Canvas, x: Float, y: Float, radius: Float) {
        fill.color = 0x55000000
        rect.set(x - radius, y - radius * 0.35f, x + radius, y + radius * 0.35f)
        canvas.drawOval(rect, fill)
    }

    private fun glow(canvas: Canvas, shader: RadialGradient, x: Float, y: Float, radius: Float) {
        canvas.withScale(radius / 100f, radius / 100f, x, y) {
            fill.shader = shader
            drawCircle(x, y, 100f, fill)
            fill.shader = null
        }
    }

    private fun sprite(
        canvas: Canvas,
        key: String,
        x: Float,
        y: Float,
        size: Float,
        rotation: Float = 0f,
    ) {
        canvas.withRotation(rotation, x, y) {
            rect.set(x - size / 2, y - size / 2, x + size / 2, y + size / 2)
            paint.color = (spriteAlpha shl 24) or 0xffffff
            drawBitmap(assets.bitmap(key), null, rect, paint)
        }
    }

    private companion object {
        fun edge(width: Float, height: Float, radius: Float, color: Int) =
            RadialGradient(
                width / 2,
                height / 2,
                radius,
                intArrayOf(0, 0, color),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP,
            )

        fun glow(color: Int) =
            RadialGradient(
                0f,
                0f,
                100f,
                intArrayOf(color, color and 0x00ffffff),
                null,
                Shader.TileMode.CLAMP,
            )
    }
}
