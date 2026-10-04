package xyz.gaon.componentory.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.ComponentCategory
import xyz.gaon.componentory.lab.LabComponent

@Composable
fun ComponentListScreen(onOpenComponent: (LabComponent) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<ComponentCategory?>(null) }
    val focus = LocalFocusManager.current
    val components =
        LabComponent.entries.filter {
            it.matchesSearch(query) && (category == null || it.category == category)
        }
    Column(
        Modifier.widthIn(max = 900.dp)
            .fillMaxSize()
            .testTag("list_screen")
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("컴포넌트", style = MaterialTheme.typography.headlineMedium)
            Text("찾고, 선택하고, 직접 사용해 보세요.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().testTag("component_search"),
            placeholder = { Text("이름 또는 클래스 검색") },
            leadingIcon = {
                Icon(painterResource(R.drawable.ic_search), contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { query = "" },
                        modifier = Modifier.testTag("clear_search"),
                    ) {
                        Icon(painterResource(R.drawable.ic_close), contentDescription = "검색어 지우기")
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
        )
        CategoryFilter(category, { category = it }, "list", Modifier.fillMaxWidth())
        Text("${components.size}개 컴포넌트", style = MaterialTheme.typography.labelLarge)
        LazyColumn(
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().weight(1f).testTag("component_list"),
        ) {
            if (components.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "검색 결과가 없습니다",
                            modifier = Modifier.testTag("search_empty"),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        TextButton(
                            onClick = {
                                query = ""
                                category = null
                                focus.clearFocus()
                            },
                            modifier = Modifier.testTag("show_all_components"),
                        ) {
                            Text("전체 컴포넌트 보기")
                        }
                    }
                }
            }
            items(components, key = { it.name }) { component ->
                OutlinedCard(
                    onClick = {
                        focus.clearFocus()
                        onOpenComponent(component)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("list_${component.name}"),
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(component.label, style = MaterialTheme.typography.titleMedium)
                            Text(
                                component.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            painterResource(R.drawable.ic_forward),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
