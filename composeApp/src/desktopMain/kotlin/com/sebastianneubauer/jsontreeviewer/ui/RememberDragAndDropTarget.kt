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
import androidx.compose.ui.draganddrop.awtTransferable
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun rememberDragAndDropTarget(
    onDragAndDropStateChanged: (DragAndDropState) -> Unit
): DragAndDropTarget {
    var dragAndDropState by remember {
        mutableStateOf<DragAndDropState>(DragAndDropState.Initial(isHovering = false))
    }

    LaunchedEffect(dragAndDropState) {
        onDragAndDropStateChanged(dragAndDropState)
    }

    return remember {
        object: DragAndDropTarget {

            override fun onStarted(event: DragAndDropEvent) {
                val state = dragAndDropState
                dragAndDropState = when(state) {
                    is DragAndDropState.Initial -> state.copy(isHovering = true)
                    is DragAndDropState.Success -> state.copy(isHovering = true)
                    is DragAndDropState.Failure -> state.copy(isHovering = true)
                }
            }

            override fun onEnded(event: DragAndDropEvent) {
                val state = dragAndDropState
                dragAndDropState = when(state) {
                    is DragAndDropState.Initial -> state.copy(isHovering = false)
                    is DragAndDropState.Success -> state.copy(isHovering = false)
                    is DragAndDropState.Failure -> state.copy(isHovering = false)
                }
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val file = getFile(event)
                if(file != null) {
                    dragAndDropState = DragAndDropState.Success(
                        file = file,
                        isHovering = false
                    )
                    return true
                } else {
                    dragAndDropState = DragAndDropState.Failure(isHovering = false)
                    return false
                }
            }

            private fun getFile(event: DragAndDropEvent): File? {
                val transferable = event.awtTransferable
                return try {
                    val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                    files.filterIsInstance<File>().firstOrNull()
                } catch (e: Throwable) {
                    null
                }
            }
        }
    }
}

sealed interface DragAndDropState {

    val isHovering: Boolean

    data class Initial(
        override val isHovering: Boolean
    ): DragAndDropState

    data class Success(
        val file: File,
        override val isHovering: Boolean
    ): DragAndDropState

    data class Failure(
        override val isHovering: Boolean
    ): DragAndDropState
}