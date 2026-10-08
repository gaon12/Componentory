package xyz.gaon.componentory.eastereggs

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EasterEggTapSequenceTest {
    @Test
    fun threeRapidTapsOpenTheScreen() {
        val sequence = EasterEggTapSequence()
        assertFalse(sequence.registerTap(0))
        assertFalse(sequence.registerTap(100))
        assertTrue(sequence.registerTap(200))
    }

    @Test
    fun theThirdTapMayArriveAtTheOneSecondBoundary() {
        val sequence = EasterEggTapSequence()
        assertFalse(sequence.registerTap(100))
        assertFalse(sequence.registerTap(600))
        assertTrue(sequence.registerTap(1_100))
    }

    @Test
    fun anExpiredTapStartsANewBurst() {
        val sequence = EasterEggTapSequence()
        assertFalse(sequence.registerTap(0))
        assertFalse(sequence.registerTap(500))
        assertFalse(sequence.registerTap(1_001))
        assertFalse(sequence.registerTap(1_100))
        assertTrue(sequence.registerTap(1_200))
    }

    @Test
    fun shortGapsDoNotAccumulateBeyondTheWholeBurstWindow() {
        val sequence = EasterEggTapSequence()
        assertFalse(sequence.registerTap(0))
        assertFalse(sequence.registerTap(600))
        assertFalse(sequence.registerTap(1_200))
    }

    @Test
    fun everyOpeningRequiresThreeNewTaps() {
        val sequence = EasterEggTapSequence()
        repeat(2) { burst ->
            val start = burst * 300L
            assertFalse(sequence.registerTap(start))
            assertFalse(sequence.registerTap(start + 100))
            assertTrue(sequence.registerTap(start + 200))
        }
    }

    @Test
    fun anEarlierClockReadingStartsANewBurst() {
        val sequence = EasterEggTapSequence()
        assertFalse(sequence.registerTap(500))
        assertFalse(sequence.registerTap(600))
        assertFalse(sequence.registerTap(100))
        assertFalse(sequence.registerTap(200))
        assertTrue(sequence.registerTap(300))
    }
}
