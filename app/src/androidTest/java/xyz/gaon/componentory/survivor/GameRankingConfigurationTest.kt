package xyz.gaon.componentory.survivor

import android.content.pm.PackageManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.*
import org.junit.Assume.assumeFalse
import org.junit.Rule
import org.junit.Test

class GameRankingConfigurationTest {
    @get:Rule val compose = createAndroidComposeRule<SurvivorActivity>()

    @Test
    fun anUnconfiguredBuildKeepsLocalPlayAndDoesNotRegisterTheSdkInitializer() {
        assumeFalse(GamePlayClient.configured(compose.activity))
        compose.onNodeWithTag("game_online_unavailable").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("game_start").performScrollTo().assertIsEnabled()
        val providers =
            compose.activity.packageManager
                .getPackageInfo(compose.activity.packageName, PackageManager.GET_PROVIDERS)
                .providers
                .orEmpty()
        assertFalse(
            providers.any {
                it.name == "com.google.android.gms.games.provider.PlayGamesInitProvider"
            }
        )
    }
}
