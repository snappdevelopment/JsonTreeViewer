package com.sebastianneubauer.jsontreeviewer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import java.awt.SystemColor.text
import java.awt.datatransfer.DataFlavor
import java.io.File
import javax.swing.text.AbstractDocument.Content

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App(viewModel: ViewModel) {
    Content(
        state = viewModel.state.value,
        onDragAndDropStateChanged = viewModel::updateDragAndDropState
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Content(
    state: Contract.State,
    onDragAndDropStateChanged: (DragAndDropState) -> Unit
) {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var showTargetBorder by remember { mutableStateOf(false) }

            val dragAndDropTarget = rememberDragAndDropTarget { state ->
                onDragAndDropStateChanged(state)
                showTargetBorder = state.isHoveringOverTarget
            }

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.LightGray)
                    .then(
                        if (showTargetBorder)
                            Modifier.border(BorderStroke(3.dp, Color.Black))
                        else
                            Modifier
                    )
                    .dragAndDropTarget(
                        // With "true" as the value of shouldStartDragAndDrop,
                        // drag-and-drop operations are enabled unconditionally.

                        shouldStartDragAndDrop = { true },
                        target = dragAndDropTarget
                    )
            ) {
                val text = (state as? Contract.State.Content)?.json ?: "No text"
                Text(text, Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun rememberDragAndDropTarget(
    onDragAndDropStateChanged: (DragAndDropState) -> Unit
): DragAndDropTarget {
    var dragAndDropState by remember {
        mutableStateOf<DragAndDropState>(DragAndDropState.Initial(isHoveringOverTarget = false))
    }

    LaunchedEffect(dragAndDropState) {
        onDragAndDropStateChanged(dragAndDropState)
    }

    return remember {
        object: DragAndDropTarget {

            override fun onStarted(event: DragAndDropEvent) {
                dragAndDropState = when(val state = dragAndDropState) {
                    is DragAndDropState.Success -> state.copy(isHoveringOverTarget = true)
                    is DragAndDropState.Failure -> state.copy(isHoveringOverTarget = true)
                    is DragAndDropState.Initial -> state.copy(isHoveringOverTarget = true)
                }
            }

            override fun onEnded(event: DragAndDropEvent) {
                dragAndDropState = when(val state = dragAndDropState) {
                    is DragAndDropState.Success -> state.copy(isHoveringOverTarget = false)
                    is DragAndDropState.Failure -> state.copy(isHoveringOverTarget = false)
                    is DragAndDropState.Initial -> state.copy(isHoveringOverTarget = false)
                }
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val transferable = event.awtTransferable
                try {
                    val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                    dragAndDropState = DragAndDropState.Success(files = files, isHoveringOverTarget = false)
                    return true
                } catch (e: Throwable) {
                    dragAndDropState = DragAndDropState.Failure(isHoveringOverTarget = false)
                    return false
                }
            }
        }
    }
}

sealed interface DragAndDropState {
    val isHoveringOverTarget: Boolean

    data class Initial(
        override val isHoveringOverTarget: Boolean
    ): DragAndDropState

    data class Success(
        val files: List<File>,
        override val isHoveringOverTarget: Boolean
    ): DragAndDropState

    data class Failure(
        override val isHoveringOverTarget: Boolean
    ): DragAndDropState

}