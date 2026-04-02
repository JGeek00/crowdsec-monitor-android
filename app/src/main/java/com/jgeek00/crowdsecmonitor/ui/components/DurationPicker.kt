package com.jgeek00.crowdsecmonitor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jgeek00.crowdsecmonitor.R
import kotlinx.coroutines.flow.filter

@Composable
fun DurationPickerView(
    days: Int,
    hours: Int,
    minutes: Int,
    onDaysChanged: (Int) -> Unit,
    onHoursChanged: (Int) -> Unit,
    onMinutesChanged: (Int) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        NumberPickerColumn(
            range = 0..30,
            selectedValue = days,
            onValueChanged = { if (enabled) onDaysChanged(it) },
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.days),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )
        NumberPickerColumn(
            range = 0..23,
            selectedValue = hours,
            onValueChanged = { if (enabled) onHoursChanged(it) },
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.hours),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )
        NumberPickerColumn(
            range = 0..59,
            selectedValue = minutes,
            onValueChanged = { if (enabled) onMinutesChanged(it) },
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.minutes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NumberPickerColumn(
    range: IntRange,
    selectedValue: Int,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 44.dp
) {
    val values = remember(range) { range.toList() }
    val selectedIndex = values.indexOf(selectedValue).coerceAtLeast(0)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val flingBehavior = rememberSnapFlingBehavior(listState)

    val centeredItemIndex by remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf selectedIndex
            val viewportCenter =
                (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            visibleItems.minByOrNull { item ->
                kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
            }?.index ?: selectedIndex
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { scrolling -> !scrolling }
            .collect {
                val index = centeredItemIndex
                if (index >= 0 && index < values.size) {
                    onValueChanged(values[index])
                }
            }
    }

    Box(modifier = modifier) {
        // Selection highlight behind the center item
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                )
        )
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight),
            modifier = Modifier.height(itemHeight * 3),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(values) { index, value ->
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$value",
                        color = if (centeredItemIndex == index)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = if (centeredItemIndex == index)
                            FontWeight.SemiBold
                        else
                            FontWeight.Normal
                    )
                }
            }
        }
    }
}
