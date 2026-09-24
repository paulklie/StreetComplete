package de.westnordost.streetcomplete.screens.user.links

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.user.achievements.Link
import de.westnordost.streetcomplete.data.user.achievements.LinkCategory
import de.westnordost.streetcomplete.data.user.achievements.links

@Composable
fun LazyGroupedLinksColumn(
    allLinks: List<Link>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero
) {
    val groupedLinks = remember(allLinks) {
        allLinks.groupBy { it.category }.map { (k, v) -> k to v }
    }
    var collapsedCategories by rememberSaveable { mutableStateOf(setOf<LinkCategory>()) }

    LazyLinksGrid(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        for ((category, links) in groupedLinks) {
            val isExpanded = category !in collapsedCategories
            item(
                key = category.name,
                span = { GridItemSpan(maxLineSpan) },
                contentType = category::class
            ) {
                LinkCategoryRow(
                    category = category,
                    expanded = isExpanded,
                    onClick = {
                        collapsedCategories = if (isExpanded) {
                            collapsedCategories + category
                        } else {
                            collapsedCategories - category
                        }
                    }
                )
            }
            if (isExpanded) {
                items(
                    links,
                    key = { it.id },
                    contentType = { it::class }
                ) { link ->
                    LinkRow(
                        link = link,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
fun LazyLinksColumn(
    links: List<Link>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero
) {
    LazyLinksGrid(modifier, contentPadding = contentPadding) {
        items(links, key = { it.id }) { link ->
            LinkRow(link)
        }
    }
}

@Composable
private fun LazyLinksGrid(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero,
    content: LazyGridScope.() -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 280.dp),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Preview
@Composable
private fun PreviewLazyGroupedLinksColumn() {
    LazyGroupedLinksColumn(
        allLinks = links,
        contentPadding = PaddingValues(16.dp)
    )
}
