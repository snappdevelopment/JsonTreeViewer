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
import androidx.compose.material.DrawerDefaults.shape
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
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jsontreeviewer.composeapp.generated.resources.Res
import jsontreeviewer.composeapp.generated.resources.initial_drag_and_drop
import jsontreeviewer.composeapp.generated.resources.plus_circle_outline
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import java.awt.datatransfer.DataFlavor
import java.io.File

@Composable
@Preview
fun App(viewModel: ViewModel) {
    Content(
        state = viewModel.state.value,
        onDragAndDropStateChanged = viewModel::updateDragAndDropState
    )
}

@Composable
private fun Content(
    state: Contract.State,
    onDragAndDropStateChanged: (DragAndDropState) -> Unit
) {
    MaterialTheme {
        when(state) {
            is Contract.State.Initial -> Initial(onDragAndDropStateChanged)
            is Contract.State.Loading -> Unit
            is Contract.State.Error -> Unit
            is Contract.State.Content -> Unit
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Initial(
    onDragAndDropStateChanged: (DragAndDropState) -> Unit
) {
    var hoveringState by remember { mutableStateOf(DragAndDropState.HoveringState.NONE) }

    val dragAndDropTarget = rememberDragAndDropTarget { state ->
        onDragAndDropStateChanged(state)
        hoveringState = when(state) {
            is DragAndDropState.Initial -> state.hoveringState
            is DragAndDropState.Failure,
            is DragAndDropState.Success -> DragAndDropState.HoveringState.NONE
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = dragAndDropTarget
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .background(
                    color = when(hoveringState) {
                        DragAndDropState.HoveringState.NONE -> Color.Transparent
                        DragAndDropState.HoveringState.SUPPORTED -> Color.LightGray//Color(0x50EB9792)
                        DragAndDropState.HoveringState.UNSUPPORTED -> Color(0x509DEBA5)
                    },
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

@Composable
private fun Content(
    json: String
) {

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun rememberDragAndDropTarget(
    onDragAndDropStateChanged: (DragAndDropState) -> Unit
): DragAndDropTarget {
    var dragAndDropState by remember {
        mutableStateOf<DragAndDropState>(DragAndDropState.Initial(hoveringState = DragAndDropState.HoveringState.NONE))
    }

    LaunchedEffect(dragAndDropState) {
        onDragAndDropStateChanged(dragAndDropState)
    }

    return remember {
        object: DragAndDropTarget {

            override fun onStarted(event: DragAndDropEvent) {
                val state = dragAndDropState
                if(state is DragAndDropState.Initial) {
                    dragAndDropState = state.copy(hoveringState = DragAndDropState.HoveringState.SUPPORTED)
                }
            }

            override fun onEnded(event: DragAndDropEvent) {
                val state = dragAndDropState
                if(state is DragAndDropState.Initial) {
                    dragAndDropState = state.copy(hoveringState = DragAndDropState.HoveringState.NONE)
                }
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val filePath = getFilePath(event)
                if(filePath != null) {
                    dragAndDropState = DragAndDropState.Success(filePath = filePath)
                    return true
                } else {
                    dragAndDropState = DragAndDropState.Failure
                    return false
                }
            }

            private fun getFilePath(event: DragAndDropEvent): String? {
                val dragData = event.dragData()
                return if(dragData is DragData.FilesList) {
                    dragData.readFiles().firstOrNull()
                } else {
                    null
                }
            }
        }
    }
}

sealed interface DragAndDropState {

    data class Initial(
        val hoveringState: HoveringState
    ): DragAndDropState

    data class Success(
        val filePath: String
    ): DragAndDropState

    data object Failure: DragAndDropState

    enum class HoveringState {
        NONE,
        SUPPORTED,
        UNSUPPORTED
    }
}