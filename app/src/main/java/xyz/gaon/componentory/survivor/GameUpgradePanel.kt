package xyz.gaon.componentory.survivor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.gaon.componentory.R

/**
 * The level-up screen. Three large cards sit over the frozen arena; the battle clock stays stopped
 * until one is chosen, as before.
 */
@Composable
internal fun GameUpgradePanel(
    s: GameSession,
    assets: GameAssets,
    choices: List<UpgradeChoice>,
    onChoose: (UpgradeChoice) -> Unit,
) {
    GameScrim {
        Column(
            Modifier.fillMaxWidth(0.92f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.game_level_up),
                Modifier.testTag("game_growth"),
                style = GameTitleStyle.copy(color = GameColors.Gold, fontSize = 36.sp),
            )
            Text(
                stringResource(R.string.game_growth) +
                    " · " +
                    stringResource(R.string.game_slots, s.weapons.size, s.supports.size),
                style = GameBodyStyle,
            )
            Row(
                Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                for (choice in choices) UpgradeCard(s, assets, choice, Modifier.weight(1f)) {
                    onChoose(choice)
                }
            }
        }
    }
}

@Composable
private fun UpgradeCard(
    s: GameSession,
    assets: GameAssets,
    choice: UpgradeChoice,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val owned =
        when (choice.kind) {
            UpgradeKind.WEAPON -> s.weapons.any { it.id == choice.weapon }
            UpgradeKind.SUPPORT -> choice.support in s.supports
            else -> true
        }
    val evolution = choice.kind == UpgradeKind.EVOLUTION
    val accent =
        when {
            evolution -> GameColors.Gold
            !owned -> GameColors.Android
            else -> GameColors.PanelEdge
        }
    Column(
        modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(listOf(Color(0xff1a2844), Color(0xff0f1828))))
            .border(BorderStroke(3.dp, accent), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("game_upgrade_" + choice.key)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val badge =
            when {
                evolution -> stringResource(R.string.game_badge_evolve)
                !owned -> stringResource(R.string.game_badge_new)
                else -> null
            }
        Box(Modifier.height(22.dp), contentAlignment = Alignment.Center) {
            if (badge != null)
                Text(
                    badge,
                    Modifier.clip(RoundedCornerShape(50))
                        .background(accent)
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                    color = GameColors.Night,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                )
        }
        val art = choice.weapon?.let(GameCatalog::weaponArt) ?: choice.support?.let(::supportArt)
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(Color(0x33ffffff)),
            contentAlignment = Alignment.Center,
        ) {
            if (art != null) Image(assets.bitmap(art).asImageBitmap(), null, Modifier.size(56.dp))
            else
                Box(
                    Modifier.size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (choice.kind == UpgradeKind.HEAL) GameColors.Danger
                            else GameColors.Gold
                        )
                )
        }
        Text(
            choiceLabel(s, choice),
            color = GameColors.Text,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
        )
        val detail =
            when {
                choice.support != null -> supportEffect(choice.support)
                choice.weapon != null && !evolution -> weaponHint(choice.weapon)
                else -> null
            }
        if (detail != null) Text(detail, style = GameSmallStyle, textAlign = TextAlign.Center)
    }
}

@Composable
internal fun supportName(id: SupportId) =
    stringResource(
        when (id) {
            SupportId.PROGRESS -> R.string.game_progress
            SupportId.JELLY_BEAN -> R.string.game_jelly
            SupportId.NEKO -> R.string.game_neko
            SupportId.OCTOPUS -> R.string.game_octopus
        }
    )

@Composable
private fun supportEffect(id: SupportId) =
    stringResource(
        when (id) {
            SupportId.PROGRESS -> R.string.game_progress_effect
            SupportId.JELLY_BEAN -> R.string.game_jelly_effect
            SupportId.NEKO -> R.string.game_neko_effect
            SupportId.OCTOPUS -> R.string.game_octopus_effect
        }
    )

@Composable
internal fun evolutionName(id: WeaponId) =
    stringResource(
        when (id) {
            WeaponId.BUTTON -> R.string.game_evolution_button
            WeaponId.SLIDER -> R.string.game_evolution_slider
            WeaponId.SWITCH -> R.string.game_evolution_switch
            WeaponId.SPINNER -> R.string.game_evolution_spinner
        }
    )

@Composable
private fun choiceLabel(s: GameSession, choice: UpgradeChoice): String =
    when (choice.kind) {
        UpgradeKind.WEAPON ->
            stringResource(
                R.string.game_upgrade_level,
                weaponName(requireNotNull(choice.weapon)),
                (s.weapons.firstOrNull { it.id == choice.weapon }?.level ?: 0) + 1,
            )
        UpgradeKind.SUPPORT ->
            stringResource(
                R.string.game_upgrade_level,
                supportName(requireNotNull(choice.support)),
                (s.supports[choice.support] ?: 0) + 1,
            )
        UpgradeKind.EVOLUTION ->
            stringResource(R.string.game_evolve, evolutionName(requireNotNull(choice.weapon)))
        UpgradeKind.HEAL -> stringResource(R.string.game_heal_choice)
        UpgradeKind.CURRENCY -> stringResource(R.string.game_currency_choice)
        UpgradeKind.RECOVERY -> stringResource(R.string.game_recovery_choice)
    }
