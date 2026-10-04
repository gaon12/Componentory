package xyz.gaon.componentory.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.ComponentCategory
import xyz.gaon.componentory.lab.LabComponent

@Composable
fun CategoryFilter(
    selected: ComponentCategory?,
    onSelect: (ComponentCategory?) -> Unit,
    tagPrefix: String,
    modifier: Modifier = Modifier,
) {
    val categories =
        ComponentCategory.entries.filter { category ->
            LabComponent.entries.any { it.category == category }
        }
    LazyRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.all_categories)) },
                modifier = Modifier.testTag("${tagPrefix}_category_ALL"),
            )
        }
        categories.forEach { category ->
            item(key = category.name) {
                FilterChip(
                    selected = selected == category,
                    onClick = { onSelect(category) },
                    label = { Text(stringResource(category.labelRes)) },
                    modifier = Modifier.testTag("${tagPrefix}_category_${category.name}"),
                )
            }
        }
    }
}
