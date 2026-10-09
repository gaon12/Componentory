package xyz.gaon.componentory.survivor

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.gaon.componentory.R

private enum class LobbyPanel {
    WEAPON,
    SHOP,
    RANKING,
}

/**
 * The title screen. A first run needs only one tap on Start: the starting weapon has a default, and
 * the shop, records, and ranked challenge stay in their own panels until the player opens them.
 */
@Composable
internal fun GameLobby(
    assets: GameAssets,
    onClose: () -> Unit,
    onStart: ((WeaponId, Boolean) -> Unit)? = null,
    canStart: Boolean = true,
    currency: Long? = null,
    saved: GameSession? = null,
    canContinue: Boolean = false,
    onContinue: () -> Unit = {},
    onDiscard: () -> Unit = {},
    onRecords: (() -> Unit)? = null,
    loading: Boolean = false,
    shop: (@Composable ColumnScope.() -> Unit)? = null,
    ranking: (@Composable ColumnScope.() -> Unit)? = null,
) {
    var selectedName by rememberSaveable { mutableStateOf(WeaponId.BUTTON.name) }
    var panelName by rememberSaveable { mutableStateOf<String?>(null) }
    val weapon = WeaponId.valueOf(selectedName)
    val panel = panelName?.let(LobbyPanel::valueOf)
    BackHandler { if (panel != null) panelName = null else onClose() }
    Box(
        Modifier.fillMaxSize()
            .background(GameColors.Night)
            .testTag("componentory_easter_egg")
            .semantics { testTagsAsResourceId = true }
    ) {
        GameBackdrop(assets)
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val landscape = maxWidth > maxHeight
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClose,
                        Modifier.clip(CircleShape)
                            .background(Color(0x66000000))
                            .testTag("componentory_easter_egg_close"),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_back),
                            stringResource(R.string.close),
                            tint = GameColors.Text,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (currency != null) CurrencyChip(currency)
                }
                if (landscape)
                    Row(
                        Modifier.weight(1f).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TitleBlock(assets, Modifier.weight(1.1f))
                        Column(
                            Modifier.weight(1f).verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            PlayActions(
                                assets,
                                weapon,
                                onStart,
                                canStart,
                                saved,
                                canContinue,
                                onContinue,
                                onDiscard,
                                loading,
                                onWeapon = { panelName = LobbyPanel.WEAPON.name },
                            )
                            MenuRow(
                                onShop = shop?.let { { panelName = LobbyPanel.SHOP.name } },
                                onRecords = onRecords,
                                onRanking = onStart?.let { { panelName = LobbyPanel.RANKING.name } },
                            )
                        }
                    }
                else
                    Column(
                        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        TitleBlock(assets, Modifier.fillMaxWidth())
                        Text(
                            stringResource(R.string.game_rotate),
                            Modifier.clip(RoundedCornerShape(12.dp))
                                .background(Color(0x99000000))
                                .padding(12.dp)
                                .testTag("game_rotate"),
                            style = GameBodyStyle,
                            textAlign = TextAlign.Center,
                        )
                        PlayActions(
                            assets,
                            weapon,
                            onStart,
                            false,
                            saved,
                            false,
                            onContinue,
                            onDiscard,
                            loading,
                            onWeapon = { panelName = LobbyPanel.WEAPON.name },
                        )
                        MenuRow(
                            onShop = shop?.let { { panelName = LobbyPanel.SHOP.name } },
                            onRecords = onRecords,
                            onRanking = onStart?.let { { panelName = LobbyPanel.RANKING.name } },
                        )
                    }
            }
            when (panel) {
                LobbyPanel.WEAPON ->
                    LobbySheet(
                        stringResource(R.string.game_starting_weapon),
                        { panelName = null },
                    ) {
                        WeaponPicker(assets, weapon) { selectedName = it.name }
                    }
                LobbyPanel.SHOP ->
                    LobbySheet(stringResource(R.string.game_shop), { panelName = null }) {
                        shop?.invoke(this)
                    }
                LobbyPanel.RANKING ->
                    LobbySheet(stringResource(R.string.game_ranked), { panelName = null }) {
                        Text(stringResource(R.string.game_mode_rules), style = GameBodyStyle)
                        GameButton(
                            stringResource(R.string.game_ranked_start),
                            { onStart?.invoke(weapon, true) },
                            Modifier.fillMaxWidth().testTag("game_ranked"),
                            enabled = landscape && canStart && saved == null,
                        )
                        ranking?.invoke(this)
                    }
                null -> Unit
            }
        }
    }
}

