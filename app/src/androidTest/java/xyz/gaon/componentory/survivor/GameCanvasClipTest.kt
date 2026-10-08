package xyz.gaon.componentory.survivor

import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

class GameCanvasClipTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun nativeBoardPaintingCannotEraseNeighboringComposeControls() {
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    Row(Modifier.safeDrawingPadding()) {
                        Box(Modifier.size(40.dp).background(Color.Red).testTag("game_paint_guard"))
                        GameBoard(
                            GameEngine.create(WeaponId.BUTTON, RunMode.NORMAL).session,
                            remember { GameAssets(compose.activity) },
                            0,
                            Modifier.size(200.dp),
                        )
                    }
                }
            }
        }
        val pixels = compose.onNodeWithTag("game_paint_guard").captureToImage().toPixelMap()
        assertEquals(Color.Red.toArgb(), pixels[pixels.width / 2, pixels.height / 2].toArgb())
    }
}
