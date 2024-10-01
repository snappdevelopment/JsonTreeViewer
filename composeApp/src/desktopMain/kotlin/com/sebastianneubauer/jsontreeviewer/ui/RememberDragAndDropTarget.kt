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
    onDragAndDropStateChanged: (DragAndDropState) -> Unit,
    onHoveringChanged: (Boolean) -> Unit,
): DragAndDropTarget {

    return remember {
        object: DragAndDropTarget {

            override fun onStarted(event: DragAndDropEvent) {
                onHoveringChanged(true)
            }

            override fun onEnded(event: DragAndDropEvent) {
                onHoveringChanged(false)
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val file = getFile(event)
                val newState = if(file != null) {
                    DragAndDropState.Success(file = file)
                } else {
                    DragAndDropState.Failure
                }
                onDragAndDropStateChanged(newState)
                return newState is DragAndDropState.Success
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

    data class Success(val file: File): DragAndDropState

    data object Failure: DragAndDropState
}