@Composable
private fun TitleBlock(assets: GameAssets, modifier: Modifier) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Image(
            assets.bitmap(GameCatalog.PLAYER_ART).asImageBitmap(),
            null,
            Modifier.size(112.dp).gameBob(),
        )
        Text(
            stringResource(R.string.game_title).uppercase(),
            style = GameTitleStyle,
            textAlign = TextAlign.Center,
            lineHeight = 46.sp,
        )
        Text(
            stringResource(R.string.game_run_goal),
            color = GameColors.Gold,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun PlayActions(
    assets: GameAssets,
    weapon: WeaponId,
    onStart: ((WeaponId, Boolean) -> Unit)?,
    canStart: Boolean,
    saved: GameSession?,
    canContinue: Boolean,
    onContinue: () -> Unit,
    onDiscard: () -> Unit,
    loading: Boolean,
    onWeapon: () -> Unit,
) {
    if (saved != null) {
        Text(
            stringResource(
                R.string.game_saved_run,
                weaponName(saved.startingWeapon),
                saved.seconds,
            ),
            style = GameBodyStyle,
            textAlign = TextAlign.Center,
        )
        if (saved.ruleset != GameCatalog.RULESET)
            Text(
                stringResource(R.string.game_old_rules),
                style = GameSmallStyle,
                textAlign = TextAlign.Center,
            )
        GameButton(
            stringResource(R.string.game_continue_button),
            onContinue,
            Modifier.widthIn(min = 240.dp).gamePulse(canContinue).testTag("game_continue"),
            enabled = canContinue,
            large = true,
        )
        GameButton(
            stringResource(R.string.game_end_saved),
            onDiscard,
            Modifier.testTag("game_discard"),
            kind = GameButtonKind.SECONDARY,
        )
        return
    }
    if (onStart != null)
        GameButton(
            stringResource(R.string.game_start_button),
            { onStart(weapon, false) },
            Modifier.widthIn(min = 240.dp).gamePulse(canStart).testTag("game_start"),
            enabled = canStart,
            large = true,
        )
    if (loading) Text(stringResource(R.string.game_save_loading), style = GameSmallStyle)
    Row(
        Modifier.clip(RoundedCornerShape(50))
            .background(Color(0x99000000))
            .border(1.dp, GameColors.PanelEdge, RoundedCornerShape(50))
            .clickable(onClick = onWeapon)
            .testTag("game_weapon_open")
            .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            assets.bitmap(GameCatalog.weaponArt(weapon)).asImageBitmap(),
            null,
            Modifier.size(32.dp),
        )
        Text(stringResource(R.string.game_starting_weapon), style = GameSmallStyle)
        Text(
            weaponName(weapon),
            Modifier.testTag("game_selected"),
            color = GameColors.Text,
            fontWeight = FontWeight.Bold,
        )
        Text(stringResource(R.string.game_change), color = GameColors.Android, fontSize = 12.sp)
    }
}

@Composable
private fun MenuRow(onShop: (() -> Unit)?, onRecords: (() -> Unit)?, onRanking: (() -> Unit)?) {
    Row(
        Modifier.widthIn(max = 420.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (onShop != null)
            GameButton(
                stringResource(R.string.game_shop),
                onShop,
                Modifier.weight(1f).testTag("game_shop"),
                kind = GameButtonKind.SECONDARY,
            )
        if (onRecords != null)
            GameButton(
                stringResource(R.string.game_records_short),
                onRecords,
                Modifier.weight(1f).testTag("game_records"),
                kind = GameButtonKind.SECONDARY,
            )
        if (onRanking != null)
            GameButton(
                stringResource(R.string.game_ranked_short),
                onRanking,
                Modifier.weight(1f).testTag("game_ranking"),
                kind = GameButtonKind.SECONDARY,
            )
    }
}

@Composable
private fun CurrencyChip(currency: Long) {
    Row(
        Modifier.clip(RoundedCornerShape(50))
            .background(Color(0x99000000))
            .border(1.dp, GameColors.Gold.copy(alpha = 0.6f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(14.dp).clip(CircleShape).background(GameColors.Gold))
        Text(
            stringResource(R.string.game_wallet, currency),
            Modifier.testTag("game_wallet"),
            color = GameColors.Gold,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** A centered menu panel over the title screen. Back and the close button both dismiss it. */
@Composable
private fun LobbySheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    GameScrim {
        GamePanel(Modifier.widthIn(max = 640.dp).fillMaxWidth(0.9f).testTag("game_panel")) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, Modifier.weight(1f), style = GameHeadingStyle)
                GameButton(
                    stringResource(R.string.close),
                    onDismiss,
                    Modifier.testTag("game_panel_close"),
                    kind = GameButtonKind.SECONDARY,
                )
            }
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun WeaponPicker(assets: GameAssets, weapon: WeaponId, onSelect: (WeaponId) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        for (item in WeaponId.entries) {
            val chosen = weapon == item
            Column(
                Modifier.weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (chosen) Color(0xff183a2c) else Color(0xff111a2c))
                    .border(
                        BorderStroke(
                            2.dp,
                            if (chosen) GameColors.Android else GameColors.PanelEdge,
                        ),
                        RoundedCornerShape(16.dp),
                    )
                    .clickable { onSelect(item) }
                    .testTag("game_weapon_" + item.name)
                    .semantics { selected = chosen }
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Image(
                    assets.bitmap(GameCatalog.weaponArt(item)).asImageBitmap(),
                    null,
                    Modifier.size(56.dp),
                )
                Text(
                    weaponName(item),
                    color = if (chosen) GameColors.Android else GameColors.Text,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(weaponHint(item), style = GameSmallStyle, textAlign = TextAlign.Center)
                Text(
                    stringResource(R.string.game_art_source, GameCatalog.weaponSource(item)),
                    style = GameSmallStyle.copy(fontSize = 10.sp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
    Text(stringResource(R.string.game_mixed_resources), style = GameSmallStyle)
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
