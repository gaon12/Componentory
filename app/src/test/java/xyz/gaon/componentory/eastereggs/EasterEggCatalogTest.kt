package xyz.gaon.componentory.eastereggs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EasterEggCatalogTest {
    @Test
    fun releaseIdsAreUniqueAndCoverEveryImportedFamilyWithoutInventedPreGingerbreadEggs() {
        assertEquals(26, easterEggReleases.size)
        assertEquals(26, easterEggReleases.map { it.id }.toSet().size)
        assertEquals(18, easterEggReleases.map { it.family.module }.toSet().size)
        assertEquals("2.3", easterEggReleases.first().version)
        assertEquals("17", easterEggReleases.last().version)
        assertFalse(easterEggReleases.any { it.version == "17.1" })
    }

    @Test
    fun sharedArtworkIsRecordedButOreoPointOneKeepsItsDifferentEntry() {
        val eight = easterEggReleases.single { it.version == "8.0" }
        val eightOne = easterEggReleases.single { it.version == "8.1" }
        assertEquals(eight.family, eightOne.family)
        assertNotEquals(eight.logo.className, eightOne.logo.className)
        assertTrue(eightOne.logo.className.endsWith("\$Point1"))
        assertEquals(
            easterEggReleases.single { it.version == "12" }.family,
            easterEggReleases.single { it.version == "12L" }.family,
        )
    }

    @Test
    fun searchFindsVersionsNicknamesAndTranslatedEggNames() {
        assertEquals(
            listOf("8.1"),
            easterEggReleases.filter { it.matches("Android 8.1", "이스터에그") }.map { it.version },
        )
        assertEquals(
            listOf("16"),
            easterEggReleases.filter { it.matches("Baklava", "이스터에그") }.map { it.version },
        )
        assertTrue(easterEggReleases.all { it.matches("이스터에그", "이스터에그") })
        assertTrue(easterEggReleases.all { it.matches("easter egg", "彩蛋") })
        assertFalse(easterEggReleases.any { it.matches("no such component", "이스터에그") })
    }

    @Test
    fun mainGamesContinueFromTheLogoWhileOnlyToolsAndPreviewsHaveExtraButtons() {
        val families = easterEggReleases.map { it.family }.distinct()
        val space = families.filter { it.logo.finishOnNextStage }
        assertEquals(
            setOf("UpsideDownCake", "VanillaIceCream", "Baklava", "CinnamonBun"),
            space.map { it.module }.toSet(),
        )
        assertTrue(
            space.all {
                it.additionalScreens.none { stage -> stage.title == "landroid.MainActivity" }
            }
        )
        assertTrue(families.single { it.module == "Oreo" }.additionalScreens.isEmpty())
        assertTrue(families.single { it.module == "Q" }.additionalScreens.isEmpty())
        assertTrue(
            families
                .single { it.module == "Marshmallow" }
                .additionalScreens
                .any { it.title == "preview.PlatLogoActivity" }
        )
        assertTrue(
            families
                .single { it.module == "S" }
                .additionalScreens
                .any { it.title == "widget.PaintChipsActivity" }
        )
    }

    @Test
    fun fullGamesAndPublicIntegrationsKeepDistinctEntryPointsAndApiRequirements() {
        val families = easterEggReleases.map { it.family }.distinct()
        assertTrue(
            families
                .single { it.module == "Marshmallow" }
                .stages
                .any { it.title == "MLandActivity" }
        )
        assertTrue(
            families.single { it.module == "Q" }.stages.any { it.title == "quares.QuaresActivity" }
        )
        assertTrue(
            families
                .filter {
                    it.module in
                        listOf("UpsideDownCake", "VanillaIceCream", "Baklava", "CinnamonBun")
                }
                .all { family -> family.stages.any { it.title == "landroid.MainActivity" } }
        )
        families
            .filter { it.module in listOf("S", "Tiramisu") }
            .forEach { family ->
                assertEquals(30, family.stages.single { it.title == "neko.NekoLand" }.minimumApi)
                assertEquals(
                    31,
                    family.stages.single { it.title == "widget.PaintChipsActivity" }.minimumApi,
                )
            }
        val integrations = families.flatMap { it.integrations }
        assertEquals(EggIntegrationKind.entries.toSet(), integrations.map { it.kind }.toSet())
        assertTrue(
            integrations
                .filter { it.kind == EggIntegrationKind.CONTROLS }
                .all { it.minimumApi >= 30 }
        )
        assertTrue(
            integrations.filter { it.kind == EggIntegrationKind.WIDGET }.all { it.minimumApi >= 31 }
        )
        assertTrue(
            families.all {
                it.stages.all { stage -> stage.className.startsWith(it.packageName + ".") }
            }
        )
    }
}
