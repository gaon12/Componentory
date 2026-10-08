package xyz.gaon.componentory.survivor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

@Composable
internal fun GameEquipmentSummary(s: GameSession, assets: GameAssets) {
    Column(Modifier.testTag("game_equipment"), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(R.string.game_slots, s.weapons.size, s.supports.size))
        for (weapon in s.weapons) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    assets.bitmap(GameCatalog.weaponArt(weapon.id)).asImageBitmap(),
                    null,
                    Modifier.size(32.dp),
                )
                Text(
                    if (weapon.evolved) evolutionName(weapon.id)
                    else
                        stringResource(
                            R.string.game_upgrade_level,
                            weaponName(weapon.id),
                            weapon.level,
                        ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        for ((id, level) in s.supports) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val art =
                    when (id) {
                        SupportId.PROGRESS -> "progress"
                        SupportId.JELLY_BEAN -> "jellybean"
                        SupportId.NEKO -> "neko"
                        SupportId.OCTOPUS -> "octopus"
                    }
                Image(assets.bitmap(art).asImageBitmap(), null, Modifier.size(32.dp))
                Text(
                    stringResource(R.string.game_upgrade_level, supportName(id), level),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
