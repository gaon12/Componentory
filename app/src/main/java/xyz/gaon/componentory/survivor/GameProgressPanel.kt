package xyz.gaon.componentory.survivor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

@Composable
internal fun GameProgressPanel(
    progress: GameProgress,
    onBuy: (PermanentUpgrade) -> Unit,
    onUnlock: (SupportId) -> Unit,
    assets: GameAssets? = null,
) {
    Text(stringResource(R.string.game_wallet, progress.currency), style = GameHeadingStyle)
    Text(stringResource(R.string.game_permanent_help), style = GameSmallStyle)
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
        ShopRow {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(label, color = GameColors.Text, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(5) { pip ->
                        Box(
                            Modifier.size(width = 22.dp, height = 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (pip < level) GameColors.Android else Color(0xff2a3550)
                                )
                        )
                    }
                }
            }
            GameButton(
                if (level == 5) "MAX" else progress.price(upgrade).toString(),
                { onBuy(upgrade) },
                Modifier.testTag("game_buy_" + upgrade.name),
                enabled = level < 5 && progress.currency >= progress.price(upgrade),
                leading = { Coin() },
            )
        }
    }
    for (support in SupportId.entries) {
        val unlocked = support in progress.unlocked
        ShopRow {
            if (assets != null)
                Image(
                    assets.bitmap(supportArt(support)).asImageBitmap(),
                    null,
                    Modifier.size(36.dp),
                )
            Text(
                if (unlocked) stringResource(R.string.game_unlocked, supportName(support))
                else supportName(support),
                Modifier.weight(1f),
                color = if (unlocked) GameColors.Android else GameColors.Text,
                fontWeight = FontWeight.Bold,
            )
            if (!unlocked)
                GameButton(
                    GameProgress.UNLOCK_PRICE.toString(),
                    { onUnlock(support) },
                    Modifier.testTag("game_unlock_" + support.name),
                    enabled = progress.currency >= GameProgress.UNLOCK_PRICE,
                    leading = { Coin() },
                )
        }
    }
}

@Composable
private fun ShopRow(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xff111a2c))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
private fun Coin() {
    Box(Modifier.size(12.dp).clip(RoundedCornerShape(50)).background(GameColors.Gold))
}

internal fun supportArt(id: SupportId) =
    when (id) {
        SupportId.PROGRESS -> "progress"
        SupportId.JELLY_BEAN -> "jellybean"
        SupportId.NEKO -> "neko"
        SupportId.OCTOPUS -> "octopus"
    }
