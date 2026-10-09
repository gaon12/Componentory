package xyz.gaon.componentory.survivor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.gaon.componentory.R

/**
 * The end-of-run screen. It shows the outcome as a large banner, the run's numbers as tiles, and
 * the final build. "Play again" starts the same mode and weapon without the title screen.
 */
@Composable
internal fun GameResultScreen(
    result: GameSession,
    record: GameRunRecord?,
    assets: GameAssets,
    canLeave: Boolean,
    onRetry: (() -> Unit)?,
    onReturn: () -> Unit,
) {
    val (title, color) =
        when (result.outcome) {
            RunOutcome.WON -> R.string.game_won to GameColors.Gold
            RunOutcome.ABANDONED -> R.string.game_abandoned to GameColors.Muted
            else -> R.string.game_defeated to GameColors.Danger
        }
    Box(Modifier.fillMaxSize().background(GameColors.Night).testTag("game_result")) {
        GameBackdrop(assets)
        Box(Modifier.fillMaxSize().background(Color(0x9903060c)))
        Row(
            Modifier.fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(title).uppercase(),
                    style =
                        GameTitleStyle.copy(
                            color = color,
                            fontSize = 44.sp,
                            letterSpacing = 5.sp,
                            shadow = GameTitleStyle.shadow?.copy(color = color.copy(alpha = 0.6f)),
                        ),
                    textAlign = TextAlign.Center,
                )
                Text(weaponName(result.startingWeapon), style = GameBodyStyle)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        stringResource(R.string.game_stat_time),
                        "%02d:%02d".format(result.seconds / 60, result.seconds % 60),
                        Modifier.weight(1f),
                    )
                    StatTile(
                        stringResource(R.string.game_stat_kills),
                        (result.regularKills + result.eliteKills + result.bossKills).toString(),
                        Modifier.weight(1f),
                    )
                }
                if (record != null)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile(
                            stringResource(R.string.game_stat_score),
                            record.score.toString(),
                            Modifier.weight(1f).testTag("game_reward"),
                            GameColors.Android,
                        )
                        StatTile(
                            stringResource(R.string.game_stat_reward),
                            "+" + record.currency,
                            Modifier.weight(1f),
                            GameColors.Gold,
                        )
                    }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (onRetry != null)
                        GameButton(
                            stringResource(R.string.game_play_again),
                            onRetry,
                            Modifier.testTag("game_retry"),
                            enabled = canLeave,
                        )
                    GameButton(
                        stringResource(R.string.game_return),
                        onReturn,
                        Modifier.testTag("game_return"),
                        enabled = canLeave,
                        kind =
                            if (onRetry == null) GameButtonKind.PRIMARY
                            else GameButtonKind.SECONDARY,
                    )
                }
            }
            GamePanel(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                GameEquipmentSummary(result, assets)
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier,
    color: Color = GameColors.Text,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GameColors.Panel)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label, style = GameSmallStyle, maxLines = 1)
        Text(
            value,
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
        )
    }
}
