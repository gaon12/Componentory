package xyz.gaon.componentory.survivor

import org.junit.Assert.*
import org.junit.Test

class GameCatalogTest {
    @Test
    fun everyApiHasAnArtworkSourceWithoutBeingAGameDifficulty() {
        assertEquals((1..37).toList(), GameCatalog.releases.map { it.api })
        assertEquals(37, GameCatalog.releases.map { it.id }.toSet().size)
        assertTrue(GameCatalog.releases.all { it.family.sourceRelease.isNotBlank() })
        assertEquals("17", GameCatalog.release(37).version)
    }

    @Test
    fun minorReleasesShareTheirActualFamily() {
        assertEquals(GameCatalog.release(11).family, GameCatalog.release(13).family)
        assertEquals(GameCatalog.release(31).family, GameCatalog.release(32).family)
        assertEquals(GameFamily.WATCH, GameCatalog.release(20).family)
    }

    @Test
    fun eachWeaponHasItsOwnEvolutionPartner() {
        assertEquals(
            SupportId.entries.toSet(),
            WeaponId.entries.map(GameCatalog::evolutionSupport).toSet(),
        )
    }
}
