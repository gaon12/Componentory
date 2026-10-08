package xyz.gaon.componentory.eastereggs

import androidx.compose.ui.geometry.Offset
import com.android_baklava.egg.landroid.rotate as rotateBaklava
import com.android_cinnamon_bun.egg.landroid.rotate as rotateCinnamonBun
import com.android_u.egg.landroid.rotate as rotateUpsideDownCake
import com.android_v.egg.landroid.rotate as rotateVanillaIceCream
import kotlin.math.PI
import org.junit.Assert.assertEquals
import org.junit.Test

class SpaceGameMathTest {
    @Test
    fun allSpaceGamesRotateAroundTheRequestedOriginAndRoundTrip() {
        val rotations: List<(Offset, Float, Offset) -> Offset> =
            listOf(
                { point, angle, origin -> point.rotateUpsideDownCake(angle, origin) },
                { point, angle, origin -> point.rotateVanillaIceCream(angle, origin) },
                { point, angle, origin -> point.rotateBaklava(angle, origin) },
                { point, angle, origin -> point.rotateCinnamonBun(angle, origin) },
            )
        val origin = Offset(20f, -10f)
        val point = Offset(23f, -6f)
        rotations.forEach { rotate ->
            val turned = rotate(point, (PI / 2).toFloat(), origin)
            assertEquals(16f, turned.x, 0.0001f)
            assertEquals(-7f, turned.y, 0.0001f)
            assertEquals(5f, (turned - origin).getDistance(), 0.0001f)
            val returned = rotate(turned, (-PI / 2).toFloat(), origin)
            assertEquals(point.x, returned.x, 0.0001f)
            assertEquals(point.y, returned.y, 0.0001f)
        }
    }
}
