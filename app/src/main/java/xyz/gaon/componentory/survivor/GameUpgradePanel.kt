package xyz.gaon.componentory.survivor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import xyz.gaon.componentory.R

@Composable
internal fun GameUpgradePanel(
    s: GameSession,
    assets: GameAssets,
    choices: List<UpgradeChoice>,
    onPause: () -> Unit,
    onChoose: (UpgradeChoice) -> Unit,
) {
    Dialog(onDismissRequest = onPause) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(
                Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(R.string.game_growth),
                    Modifier.testTag("game_growth"),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(stringResource(R.string.game_slots, s.weapons.size, s.supports.size))
                for (choice in choices) {
                    OutlinedButton(
                        { onChoose(choice) },
                        Modifier.fillMaxWidth().testTag("game_upgrade_" + choice.key),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val art =
                                choice.weapon?.let(GameCatalog::weaponArt)
                                    ?: choice.support?.let {
                                        when (it) {
                                            SupportId.PROGRESS -> "progress"
                                            SupportId.JELLY_BEAN -> "jellybean"
                                            SupportId.NEKO -> "neko"
                                            SupportId.OCTOPUS -> "octopus"
                                        }
                                    }
                            if (art != null)
                                Image(
                                    assets.bitmap(art).asImageBitmap(),
                                    null,
                                    Modifier.size(40.dp),
                                )
                            Column {
                                Text(choiceLabel(s, choice))
                                if (choice.support != null)
                                    Text(
                                        supportEffect(choice.support),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                            }
                        }
                    }
                }
            }
        }
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
