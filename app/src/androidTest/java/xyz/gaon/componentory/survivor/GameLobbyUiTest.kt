package xyz.gaon.componentory.survivor

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

class GameLobbyUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun startingWeaponsCanBeSelectedWithoutAnAndroidVersionMenu() {
        showLobby()
        compose
            .onNodeWithTag("game_weapon_SPINNER")
            .performScrollTo()
            .performClick()
            .assertIsSelected()
        compose.onNodeWithTag("game_selected").assertTextEquals("Spinner")
        compose.onNodeWithTag("game_characters").assertDoesNotExist()
    }

    @Test
    fun aPortraitWindowBlocksBothStartActionsAndKeepsCloseReachable() {
        showLobby(portrait = true)
        compose.onNodeWithTag("game_rotate").assertIsDisplayed()
        compose.onNodeWithTag("game_start").assertIsNotEnabled()
        compose.onNodeWithTag("game_ranked").assertIsNotEnabled()
        compose.onNodeWithTag("componentory_easter_egg_close").assertIsDisplayed()
    }

    private fun showLobby(portrait: Boolean = false) {
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
                            onStart = { _, _ -> },
                        )
                    }
                }
            }
        }
    }
}
