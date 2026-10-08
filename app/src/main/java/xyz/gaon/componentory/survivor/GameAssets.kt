package xyz.gaon.componentory.survivor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.android_n.egg.neko.Cat
import com.android_o.egg.octo.OctopusDrawable
import xyz.gaon.componentory.R
import xyz.gaon.componentory.eastereggs.port.R as EggR

/** Battle frames only read these cached bitmaps, never load a resource again. */
internal class GameAssets(private val context: Context) {
    private val cache = mutableMapOf<String, Bitmap>()

    fun bitmap(key: String): Bitmap =
        cache.getOrPut(key) {
            val drawable: Drawable =
                when (key) {
                    "neko" -> Cat(context, 20261008L)
                    "octopus" -> OctopusDrawable(context)
                    else -> checkNotNull(context.getDrawable(resource(key))).mutate()
                }
            drawable.state =
                intArrayOf(
                    android.R.attr.state_enabled,
                    android.R.attr.state_window_focused,
                    android.R.attr.state_checked,
                )
            drawable.level = 10000
            val bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
            val aspect =
                if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0)
                    drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight
                else 1f
            val width = if (aspect >= 1f) 96 else (96 * aspect).toInt().coerceAtLeast(1)
            val height = if (aspect >= 1f) (96 / aspect).toInt().coerceAtLeast(1) else 96
            drawable.setBounds(
                (96 - width) / 2,
                (96 - height) / 2,
                (96 + width) / 2,
                (96 + height) / 2,
            )
            drawable.draw(Canvas(bitmap))
            bitmap
        }

    private fun resource(key: String): Int =
        when (key) {
            "classic",
            "button" -> R.drawable.aosp_classic_btn_default
            "holo" -> R.drawable.aosp_holo_btn_default_holo_light
            "slider" -> R.drawable.aosp_classic_seek_thumb
            "switch" -> R.drawable.aosp_holo_btn_toggle_holo_light
            "spinner" -> R.drawable.game_holo_progress_medium_holo
            "progress" -> R.drawable.aosp_classic_progress_horizontal
            "gingerbread" -> EggR.drawable.g_platlogo
            "honeycomb" -> EggR.drawable.h_platlogo
            "icecream" -> EggR.drawable.i_nyandroid00
            "jellybean" -> EggR.drawable.j_redbean0
            "kitkat" -> EggR.drawable.k_platlogo
            "lollipop" -> EggR.drawable.l_platlogo
            "marshmallow" -> EggR.drawable.m_platlogo
            "pie" -> EggR.drawable.p
            "q" -> EggR.drawable.q
            "r" -> EggR.drawable.r_android_11_dial
            "s" -> EggR.drawable.s_android_logo
            "t" -> EggR.drawable.t_emoji_u2764
            "u" -> EggR.drawable.u_platlogo
            "v" -> EggR.drawable.v_android_logo_2024_
            "baklava" -> EggR.drawable.baklava_platlogo
            "cinnamon" -> EggR.drawable.cinnamon_bun_platlogo
            else -> error("Unknown game artwork: " + key)
        }
}
