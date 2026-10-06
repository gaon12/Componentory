package xyz.gaon.componentory.navigation

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import xyz.gaon.componentory.R
import xyz.gaon.componentory.catalog.ComponentDetailScreen
import xyz.gaon.componentory.catalog.ComponentListScreen
import xyz.gaon.componentory.compare.CompareScreen
import xyz.gaon.componentory.compare.ComparisonEntry
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
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
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComponentoryNavigation(
    appearance: AppAppearance,
    language: AppLanguage,
    onAppearanceChange: (AppAppearance) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(AppTab.LIST) }
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
    val runStore = remember { RunStore(context.filesDir) }
    var runRecords by remember { mutableStateOf(runStore.list()) }
    val savedScreens = rememberSaveableStateHolder()
    val inDetail = tab == AppTab.LIST && detail != null

    BackHandler(enabled = inDetail || tab != AppTab.LIST) {
        if (inDetail) detail = null else tab = AppTab.LIST
    }

    Scaffold(
        // Native automation can find live animated samples without waiting for an idle renderer.
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (inDetail) stringResource(requireNotNull(detail).labelRes)
                        else "Componentory",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                navigationIcon = {
                    if (inDetail) {
                        IconButton(
                            onClick = { detail = null },
                            modifier = Modifier.testTag("detail_back"),
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_back),
                                contentDescription = stringResource(R.string.back_to_list),
                            )
                        }
                    }
                },
                actions = {
                    if (inDetail) {
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
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                Spacer(Modifier.weight(1f))
                Row(Modifier.widthIn(max = 580.dp).fillMaxWidth()) {
                    AppTab.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = tab == destination,
                            onClick = {
                                if (tab == AppTab.LIST && destination == AppTab.LIST) detail = null
                                tab = destination
                            },
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
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = Color.Transparent,
                                    unselectedIconColor =
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            modifier = Modifier.testTag(destination.tag),
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
            }
        },
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.TopCenter) {
            // Keep each tab's search, scroll position, and live sample state when switching tabs.
            savedScreens.SaveableStateProvider(tab.name) {
                when (tab) {
                    AppTab.LIST -> {
                        val selected = detail
                        val catalogScreens = rememberSaveableStateHolder()
                        catalogScreens.SaveableStateProvider(selected?.name ?: "catalog") {
                            if (selected == null) {
                                ComponentListScreen(
                                    onOpenComponent = { component ->
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
                                        detail = component
                                    }
                                )
                            } else {
                                ComponentDetailScreen(
                                    selected,
                                    detailFamily,
                                    { family ->
                                        detailFamily = family
                                        detailProviders =
                                            detailProviders + (selected.name to family.name)
                                    },
                                    onCompareExporterChange = { detailExporter = it },
                                )
                            }
                        }
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
                            ) { record ->
                                runStore.append(record)
                                runRecords = runStore.list()
                            }
                        }
                    AppTab.RUNS ->
                        RunsScreen(
                            runRecords,
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
                                runStore.delete(record.id)
                                runRecords = runStore.list()
                            },
                            onExport = {
                                val send =
                                    Intent(Intent.ACTION_SEND)
                                        .setType("text/plain")
                                        .putExtra(Intent.EXTRA_TEXT, runStore.exportText())
                                context.startActivity(Intent.createChooser(send, null))
                            },
                        )
                    AppTab.SETTINGS ->
                        SettingsScreen(appearance, onAppearanceChange, language, onLanguageChange)
                }
            }
        }
    }
}
