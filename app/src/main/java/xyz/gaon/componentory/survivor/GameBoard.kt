package xyz.gaon.componentory.survivor

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.min

@Composable
internal fun GameBoard(session: GameSession, assets: GameAssets, tick: Int, modifier: Modifier) {
    val painter = remember(assets) { BoardPainter(assets) }
    Canvas(modifier) {
        drawIntoCanvas { painter.draw(it.nativeCanvas, session, tick, size.width, size.height) }
    }
}

private class BoardPainter(private val assets: GameAssets) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val rect = RectF()

    fun draw(canvas: Canvas, s: GameSession, tick: Int, width: Float, height: Float) {
        canvas.save()
        canvas.clipRect(0f, 0f, width, height)
        canvas.drawColor(0xff101924.toInt())
        val scale = min(width / GameEngine.WIDTH, height / GameEngine.HEIGHT)
        canvas.translate(
            (width - GameEngine.WIDTH * scale) / 2,
            (height - GameEngine.HEIGHT * scale) / 2,
        )
        canvas.scale(scale, scale)
        paint.color = 0xff233343.toInt()
        paint.strokeWidth = 1.5f
        for (x in 0..1600 step 100) canvas.drawLine(x.toFloat(), 0f, x.toFloat(), 900f, paint)
        for (y in 0..900 step 100) canvas.drawLine(0f, y.toFloat(), 1600f, y.toFloat(), paint)
        for (drop in s.drops) sprite(canvas, "jellybean", drop.x, drop.y, 28f)
        for (enemy in s.enemies) {
            val size =
                when (enemy.kind) {
                    EnemyKind.NORMAL -> 48f
                    EnemyKind.ELITE -> 72f
                    EnemyKind.BOSS -> 120f
                }
            sprite(canvas, enemy.source.art, enemy.x, enemy.y, size)
            paint.color = 0xffed7777.toInt()
            canvas.drawRect(
                enemy.x - size / 2,
                enemy.y - size / 2 - 8,
                enemy.x - size / 2 + size * (enemy.health / enemy.maxHealth).coerceIn(0f, 1f),
                enemy.y - size / 2 - 3,
                paint,
            )
        }
        for (shot in s.shots) {
            if (shot.delay > 0) continue
            if (shot.hostile) {
                paint.color = 0xffffa94d.toInt()
                canvas.drawCircle(shot.x, shot.y, 20f, paint)
            }
            sprite(
                canvas,
                shot.art,
                shot.x,
                shot.y,
                if (shot.radius > 100) shot.radius * 2 else if (shot.orbit) 48f else 28f,
                shot.angle * 57.29578f,
            )
        }
        if (s.shieldTicks > 0) sprite(canvas, "switch", s.x, s.y, 120f, tick * 0.2f)
        sprite(canvas, GameCatalog.PLAYER_ART, s.x, s.y, 72f)
        if (s.freezeTicks > 0) {
            paint.color = 0x2266ccff
            canvas.drawRect(0f, 0f, 1600f, 900f, paint)
        }
        canvas.restore()
    }

    private fun sprite(
        canvas: Canvas,
        key: String,
        x: Float,
        y: Float,
        size: Float,
        rotation: Float = 0f,
    ) {
        canvas.save()
        canvas.rotate(rotation, x, y)
        rect.set(x - size / 2, y - size / 2, x + size / 2, y + size / 2)
        paint.color = android.graphics.Color.WHITE
        canvas.drawBitmap(assets.bitmap(key), null, rect, paint)
        canvas.restore()
    }
}
