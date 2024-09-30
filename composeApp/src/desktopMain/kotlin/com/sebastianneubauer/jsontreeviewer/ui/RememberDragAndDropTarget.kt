package com.sebastianneubauer.jsontreeviewer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun rememberDragAndDropTarget(
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