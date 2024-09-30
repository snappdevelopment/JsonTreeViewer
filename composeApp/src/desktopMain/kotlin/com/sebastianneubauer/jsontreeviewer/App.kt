package com.sebastianneubauer.jsontreeviewer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sebastianneubauer.jsontreeviewer.ui.DragAndDropBox
import com.sebastianneubauer.jsontreeviewer.ui.DragAndDropState
import com.sebastianneubauer.jsontreeviewer.ui.rememberDragAndDropTarget
import jsontreeviewer.composeapp.generated.resources.Res
import jsontreeviewer.composeapp.generated.resources.error
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
    var isHovering by remember { mutableStateOf(false) }

    val dragAndDropTarget = rememberDragAndDropTarget { dragAndDropState ->
        onDragAndDropStateChanged(dragAndDropState)
        isHovering = dragAndDropState.isHovering
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
                is Contract.State.Initial -> Initial(isHovering = isHovering)
                is Contract.State.Loading -> Loading(isHovering = isHovering)
                is Contract.State.Error -> Error(isHovering = isHovering)
                is Contract.State.Content -> Unit
            }
        }
    }
}

@Composable
private fun Initial(
    isHovering: Boolean,
) {
    DragAndDropBox(
        isHovering = isHovering,
    ) {
        Column(
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

@Composable
private fun Loading(
    isHovering: Boolean
) {
    DragAndDropBox(isHovering = isHovering) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Error(
    isHovering: Boolean
) {
    DragAndDropBox(isHovering = isHovering) {
        Text(
            text = stringResource(Res.string.error),
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}