package xyz.gaon.componentory.survivor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

@Composable
internal fun GameLobby(
    assets: GameAssets,
    onClose: () -> Unit,
    onStart: ((GameCharacter, Boolean) -> Unit)? = null,
    extraContent: @Composable ColumnScope.() -> Unit = {},
) {
    var api by rememberSaveable { mutableIntStateOf(1) }
    val character = GameCatalog.character(api)
    BackHandler(onBack = onClose)
    Surface(
        Modifier.fillMaxSize().testTag("componentory_easter_egg").semantics {
            testTagsAsResourceId = true
        }
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val landscape = maxWidth > maxHeight
            Column(
                Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClose, Modifier.testTag("componentory_easter_egg_close")) {
                        Icon(painterResource(R.drawable.ic_back), stringResource(R.string.close))
                    }
                    Text(
                        stringResource(R.string.game_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                if (!landscape)
                    Text(stringResource(R.string.game_rotate), Modifier.testTag("game_rotate"))
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LazyVerticalGrid(
                        GridCells.Adaptive(112.dp),
                        Modifier.weight(1f).testTag("game_characters"),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(GameCatalog.characters, key = { it.id }) { item ->
                            Card(
                                Modifier.testTag("game_character_" + item.api)
                                    .semantics { selected = api == item.api }
                                    .clickable { api = item.api },
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            if (api == item.api)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceContainer
                                    ),
                            ) {
                                Column(
                                    Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Image(
                                        assets.bitmap(item.family.art).asImageBitmap(),
                                        null,
                                        Modifier.size(40.dp),
                                    )
                                    Text(item.title, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Image(
                            assets.bitmap(character.family.art).asImageBitmap(),
                            null,
                            Modifier.size(80.dp),
                        )
                        Text(
                            character.title,
                            Modifier.testTag("game_selected"),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            stringResource(
                                R.string.game_art_source,
                                character.family.sourceRelease,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            stringResource(R.string.game_shared_art),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (onStart != null) {
                            Button(
                                { onStart(character, false) },
                                Modifier.testTag("game_start"),
                                enabled = landscape,
                            ) {
                                Text(stringResource(R.string.game_normal))
                            }
                            OutlinedButton(
                                { onStart(character, true) },
                                Modifier.testTag("game_ranked"),
                                enabled = landscape,
                            ) {
                                Text(stringResource(R.string.game_ranked))
                            }
                        }
                        extraContent()
                    }
                }
            }
        }
    }
}
