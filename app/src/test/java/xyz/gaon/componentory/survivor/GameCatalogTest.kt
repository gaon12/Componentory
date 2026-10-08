package xyz.gaon.componentory.survivor

import org.junit.Assert.*
import org.junit.Test

class GameCatalogTest {
    @Test
    fun everyApiHasAPlayableIdentityAndSource() {
        assertEquals((1..37).toList(), GameCatalog.characters.map { it.api })
        assertEquals(37, GameCatalog.characters.map { it.id }.toSet().size)
        assertTrue(GameCatalog.characters.all { it.family.sourceRelease.isNotBlank() })
        assertEquals("17", GameCatalog.character(37).version)
    }

    @Test
    fun minorReleasesShareTheirActualFamily() {
        assertEquals(GameCatalog.character(11).family, GameCatalog.character(13).family)
        assertEquals(GameCatalog.character(31).family, GameCatalog.character(32).family)
        assertEquals(GameFamily.WATCH, GameCatalog.character(20).family)
    }

    @Test
    fun eachWeaponHasItsOwnEvolutionPartner() {
        assertEquals(
            SupportId.entries.toSet(),
            WeaponId.entries.map(GameCatalog::evolutionSupport).toSet(),
        )
    }
}
