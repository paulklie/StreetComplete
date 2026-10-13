package de.westnordost.streetcomplete.quests.building_levels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.StepperButton
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import de.westnordost.streetcomplete.ui.theme.largeInput
import org.jetbrains.compose.resources.stringResource

import kotlin.math.max

/** Form to input building levels and roof levels, with quick-select buttons */
@Composable
fun BuildingLevelsForm(
    levels: Int?,
    onLevelsChange: (Int?) -> Unit,
    roofLevels: Int?,
    onRoofLevelsChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    previousBuildingLevels: List<BuildingLevels> = listOf(),
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.height(IntrinsicSize.Min)) {
            BuildingLevelsIllustration(Modifier.fillMaxSize())
            Row {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(128.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = stringResource(Res.string.quest_buildingLevels_levelsLabel2),
                        style = MaterialTheme.typography.caption.copy(hyphens = Hyphens.Auto),
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    )
                    var levelsInputHeightPx by remember { mutableIntStateOf(0) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val levelsString = levels?.toString().orEmpty()
                        TextField2(
                            value = levelsString,
                            onValueChange = { value ->
                                if (value.isEmpty() || value.isValidLevel()) {
                                    onLevelsChange(value.toIntOrNull())
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .onSizeChanged { levelsInputHeightPx = it.height },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.largeInput,
                        )
                        StepperButton(
                            onIncrease = { onLevelsChange((levels ?: 1) + 1) },
                            onDecrease = { onLevelsChange(if (levels == null) 1 else max(1, levels - 1)) },
                            increaseEnabled = true,
                            decreaseEnabled = levels == null || levels > 1,
                            modifier = Modifier
                                .width(48.dp)
                                .height(levelsInputHeightPx.pxToDp())
                        )
                    }
                }
                // same height as BuildingLevelsIllustration
                Spacer(Modifier
                    .height(144.dp)
                    .widthIn(max = 188.dp)
                    .weight(1f)
                )
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(128.dp)
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    var roofLevelsInputHeightPx by remember { mutableIntStateOf(0) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val roofLevelsString = roofLevels?.toString().orEmpty()
                        TextField2(
                            value = roofLevelsString,
                            onValueChange = { value ->
                                if (value.isEmpty() || value.isValidLevel()) {
                                    onRoofLevelsChange(value.toIntOrNull())
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .onSizeChanged { roofLevelsInputHeightPx = it.height },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.largeInput,
                        )
                        StepperButton(
                            onIncrease = { onRoofLevelsChange((roofLevels ?: 1) + 1) },
                            onDecrease = { onRoofLevelsChange(if (roofLevels == null) 1 else max(0, roofLevels - 1)) },
                            increaseEnabled = true,
                            decreaseEnabled = roofLevels == null || roofLevels > 0,
                            modifier = Modifier
                                .width(48.dp)
                                .height(roofLevelsInputHeightPx.pxToDp())
                        )
                    }
                    Text(
                        text = stringResource(Res.string.quest_buildingLevels_roofLevelsLabel2),
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.caption.copy(hyphens = Hyphens.Auto),
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    )
                }
            }
        }
        LastPickedChipsRow(
            items = previousBuildingLevels,
            onClick = {
                onLevelsChange(it.levels)
                onRoofLevelsChange(it.roofLevels)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            BuildingLevelsButtonContent(it.levels, it.roofLevels)
        }
    }
}

private fun String.isValidLevel(): Boolean =
    toIntOrNull()?.takeIf { it >= 0 } != null

@Composable
@Preview(widthDp = 288)
private fun BuildingLevelsFormPreview() {
    val levels = remember { mutableStateOf<Int?>(55) }
    val roofLevels = remember { mutableStateOf<Int?>(55) }
    BuildingLevelsForm(
        levels = levels.value,
        onLevelsChange = { levels.value = it },
        roofLevels = roofLevels.value,
        onRoofLevelsChange = { roofLevels.value = it },
        previousBuildingLevels = listOf(
            BuildingLevels(5, 2),
            BuildingLevels(4, 1),
            BuildingLevels(3, 0),
            BuildingLevels(13, 0),
        ),
    )
}
