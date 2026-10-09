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
    private val batch = SpriteBatch(assets)

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
        for (drop in s.drops) glow(canvas, beanGlow, drop.x, drop.y, 26f)
        for (drop in s.drops) batch.add("jellybean", drop.x, drop.y, 26f, tick * 2f + drop.x)
        batch.draw(canvas)
        enemies(canvas, s.enemies, tick)
        for (shot in s.shots) {
            if (shot.delay > 0 || !shot.hostile) continue
            glow(canvas, orangeGlow, shot.x, shot.y, 34f)
            fill.color = 0xffffe0b0.toInt()
            canvas.drawCircle(shot.x, shot.y, 9f, fill)
        }
        for (shot in s.shots) {
            if (shot.delay > 0) continue
            batch.add(
                shot.art,
                shot.x,
                shot.y,
                if (shot.radius > 100) shot.radius * 2 else if (shot.orbit) 52f else 30f,
                shot.angle * 57.29578f,
            )
        }
        batch.draw(canvas)
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
            val radius = size * 0.36f
            val y = enemy.y + size * 0.42f
            batch.addRect(
                SpriteBatch.SHADOW,
                enemy.x - radius,
                y - radius * 0.35f,
                enemy.x + radius,
                y + radius * 0.35f,
            )
        }
        batch.draw(canvas)
        for (enemy in enemies) {
            val size = size(enemy)
            when (enemy.kind) {
                EnemyKind.ELITE -> glow(canvas, goldGlow, enemy.x, enemy.y, size * 0.75f)
                EnemyKind.BOSS ->
                    glow(canvas, redGlow, enemy.x, enemy.y, size * (0.8f + (tick % 60) / 300f))
                EnemyKind.NORMAL -> Unit
            }
        }
        for (enemy in enemies) batch.add(enemy.source.art, enemy.x, enemy.y, size(enemy))
        batch.draw(canvas)
        for (enemy in enemies) {
            val size = size(enemy)
            val fraction = (enemy.health / enemy.maxHealth).coerceIn(0f, 1f)
            if (enemy.kind != EnemyKind.BOSS && fraction >= 1f) continue
            val top = enemy.y - size / 2 - 12
            val left = enemy.x - size / 2
            batch.addRect(SpriteBatch.BAR_BACK, left, top, left + size, top + 7)
            batch.addRect(
                if (enemy.kind == EnemyKind.BOSS) SpriteBatch.BOSS_BAR else SpriteBatch.ENEMY_BAR,
                left + 1,
                top + 1,
                left + 1 + (size - 2) * fraction,
                top + 6,
            )
        }
        batch.draw(canvas)
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
        // The player and shield are drawn alone because their alpha or order differs.
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

/**
 * Collects many sprites and draws them with one drawVertices call. Each artwork is copied once into
 * a shared atlas bitmap, and each sprite becomes two textured triangles. A crowd of 150 enemies is
 * then one draw instead of 150, which shortens both recording and GPU work.
 */
