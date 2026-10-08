package xyz.gaon.componentory.eastereggs

import org.junit.Assert.assertEquals
import org.junit.Test

class EasterEggTapSequenceTest {
    @Test
    fun sevenTapsCountDownToTheOpening() {
        val sequence = EasterEggTapSequence()
        assertEquals(listOf(6, 5, 4, 3, 2, 1, 0), List(7) { sequence.registerTap() })
    }

    @Test
    fun everyOpeningRequiresSevenNewTaps() {
        val sequence = EasterEggTapSequence()
        repeat(3) { assertEquals(listOf(6, 5, 4, 3, 2, 1, 0), List(7) { sequence.registerTap() }) }
    }

    @Test
    fun leavingTheVisitDiscardsPartialTaps() {
        val sequence = EasterEggTapSequence()
        repeat(6) { sequence.registerTap() }
        sequence.reset()
        assertEquals(listOf(6, 5, 4, 3, 2, 1, 0), List(7) { sequence.registerTap() })
    }
}
