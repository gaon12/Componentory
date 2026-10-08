package xyz.gaon.componentory.survivor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    onStart: ((WeaponId, Boolean) -> Unit)? = null,
    canStart: Boolean = true,
    extraContent: @Composable ColumnScope.() -> Unit = {},
) {
    var selectedName by rememberSaveable { mutableStateOf(WeaponId.BUTTON.name) }
    val weapon = WeaponId.valueOf(selectedName)
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
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Image(
                            assets.bitmap(GameCatalog.PLAYER_ART).asImageBitmap(),
                            null,
                            Modifier.size(96.dp),
                        )
                        Text(
                            stringResource(R.string.game_run_goal),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(stringResource(R.string.game_run_loop))
                        Text(
                            stringResource(R.string.game_mixed_resources),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        extraContent()
                    }
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            stringResource(R.string.game_starting_weapon),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(weaponName(weapon), Modifier.testTag("game_selected"))
                        for (item in WeaponId.entries) {
                            Card(
                                Modifier.fillMaxWidth()
                                    .testTag("game_weapon_" + item.name)
                                    .semantics { selected = weapon == item }
                                    .clickable { selectedName = item.name },
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            if (weapon == item)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceContainer
                                    ),
                            ) {
                                Row(
                                    Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Image(
                                        assets.bitmap(GameCatalog.weaponArt(item)).asImageBitmap(),
                                        null,
                                        Modifier.size(40.dp),
                                    )
                                    Column {
                                        Text(
                                            weaponName(item),
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                        Text(
                                            weaponHint(item),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                        Text(
                                            stringResource(
                                                R.string.game_art_source,
                                                GameCatalog.weaponSource(item),
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }
                            }
                        }
                        if (onStart != null) {
                            Button(
                                { onStart(weapon, false) },
                                Modifier.fillMaxWidth().testTag("game_start"),
                                enabled = landscape && canStart,
                            ) {
                                Text(stringResource(R.string.game_normal))
                            }
                            OutlinedButton(
                                { onStart(weapon, true) },
                                Modifier.fillMaxWidth().testTag("game_ranked"),
                                enabled = landscape && canStart,
                            ) {
                                Text(stringResource(R.string.game_ranked))
                            }
                            Text(
                                stringResource(R.string.game_mode_rules),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun weaponName(id: WeaponId) =
    stringResource(
        when (id) {
            WeaponId.BUTTON -> R.string.game_weapon_button
            WeaponId.SLIDER -> R.string.game_weapon_slider
            WeaponId.SWITCH -> R.string.game_weapon_switch
            WeaponId.SPINNER -> R.string.game_weapon_spinner
        }
    )

@Composable
private fun weaponHint(id: WeaponId) =
    stringResource(
        when (id) {
            WeaponId.BUTTON -> R.string.game_button_hint
            WeaponId.SLIDER -> R.string.game_slider_hint
            WeaponId.SWITCH -> R.string.game_switch_hint
            WeaponId.SPINNER -> R.string.game_spinner_hint
        }
    )
