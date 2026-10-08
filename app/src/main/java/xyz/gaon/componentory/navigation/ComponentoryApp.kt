package xyz.gaon.componentory.navigation

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.launch
import xyz.gaon.componentory.R
import xyz.gaon.componentory.catalog.CatalogMode
import xyz.gaon.componentory.catalog.ComponentDetailScreen
import xyz.gaon.componentory.catalog.ComponentListScreen
import xyz.gaon.componentory.compare.CompareScreen
import xyz.gaon.componentory.compare.ComparisonEntry
import xyz.gaon.componentory.eastereggs.ComponentoryEasterEggScreen
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.onboarding.IntroductionDialog
import xyz.gaon.componentory.onboarding.OnboardingPreferences
import xyz.gaon.componentory.runs.RunHistory
import xyz.gaon.componentory.runs.RunOperation
import xyz.gaon.componentory.runs.RunStore
import xyz.gaon.componentory.runs.RunsScreen
import xyz.gaon.componentory.runs.toComparisonEntry
import xyz.gaon.componentory.settings.AppAppearance
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.AppearancePreferences
import xyz.gaon.componentory.settings.LanguagePreferences
import xyz.gaon.componentory.settings.SettingsScreen
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

private enum class AppTab(val labelRes: Int, val icon: Int, val tag: String) {
    LIST(R.string.nav_list, R.drawable.ic_list, "nav_list"),
    COMPARE(R.string.nav_compare, R.drawable.ic_compare, "nav_compare"),
    RUNS(R.string.nav_runs, R.drawable.ic_history, "nav_runs"),
    SETTINGS(R.string.nav_settings, R.drawable.ic_settings, "nav_settings"),
}

@Composable
fun ComponentoryApp() {
    val context = LocalContext.current
    val preferences = remember(context) { AppearancePreferences(context) }
    val introductionPreferences = remember(context) { OnboardingPreferences(context) }
    var introductionOpen by rememberSaveable {
        mutableStateOf(!introductionPreferences.completed())
    }
    var appearance by remember { mutableStateOf(preferences.read()) }
    val dark =
        when (appearance) {
            AppAppearance.SYSTEM -> isSystemInDarkTheme()
            AppAppearance.LIGHT -> false
            AppAppearance.DARK -> true
        }
    val view = LocalView.current
    SideEffect {
        (context as? Activity)?.window?.let { window ->
            val bars = WindowCompat.getInsetsController(window, view)
            bars.isAppearanceLightStatusBars = !dark
            bars.isAppearanceLightNavigationBars = !dark
        }
    }
    ComponentoryTheme(darkTheme = dark, dynamicColor = false) {
        ComponentoryNavigation(
            appearance,
            LanguagePreferences.read(context),
            {
                preferences.save(it)
                appearance = it
            },
            { LanguagePreferences.apply(context as Activity, it) },
            { introductionOpen = true },
        )
        if (introductionOpen)
            IntroductionDialog {
                introductionPreferences.saveCompleted(true)
                introductionOpen = false
            }
    }
}