private class SpriteBatch(private val assets: GameAssets) {
    private val atlas = Bitmap.createBitmap(ATLAS, ATLAS, Bitmap.Config.ARGB_8888)
    private val atlasCanvas = Canvas(atlas)
    private val slots = HashMap<String, Int>()
    private val cell = RectF()
    private val shape = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            shader = BitmapShader(atlas, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
    private val vertices = FloatArray(LIMIT * 8)
    private val textures = FloatArray(LIMIT * 8)
    private val indices =
        ShortArray(LIMIT * 6).also {
            for (sprite in 0 until LIMIT) {
                val first = sprite * 4
                val index = sprite * 6
                it[index] = first.toShort()
                it[index + 1] = (first + 1).toShort()
                it[index + 2] = (first + 2).toShort()
                it[index + 3] = first.toShort()
                it[index + 4] = (first + 2).toShort()
                it[index + 5] = (first + 3).toShort()
            }
        }
    private var count = 0

    /** Queues an upright rectangle filled with a built-in shape such as [SHADOW]. */
    fun addRect(shape: String, left: Float, top: Float, right: Float, bottom: Float) {
        if (count == LIMIT) return
        val slot = slot(shape) ?: return
        val at = count * 8
        vertices[at] = left
        vertices[at + 1] = top
        vertices[at + 2] = right
        vertices[at + 3] = top
        vertices[at + 4] = right
        vertices[at + 5] = bottom
        vertices[at + 6] = left
        vertices[at + 7] = bottom
        // Solid colors sample the middle of their cell, so filtering never reaches the edge.
        val inset = if (shape == SHADOW) 0f else SPRITE / 2f - 1
        texture(at, slot, inset)
        count++
    }

    /** Queues one sprite. Sprites past [LIMIT] in one batch are skipped. */
    fun add(key: String, x: Float, y: Float, size: Float, rotation: Float = 0f) {
        if (count == LIMIT) return
        val slot = slot(key) ?: return
        val half = size / 2
        var cos = 1f
        var sin = 0f
        if (rotation != 0f) {
            val radians = Math.toRadians(rotation.toDouble())
            cos = kotlin.math.cos(radians).toFloat()
            sin = kotlin.math.sin(radians).toFloat()
        }
        // Corners in order: top left, top right, bottom right, bottom left.
        val at = count * 8
        corner(at, x, y, -half, -half, cos, sin)
        corner(at + 2, x, y, half, -half, cos, sin)
        corner(at + 4, x, y, half, half, cos, sin)
        corner(at + 6, x, y, -half, half, cos, sin)
        texture(at, slot, 0f)
        count++
    }

    private fun texture(at: Int, slot: Int, inset: Float) {
        val left = slot % COLUMNS * CELL + PAD + inset
        val top = slot / COLUMNS * CELL + PAD + inset
        val right = left + SPRITE - 2 * inset
        val bottom = top + SPRITE - 2 * inset
        textures[at] = left
        textures[at + 1] = top
        textures[at + 2] = right
        textures[at + 3] = top
        textures[at + 4] = right
        textures[at + 5] = bottom
        textures[at + 6] = left
        textures[at + 7] = bottom
    }

    fun draw(canvas: Canvas) {
        if (count == 0) return
        canvas.drawVertices(
            Canvas.VertexMode.TRIANGLES,
            count * 8,
            vertices,
            0,
            textures,
            0,
            null,
            0,
            indices,
            0,
            count * 6,
            paint,
        )
        count = 0
    }

    private fun corner(at: Int, x: Float, y: Float, dx: Float, dy: Float, cos: Float, sin: Float) {
        vertices[at] = x + dx * cos - dy * sin
        vertices[at + 1] = y + dx * sin + dy * cos
    }

    /**
     * The atlas cell for [key], copying the artwork in on first use. Null when the atlas is full.
     */
    private fun slot(key: String): Int? {
        slots[key]?.let {
            return it
        }
        if (slots.size == COLUMNS * COLUMNS) return null
        val slot = slots.size
        val left = (slot % COLUMNS * CELL + PAD).toFloat()
        val top = (slot / COLUMNS * CELL + PAD).toFloat()
        cell.set(left, top, left + SPRITE, top + SPRITE)
        when (key) {
            SHADOW -> atlasCanvas.drawOval(cell, shape.apply { color = 0x55000000 })
            BAR_BACK -> atlasCanvas.drawRect(cell, shape.apply { color = 0xaa000000.toInt() })
            ENEMY_BAR -> atlasCanvas.drawRect(cell, shape.apply { color = 0xffff8a80.toInt() })
            BOSS_BAR -> atlasCanvas.drawRect(cell, shape.apply { color = 0xffff5d6c.toInt() })
            else -> atlasCanvas.drawBitmap(assets.bitmap(key), null, cell, null)
        }
        slots[key] = slot
        return slot
    }

    companion object {
        // Built-in shapes share the atlas with artwork. Artwork keys never start with "#".
        const val SHADOW = "#shadow"
        const val BAR_BACK = "#bar-back"
        const val ENEMY_BAR = "#enemy-bar"
        const val BOSS_BAR = "#boss-bar"
        private const val SPRITE = 96
        // A transparent pixel on each side keeps filtering from bleeding between sprites.
        private const val PAD = 1
        private const val CELL = SPRITE + 2 * PAD
        private const val COLUMNS = 10
        private const val ATLAS = CELL * COLUMNS
        // Enemies and shots are capped at 160 and 256, so one batch never needs more.
        private const val LIMIT = 512
    }
}
