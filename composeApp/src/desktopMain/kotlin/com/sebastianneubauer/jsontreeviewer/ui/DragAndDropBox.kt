package com.sebastianneubauer.jsontreeviewer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DragAndDropBox(
    isHovering: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .background(
                color = if(isHovering) Color.LightGray else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                border = BorderStroke(
                    width = 2.dp,
                    color = Color.Gray
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}