@Composable
private fun ComponentoryNavigation(
    appearance: AppAppearance,
    language: AppLanguage,
    onAppearanceChange: (AppAppearance) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onShowIntroduction: () -> Unit,
) {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(AppTab.LIST) }
    var easterEggOpen by rememberSaveable { mutableStateOf(false) }
    var catalogMode by rememberSaveable { mutableStateOf(CatalogMode.SAMPLES) }
    var detailOriginMode by rememberSaveable { mutableStateOf(CatalogMode.SAMPLES) }
    LaunchedEffect(catalogMode, detailOriginMode) {
        catalogMode = catalogMode.current()
        detailOriginMode = detailOriginMode.current()
    }
    var detail by rememberSaveable { mutableStateOf<LabComponent?>(null) }
    var detailFamily by rememberSaveable { mutableStateOf(DesignFamily.CLASSIC) }
    var detailProviders by rememberSaveable { mutableStateOf<Map<String, String>>(emptyMap()) }
    var comparison by rememberSaveable { mutableStateOf(LabComponent.BUTTON) }
    var left by rememberSaveable { mutableStateOf(DesignFamily.CLASSIC) }
    var right by rememberSaveable { mutableStateOf(DesignFamily.HOLO) }
    var comparisonEntry by
        rememberSaveable(stateSaver = ComparisonEntry.Saver) {
            mutableStateOf<ComparisonEntry?>(null)
        }
    var comparisonGeneration by rememberSaveable { mutableIntStateOf(0) }
    var detailExporter by remember { mutableStateOf<(() -> ComparisonEntry)?>(null) }
    val history = remember { RunHistory(RunStore(context.filesDir)) }
    // The root owns storage work so switching tabs does not cancel a pending save.
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(history) { history.load() }
    val failureMessage =
        history.failure?.let {
            stringResource(
                when (it) {
                    RunOperation.LOAD -> R.string.run_load_failed
                    RunOperation.SAVE -> R.string.run_save_failed
                    RunOperation.DELETE -> R.string.run_delete_failed
                    RunOperation.EXPORT -> R.string.run_export_failed
                }
            )
        }
    LaunchedEffect(failureMessage) { failureMessage?.let { snackbar.showSnackbar(it) } }
    val savedScreens = rememberSaveableStateHolder()
    if (easterEggOpen) {
        ComponentoryEasterEggScreen { easterEggOpen = false }
        return
    }
    val inDetail = tab == AppTab.LIST && detail != null && catalogMode == CatalogMode.SAMPLES
    val closeDetail = {
        detail = null
        catalogMode = detailOriginMode
    }
    val selectTab: (AppTab) -> Unit = { destination ->
        if (tab == AppTab.LIST && destination == AppTab.LIST) closeDetail()
        tab = destination
    }

    BackHandler(enabled = inDetail || tab != AppTab.LIST) {
        if (inDetail) closeDetail() else tab = AppTab.LIST
    }

    BoxWithConstraints(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
        val expanded = maxWidth >= 800.dp
        val catalogWidth = if (maxWidth >= 1000.dp) 380.dp else 320.dp
        Row(Modifier.fillMaxSize()) {
            if (expanded) {
                NavigationRail(
                    Modifier.width(96.dp).testTag("app_navigation_rail"),
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    Spacer(Modifier.weight(1f))
                    AppTab.entries.forEach { destination ->
                        NavigationRailItem(
                            selected = tab == destination,
                            onClick = { selectTab(destination) },
                            icon = {
                                Icon(painterResource(destination.icon), null, Modifier.size(24.dp))
                            },
                            label = {
                                Text(
                                    stringResource(destination.labelRes),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            colors =
                                NavigationRailItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                            modifier = Modifier.testTag(destination.tag),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
            Scaffold(
                // Native automation can find live animated samples without waiting for an idle
                // renderer.
                modifier = Modifier.weight(1f).semantics { testTagsAsResourceId = true },
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbar) },
                topBar = {
                    if (inDetail)
                        Row(
                            Modifier.fillMaxWidth()
                                .windowInsetsPadding(
                                    WindowInsets.safeDrawing.only(
                                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                                    )
                                )
                                .padding(horizontal = 12.dp)
                                .testTag("detail_actions"),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = closeDetail,
                                modifier = Modifier.testTag("detail_back"),
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_back),
                                    contentDescription = stringResource(R.string.back_to_list),
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            TextButton(
                                onClick = {
                                    val entry = detailExporter?.invoke()
                                    if (
                                        entry == null ||
                                            entry.left.component != detail ||
                                            entry.left.sourceFamily != detailFamily
                                    ) {
                                        return@TextButton
                                    }
                                    comparisonEntry = entry
                                    comparisonGeneration++
                                    savedScreens.removeState(AppTab.COMPARE.name)
                                    comparison = requireNotNull(detail)
                                    left = detailFamily
                                    if (right == left) {
                                        right =
                                            if (left == DesignFamily.MATERIAL3) DesignFamily.CLASSIC
                                            else DesignFamily.MATERIAL3
                                    }
                                    tab = AppTab.COMPARE
                                },
                                modifier = Modifier.testTag("detail_compare"),
                                enabled = detailExporter != null,
                            ) {
                                Text(stringResource(R.string.compare_action))
                            }
                        }
                },
                bottomBar = {
                    if (!expanded)
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                        ) {
                            Spacer(Modifier.weight(1f))
                            Row(Modifier.widthIn(max = 580.dp).fillMaxWidth()) {
                                AppTab.entries.forEach { destination ->
                                    NavigationBarItem(
                                        selected = tab == destination,
                                        onClick = { selectTab(destination) },
                                        icon = {
                                            Icon(
                                                painterResource(destination.icon),
                                                contentDescription = null,
                                                modifier = Modifier.size(22.dp),
                                            )
                                        },
                                        label = {
                                            Text(
                                                stringResource(destination.labelRes),
                                                style = MaterialTheme.typography.labelMedium,
                                            )
                                        },
                                        colors =
                                            NavigationBarItemDefaults.colors(
                                                selectedIconColor =
                                                    MaterialTheme.colorScheme.primary,
                                                selectedTextColor =
                                                    MaterialTheme.colorScheme.primary,
                                                indicatorColor = Color.Transparent,
                                                unselectedIconColor =
                                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                                unselectedTextColor =
                                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                            ),
                                        modifier = Modifier.testTag(destination.tag),
                                    )
                                }
                            }
                            Spacer(Modifier.weight(1f))
                        }
                },
            ) { insets ->
                Box(
                    Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets).imePadding(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    // Keep each tab's search, scroll position, and live sample state when switching
                    // tabs.
                    savedScreens.SaveableStateProvider(tab.name) {
                        when (tab) {
                            AppTab.LIST -> {
                                val catalogScreens = rememberSaveableStateHolder()
                                val openComponent: (LabComponent) -> Unit = { component ->
                                    val remembered =
                                        DesignFamily.entries.firstOrNull {
                                            it.name == detailProviders[component.name]
                                        }
                                    val family =
                                        selectDetailProvider(
                                            component,
                                            detailFamily,
                                            remembered,
                                            Build.VERSION.SDK_INT,
                                        )
                                    detailFamily = family
                                    detailProviders =
                                        detailProviders + (component.name to family.name)
                                    detailOriginMode = catalogMode
                                    catalogMode = CatalogMode.SAMPLES
                                    detail = component
                                }
                                // Move the same composition so saveable keys and native view state
                                // survive reflow.
                                val catalog =
                                    remember(catalogScreens) {
                                        movableContentOf {
                                            catalogScreens.SaveableStateProvider("catalog") {
                                                ComponentListScreen(
                                                    catalogMode,
                                                    { catalogMode = it },
                                                    detail,
                                                    openComponent,
                                                )
                                            }
                                        }
                                    }
                                val sample =
                                    remember(catalogScreens) {
                                        movableContentOf {
                                            val selected = detail
                                            if (selected != null) {
                                                catalogScreens.SaveableStateProvider(
                                                    selected.name
                                                ) {
                                                    ComponentDetailScreen(
                                                        selected,
                                                        detailFamily,
                                                        { family ->
                                                            detailFamily = family
                                                            detailProviders =
                                                                detailProviders +
                                                                    (selected.name to family.name)
                                                        },
                                                        onCompareExporterChange = {
                                                            detailExporter = it
                                                        },
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    Modifier.fillMaxSize().padding(24.dp),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Card(
                                                        Modifier.widthIn(max = 520.dp)
                                                            .testTag("catalog_detail_empty")
                                                    ) {
                                                        Column(
                                                            Modifier.padding(24.dp),
                                                            verticalArrangement =
                                                                Arrangement.spacedBy(12.dp),
                                                        ) {
                                                            Text(
                                                                stringResource(
                                                                    R.string.catalog_select_title
                                                                ),
                                                                style =
                                                                    MaterialTheme.typography
                                                                        .headlineSmall,
                                                            )
                                                            Text(
                                                                stringResource(
                                                                    R.string.catalog_select_note
                                                                ),
                                                                style =
                                                                    MaterialTheme.typography
                                                                        .bodyLarge,
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                if (expanded && catalogMode == CatalogMode.SAMPLES) {
                                    Row(Modifier.fillMaxSize().testTag("catalog_split")) {
                                        Box(Modifier.width(catalogWidth).fillMaxSize()) {
                                            catalog()
                                        }
                                        Box(Modifier.weight(1f).fillMaxSize()) { sample() }
                                    }
                                } else if (inDetail) sample() else catalog()
                            }
                            AppTab.COMPARE ->
                                key(comparisonGeneration) {
                                    CompareScreen(
                                        comparison,
                                        { comparison = it },
                                        left,
                                        { left = it },
                                        right,
                                        { right = it },
                                        initialEntry = comparisonEntry,
                                    ) { record, completed ->
                                        scope.launch { completed(history.save(record)) }
                                    }
                                }
                            AppTab.RUNS ->
                                RunsScreen(
                                    history.records,
                                    loading = history.loading,
                                    busy = history.busy,
                                    loadFailed = history.failure == RunOperation.LOAD,
                                    onRetry = { scope.launch { history.load() } },
                                    onCreate = { tab = AppTab.COMPARE },
                                    onOpen = { record ->
                                        record.toComparisonEntry()?.let { entry ->
                                            comparisonEntry = entry
                                            comparisonGeneration++
                                            savedScreens.removeState(AppTab.COMPARE.name)
                                            comparison = entry.left.component
                                            left = entry.left.sourceFamily
                                            right = requireNotNull(entry.right).sourceFamily
                                            tab = AppTab.COMPARE
                                        }
                                    },
                                    onDelete = { record ->
                                        scope.launch { history.delete(record.id) }
                                    },
                                    onExport = {
                                        scope.launch {
                                            history.export()?.let { text ->
                                                val send =
                                                    Intent(Intent.ACTION_SEND)
                                                        .setType("text/plain")
                                                        .putExtra(Intent.EXTRA_TEXT, text)
                                                context.startActivity(
                                                    Intent.createChooser(send, null)
                                                )
                                            }
                                        }
                                    },
                                )
                            AppTab.SETTINGS ->
                                SettingsScreen(
                                    appearance,
                                    onAppearanceChange,
                                    language,
                                    onLanguageChange,
                                    onShowIntroduction,
                                    onOpenEasterEgg = { easterEggOpen = true },
                                )
                        }
                    }
                }
            }
        }
    }
}
