package xyz.gaon.componentory.survivor

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
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

    // Glows are small prerendered bitmaps stretched to each size. Unlike a gradient circle in a
    // scaled save layer, repeated bitmap draws need no save and restore and batch on the GPU.
    private val glowPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val greenGlow = glow(0x663ddc84.toInt())
    private val goldGlow = glow(0x77ffc94d.toInt())
    private val redGlow = glow(0x88ff5d6c.toInt())
    private val orangeGlow = glow(0xccffa94d.toInt())
    private val beanGlow = glow(0x66ff6b6b.toInt())
    private var vignette: RadialGradient? = null
    private var hurtEdge: RadialGradient? = null
    private var vignetteWidth = 0f
    private var vignetteHeight = 0f

    // The checker floor is one rectangle with a 2x2 pixel shader scaled to 100 units per cell.
    // Drawing each cell separately cost about 70 draw calls a frame.
    private val checker =
        Paint().apply {
            shader =
                BitmapShader(
                        Bitmap.createBitmap(
                            intArrayOf(LIGHT_CELL, DARK_CELL, DARK_CELL, LIGHT_CELL),
                            2,
                            2,
                            Bitmap.Config.ARGB_8888,
                        ),
                        Shader.TileMode.REPEAT,
                        Shader.TileMode.REPEAT,
                    )
                    .apply { setLocalMatrix(Matrix().apply { setScale(CELL, CELL) }) }
        }
    // Grid lines change only with the canvas size, so they are built once and drawn in one call.
    private var gridLines = FloatArray(0)
    private var gridWidth = 0f
    private var gridHeight = 0f

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
        if (gridWidth != width || gridHeight != height) {
            gridLines =
                grid(-left / scale, -top / scale, (width - left) / scale, (height - top) / scale)
            gridWidth = width
            gridHeight = height
        }
        floor(canvas)
        for (drop in s.drops) {
            glow(canvas, beanGlow, drop.x, drop.y, 26f)
            sprite(canvas, "jellybean", drop.x, drop.y, 26f, tick * 2f + drop.x)
        }
        enemies(canvas, s.enemies, tick)
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

    /** A two-tone tiled floor with grid lines that continue past the arena, and a bright edge. */
    private fun floor(canvas: Canvas) {
        canvas.drawRect(0f, 0f, GameEngine.WIDTH, GameEngine.HEIGHT, checker)
        line.color = 0x14ffffff
        line.strokeWidth = 1.5f
        canvas.drawLines(gridLines, line)
        line.color = 0x553ddc84
        line.strokeWidth = 6f
        canvas.drawRect(0f, 0f, GameEngine.WIDTH, GameEngine.HEIGHT, line)
        line.color = 0xaa3ddc84.toInt()
        line.strokeWidth = 2f
        canvas.drawRect(0f, 0f, GameEngine.WIDTH, GameEngine.HEIGHT, line)
    }

    /**
     * Draws enemies in layers: all shadows, then auras, sprites, and health bars. Keeping each kind
     * of draw together lets the GPU merge them, which matters with 150 enemies on screen.
     */
    private fun enemies(canvas: Canvas, enemies: List<GameEnemy>, tick: Int) {
        for (enemy in enemies) {
            val size = size(enemy)
            shadow(canvas, enemy.x, enemy.y + size * 0.42f, size * 0.36f)
        }
        for (enemy in enemies) {
            val size = size(enemy)
            when (enemy.kind) {
                EnemyKind.ELITE -> glow(canvas, goldGlow, enemy.x, enemy.y, size * 0.75f)
                EnemyKind.BOSS ->
                    glow(canvas, redGlow, enemy.x, enemy.y, size * (0.8f + (tick % 60) / 300f))
                EnemyKind.NORMAL -> Unit
            }
        }
        for (enemy in enemies) sprite(canvas, enemy.source.art, enemy.x, enemy.y, size(enemy))
        for (enemy in enemies) {
            val size = size(enemy)
            val fraction = (enemy.health / enemy.maxHealth).coerceIn(0f, 1f)
            if (enemy.kind != EnemyKind.BOSS && fraction >= 1f) continue
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

    private fun size(enemy: GameEnemy) =
        when (enemy.kind) {
            EnemyKind.NORMAL -> 52f
            EnemyKind.ELITE -> 76f
            EnemyKind.BOSS -> 128f
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

    private fun glow(canvas: Canvas, glow: Bitmap, x: Float, y: Float, radius: Float) {
        rect.set(x - radius, y - radius, x + radius, y + radius)
        canvas.drawBitmap(glow, null, rect, glowPaint)
    }

    private fun sprite(
        canvas: Canvas,
        key: String,
        x: Float,
        y: Float,
        size: Float,
        rotation: Float = 0f,
    ) {
        rect.set(x - size / 2, y - size / 2, x + size / 2, y + size / 2)
        paint.color = (spriteAlpha shl 24) or 0xffffff
        // Most sprites are upright; skipping the save and restore keeps the frame shorter.
        if (rotation == 0f) canvas.drawBitmap(assets.bitmap(key), null, rect, paint)
        else
            canvas.withRotation(rotation, x, y) {
                drawBitmap(assets.bitmap(key), null, rect, paint)
            }
    }

    private companion object {
        const val CELL = 100f
        const val LIGHT_CELL = 0xff111b2b.toInt()
        const val DARK_CELL = 0xff0f1826.toInt()

        /** Grid lines on every cell edge across the visible area, as drawLines pairs. */
        fun grid(x0: Float, y0: Float, x1: Float, y1: Float): FloatArray {
            val lines = ArrayList<Float>()
            var x = floor(x0 / CELL) * CELL
            while (x < x1) {
                lines += listOf(x, y0, x, y1)
                x += CELL
            }
            var y = floor(y0 / CELL) * CELL
            while (y < y1) {
                lines += listOf(x0, y, x1, y)
                y += CELL
            }
            return lines.toFloatArray()
        }

        fun edge(width: Float, height: Float, radius: Float, color: Int) =
            RadialGradient(
                width / 2,
                height / 2,
                radius,
                intArrayOf(0, 0, color),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP,
            )

        /** A 64-pixel soft circle that fades from [color] to transparent at its edge. */
        fun glow(color: Int): Bitmap {
            val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
            val paint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader =
                        RadialGradient(
                            32f,
                            32f,
                            32f,
                            intArrayOf(color, color and 0x00ffffff),
                            null,
                            Shader.TileMode.CLAMP,
                        )
                }
            Canvas(bitmap).drawCircle(32f, 32f, 32f, paint)
            return bitmap
        }
    }
}
