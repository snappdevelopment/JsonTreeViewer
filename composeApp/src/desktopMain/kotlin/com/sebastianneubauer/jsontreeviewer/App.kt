package com.sebastianneubauer.jsontreeviewer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sebastianneubauer.jsontreeviewer.ui.DragAndDropState
import com.sebastianneubauer.jsontreeviewer.ui.rememberDragAndDropTarget
import jsontreeviewer.composeapp.generated.resources.Res
import jsontreeviewer.composeapp.generated.resources.initial_drag_and_drop
import jsontreeviewer.composeapp.generated.resources.plus_circle_outline
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
@Preview
fun App(viewModel: ViewModel) {
    AppUi(
        state = viewModel.state.value,
        onDragAndDropStateChanged = viewModel::updateDragAndDropState
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppUi(
    state: Contract.State,
    onDragAndDropStateChanged: (DragAndDropState) -> Unit
) {
    var hoveringState by remember { mutableStateOf(DragAndDropState.HoveringState.NONE) }

    val dragAndDropTarget = rememberDragAndDropTarget { dragAndDropState ->
        onDragAndDropStateChanged(dragAndDropState)
        hoveringState = when(dragAndDropState) {
            is DragAndDropState.Initial -> dragAndDropState.hoveringState
            is DragAndDropState.Failure,
            is DragAndDropState.Success -> DragAndDropState.HoveringState.NONE
        }
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { true },
                    target = dragAndDropTarget
                )
        ) {
            when(state) {
                is Contract.State.Initial -> Initial(
                    isHovering = hoveringState == DragAndDropState.HoveringState.SUPPORTED
                )
                is Contract.State.Loading -> Unit
                is Contract.State.Error -> Unit
                is Contract.State.Content -> Unit
            }
        }
    }
}

@Composable
private fun Initial(
    isHovering: Boolean,
) {
    Box(modifier = Modifier.fillMaxSize()) {
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
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = vectorResource(Res.drawable.plus_circle_outline),
                tint = Color.Gray,
                contentDescription = null
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.initial_drag_and_drop),
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
    }
}

