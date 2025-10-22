package com.tumme.scrudstudents.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

/**
 * TABLE HEADER - Reusable header component for table layouts
 *
 * This composable creates a styled header row with multiple columns
 * used for displaying column titles in list screens
 *
 *
 *
 * COMPOSE WEIGHT SYSTEM
 *
 * The weight modifier distributes available space proportionally
 *
 * How weights work:
 * - Each cell gets space proportional to its weight
 * - Weights are relative, not absolute percentages
 *
 *
 *
 * RESPONSIVENESS
 *
 * fillMaxWidth():
 * - Header stretches to full screen width
 * - Adapts to different screen sizes
 * - Columns scale proportionally using weights
 *
 * REUSABILITY
 *
 * This component is generic and reusable:
 * - Not tied to Student entity, Can be used for Course and Subscribe
 *
 * @param cells List of column titles to display
 * @param weights List of proportional widths for each column
 *
 */
@Composable
fun TableHeader(
    cells: List<String>,
    weights: List<Float>
) {
    /**
     * ROW LAYOUT - Horizontal arrangement of cells
     *
     * Modifier:
     * - fillMaxWidth(): Expand to full screen width
     * - background(): Apply light gray background color
     * - padding(8.dp): Add 8dp space around content
     *
     * verticalAlignment:
     * - Centers text vertically in the row
     */
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEFEF))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        /**
         * COLUMN ITERATION - Creates one Text cell per column
         *
         * for (i in cells.indices):
         * - Iterates through column titles by index
         * - Index used to access corresponding weight
         *
         */
        for (i in cells.indices) {
            /**
             * TEXT CELL - Individual column header
             *
             * Weight allocation:
             * - Modifier.weight(weights[i]): Assigns proportional width
             * - Available space is distributed based on weight ratios
             *
             * Horizontal padding:
             * - Creates visual separation between columns
             * - Prevents text from touching column boundaries
             */
            Text(
                text = cells[i],
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(weights[i])
                    .padding(horizontal = 4.dp)
            )
        }
    }
}