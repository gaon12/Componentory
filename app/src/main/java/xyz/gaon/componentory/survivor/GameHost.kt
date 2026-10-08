package xyz.gaon.componentory.survivor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

@Composable
internal fun GameHost(assets: GameAssets, onClose: () -> Unit) {
    var engine by remember { mutableStateOf<GameEngine?>(null) }
    var finished by remember { mutableStateOf<GameSession?>(null) }
    val screens = rememberSaveableStateHolder()
    val battle = engine
    val result = finished
    LaunchedEffect(assets) {
        (GameCatalog.releases.map { it.family.art } +
                listOf(
                    "button",
                    "slider",
                    "switch",
                    "spinner",
                    "progress",
                    "jellybean",
                    "neko",
                    "octopus",
                    "holo",
                ))
            .distinct()
            .forEach { assets.bitmap(it) }
    }
    when {
        result != null ->
            Surface(Modifier.fillMaxSize()) {
                Column(
                    Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        stringResource(
                            if (result.outcome == RunOutcome.WON) R.string.game_won
                            else if (result.outcome == RunOutcome.ABANDONED) R.string.game_abandoned
                            else R.string.game_defeated
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(weaponName(result.startingWeapon))
                    Text(
                        stringResource(
                            R.string.game_result_counts,
                            result.seconds,
                            result.regularKills + result.eliteKills + result.bossKills,
                        )
                    )
                    Button(
                        {
                            engine = null
                            finished = null
                        },
                        Modifier.testTag("game_return"),
                    ) {
                        Text(stringResource(R.string.game_return))
                    }
                }
            }
        battle != null -> GameBattle(battle, assets) { finished = it }
        else ->
            screens.SaveableStateProvider("lobby") {
                GameLobby(
                    assets,
                    onClose,
                    onStart = { weapon, ranked ->
                        engine =
                            GameEngine.create(
                                weapon,
                                if (ranked) RunMode.RANKED else RunMode.NORMAL,
                            )
                    },
                ) {
                    Text(
                        stringResource(R.string.game_controls),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
    }
}
