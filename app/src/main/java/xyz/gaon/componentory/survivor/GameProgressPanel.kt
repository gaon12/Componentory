package xyz.gaon.componentory.survivor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import xyz.gaon.componentory.R

@Composable
internal fun GameProgressPanel(
    progress: GameProgress,
    onBuy: (PermanentUpgrade) -> Unit,
    onUnlock: (SupportId) -> Unit,
) {
    Text(stringResource(R.string.game_wallet, progress.currency), Modifier.testTag("game_wallet"))
    Text(stringResource(R.string.game_permanent_help), style = MaterialTheme.typography.bodySmall)
    for (upgrade in PermanentUpgrade.entries) {
        val label =
            stringResource(
                when (upgrade) {
                    PermanentUpgrade.HEALTH -> R.string.game_permanent_health
                    PermanentUpgrade.DAMAGE -> R.string.game_permanent_damage
                    PermanentUpgrade.EXPERIENCE -> R.string.game_permanent_experience
                }
            )
        val level = progress.level(upgrade)
        OutlinedButton(
            { onBuy(upgrade) },
            Modifier.fillMaxWidth().testTag("game_buy_" + upgrade.name),
            enabled = level < 5 && progress.currency >= progress.price(upgrade),
        ) {
            Text(
                stringResource(
                    R.string.game_permanent_price,
                    label,
                    level,
                    if (level == 5) 0 else progress.price(upgrade),
                )
            )
        }
    }
    for (support in SupportId.entries) {
        if (support in progress.unlocked)
            Text(stringResource(R.string.game_unlocked, supportName(support)))
        else
            OutlinedButton(
                { onUnlock(support) },
                Modifier.fillMaxWidth().testTag("game_unlock_" + support.name),
                enabled = progress.currency >= GameProgress.UNLOCK_PRICE,
            ) {
                Text(
                    stringResource(
                        R.string.game_unlock_price,
                        supportName(support),
                        GameProgress.UNLOCK_PRICE,
                    )
                )
            }
    }
}
