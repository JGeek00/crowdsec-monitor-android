package com.jgeek00.crowdsecmonitor.ui.screens.alerts.components.filters

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MultiSelectFilterContent(
    options: List<String>,
    selected: List<String>,
    onChange: (List<String>) -> Unit,
    customLabel: (@Composable (String) -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        items(options) { option ->
            val isChecked = option in selected
            val index = options.indexOf(option)
            SegmentedListItem(
                checked = isChecked,
                onCheckedChange = {
                    val newSelected = if (isChecked) selected - option else selected + option
                    onChange(newSelected)
                },
                shapes = ListItemDefaults.segmentedShapes(index = index, count = options.size),
                modifier = Modifier.padding(bottom = if (index < options.size - 1) 2.dp else 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (customLabel != null) {
                        customLabel(option)
                    } else {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}