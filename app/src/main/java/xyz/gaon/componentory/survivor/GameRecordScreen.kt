package xyz.gaon.componentory.survivor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

@Composable
internal fun GameRecordScreen(
    records: List<GameRunRecord>,
    assets: GameAssets,
    onClose: () -> Unit,
) {
    var mode by remember { mutableStateOf(RunMode.NORMAL) }
    var rule by remember { mutableStateOf(GameCatalog.RULESET) }
    var weapon by remember { mutableStateOf<WeaponId?>(null) }
    var selection by remember { mutableStateOf<List<String>>(emptyList()) }
    var comparison by remember { mutableStateOf(false) }
    val filtered = GameRecords.filter(records, mode, rule, weapon)
    val selected = records.filter { it.id in selection }
    BackHandler(onBack = onClose)
    Surface(Modifier.fillMaxSize().testTag("game_records_screen")) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row {
                TextButton(onClose, Modifier.testTag("game_records_close")) {
                    Text(stringResource(R.string.close))
                }
                Text(
                    stringResource(R.string.game_records),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RunMode.entries.forEach { item ->
                    FilterChip(
                        mode == item,
                        {
                            mode = item
                            selection = emptyList()
                        },
                        label = { Text(modeName(item)) },
                        modifier = Modifier.testTag("game_record_mode_" + item.name),
                    )
                }
                (records.map { it.ruleset } + GameCatalog.RULESET).distinct().sorted().forEach {
                    item ->
                    FilterChip(
                        rule == item,
                        {
                            rule = item
                            selection = emptyList()
                        },
                        label = { Text(item) },
                    )
                }
            }
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    weapon == null,
                    {
                        weapon = null
                        selection = emptyList()
                    },
                    label = { Text(stringResource(R.string.game_all_builds)) },
                )
                WeaponId.entries.forEach { item ->
                    FilterChip(
                        weapon == item,
                        {
                            weapon = item
                            selection = emptyList()
                        },
                        label = { Text(weaponName(item)) },
                    )
                }
            }
            GameRecords.best(filtered)?.let {
                Text(
                    stringResource(R.string.game_best_record, it.score, it.seconds),
                    Modifier.testTag("game_best"),
                )
            }
            Button(
                { comparison = true },
                Modifier.testTag("game_compare"),
                enabled = selected.size == 2 && GameRecords.comparable(selected[0], selected[1]),
            ) {
                Text(stringResource(R.string.game_compare))
            }
            Text(
                stringResource(R.string.game_compare_help),
                style = MaterialTheme.typography.bodySmall,
            )
            if (filtered.isEmpty()) Text(stringResource(R.string.game_no_records))
            LazyColumn(
                Modifier.weight(1f).testTag("game_records_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filtered, key = { it.id }) { record ->
                    OutlinedCard {
                        Row(
                            Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Checkbox(
                                record.id in selection,
                                {
                                    selection =
                                        if (record.id in selection) selection - record.id
                                        else (selection + record.id).takeLast(2)
                                },
                                Modifier.testTag("game_record_" + record.id),
                            )
                            Column(Modifier.weight(1f)) { GameRecordDetails(record, assets) }
                        }
                    }
                }
            }
        }
    }
    if (comparison && selected.size == 2)
        AlertDialog(
            onDismissRequest = { comparison = false },
            title = { Text(stringResource(R.string.game_compare)) },
            text = {
                Row(
                    Modifier.verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    selected.forEach { record ->
                        Column(Modifier.weight(1f)) { GameRecordDetails(record, assets) }
                    }
                }
            },
            confirmButton = {
                TextButton({ comparison = false }, Modifier.testTag("game_compare_close")) {
                    Text(stringResource(R.string.close))
                }
            },
        )
}

@Composable
internal fun modeName(mode: RunMode) =
    stringResource(if (mode == RunMode.NORMAL) R.string.game_normal else R.string.game_ranked)

@Composable
internal fun outcomeName(outcome: RunOutcome) =
    stringResource(
        when (outcome) {
            RunOutcome.WON -> R.string.game_won
            RunOutcome.DEFEATED -> R.string.game_defeated
            RunOutcome.ABANDONED -> R.string.game_abandoned
            RunOutcome.ACTIVE -> R.string.game_continue
        }
    )

@Composable
private fun GameRecordDetails(record: GameRunRecord, assets: GameAssets) {
    Text(weaponName(record.startingWeapon), style = MaterialTheme.typography.titleMedium)
    Text(modeName(record.mode) + " · " + record.ruleset)
    Text(outcomeName(record.outcome))
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val date =
        remember(record.completedAtEpochMillis, locale) {
            java.text.DateFormat.getDateTimeInstance(
                    java.text.DateFormat.SHORT,
                    java.text.DateFormat.SHORT,
                    locale,
                )
                .format(java.util.Date(record.completedAtEpochMillis))
        }
    Text(date, style = MaterialTheme.typography.bodySmall)
    Text(stringResource(R.string.game_result_reward, record.score, record.currency))
    Text(
        stringResource(
            R.string.game_record_counts,
            record.seconds,
            record.regularKills,
            record.eliteKills,
            record.bossKills,
        )
    )
    Text(
        stringResource(R.string.game_record_seed, record.seed),
        style = MaterialTheme.typography.bodySmall,
    )
    Text(
        stringResource(
            R.string.game_record_meta,
            record.permanent.health,
            record.permanent.damage,
            record.permanent.experience,
        ),
        style = MaterialTheme.typography.bodySmall,
    )
    GameEquipmentSummary(record, assets)
}
