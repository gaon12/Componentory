package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

private const val CAROUSEL_ITEM_COUNT = 10

// Each variant drives a real CarouselState and hosts the same card items.
// Tapping an item records the selected index, which is the copied input;
// the scroll offset itself is transient scroll state and is not copied.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Material3CarouselSample(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    val carouselState = rememberCarouselState { CAROUSEL_ITEM_COUNT }
    val item: @Composable (Int) -> Unit = { index ->
        val label = stringResource(R.string.list_item, index + 1)
        Card(
            onClick = { state.value = index + 1 },
            enabled = enabled,
            modifier = Modifier.fillMaxSize().testTag("library_${panel}_item_${index + 1}"),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(label, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
    when (component) {
        LabComponent.MULTI_BROWSE_CAROUSEL ->
            HorizontalMultiBrowseCarousel(
                state = carouselState,
                preferredItemWidth = 150.dp,
                modifier = modifier,
            ) { index ->
                item(index)
            }
        LabComponent.UNCONTAINED_CAROUSEL ->
            HorizontalUncontainedCarousel(
                state = carouselState,
                itemWidth = 150.dp,
                modifier = modifier,
            ) { index ->
                item(index)
            }
        LabComponent.CENTERED_HERO_CAROUSEL ->
            HorizontalCenteredHeroCarousel(state = carouselState, modifier = modifier) { index ->
                item(index)
            }
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
}
