package xyz.gaon.componentory.survivor

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

class GameLobbyUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun oneStartTapBeginsANormalRunWithTheDefaultWeapon() {
        val started = mutableListOf<Pair<WeaponId, Boolean>>()
        showLobby(onStart = { weapon, ranked -> started += weapon to ranked })
        compose.onNodeWithTag("game_start").assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(listOf(WeaponId.BUTTON to false), started) }
        compose.onNodeWithTag("game_panel").assertDoesNotExist()
    }

    @Test
    fun startingWeaponsCanBeSelectedWithoutAnAndroidVersionMenu() {
        showLobby()
        compose.onNodeWithTag("game_weapon_open").performScrollTo().performClick()
        compose
            .onNodeWithTag("game_weapon_SPINNER")
            .performScrollTo()
            .performClick()
            .assertIsSelected()
        compose.onNodeWithTag("game_selected", useUnmergedTree = true).assertTextEquals("Spinner")
        compose.onNodeWithTag("game_characters").assertDoesNotExist()
        compose.onNodeWithTag("game_panel_close").performClick()
        compose.onNodeWithTag("game_panel").assertDoesNotExist()
    }

    @Test
    fun aPortraitWindowBlocksBothStartActionsAndKeepsCloseReachable() {
        showLobby(portrait = true)
        compose.onNodeWithTag("game_rotate").assertIsDisplayed()
        compose.onNodeWithTag("game_start").assertIsNotEnabled()
        compose.onNodeWithTag("componentory_easter_egg_close").assertIsDisplayed()
        compose.onNodeWithTag("game_ranking").performScrollTo().performClick()
        compose.onNodeWithTag("game_ranked").performScrollTo().assertIsNotEnabled()
    }

    private fun showLobby(
        portrait: Boolean = false,
        onStart: (WeaponId, Boolean) -> Unit = { _, _ -> },
    ) {
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    Box(
                        Modifier.size(
                            if (portrait) 320.dp else 700.dp,
                            if (portrait) 500.dp else 350.dp,
                        )
                    ) {
                        GameLobby(
                            remember { GameAssets(compose.activity) },
                            onClose = {},
                            onStart = onStart,
                        )
                    }
                }
            }
        }
    }
}
