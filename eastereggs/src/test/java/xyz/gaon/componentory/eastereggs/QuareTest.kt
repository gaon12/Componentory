package xyz.gaon.componentory.eastereggs

import com.android_q.egg.quares.Quare
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuareTest {
    @Test
    fun markingOneCellChangesOnlyItsRowAndColumnAndResetClearsIt() {
        val puzzle = Quare(4, 3, 1)
        assertTrue(puzzle.isBlank())
        assertTrue(puzzle.check())
        puzzle.setUserMark(2, 1, 255)
        assertEquals(255, puzzle.getUserMark(2, 1))
        assertFalse(puzzle.check())
        assertFalse(puzzle.check(-1, 1))
        assertFalse(puzzle.check(2, -1))
        assertTrue(puzzle.check(-1, 0))
        assertTrue(puzzle.check(0, -1))
        assertArrayEquals(intArrayOf(0), puzzle.getRowClue(1))
        puzzle.resetUserMarks()
        assertEquals(0, puzzle.getUserMark(2, 1))
        assertTrue(puzzle.check())
    }
}
