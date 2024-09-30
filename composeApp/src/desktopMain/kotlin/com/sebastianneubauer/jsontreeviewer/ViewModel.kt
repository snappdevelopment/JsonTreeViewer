package com.sebastianneubauer.jsontreeviewer

import androidx.compose.runtime.State as ComposeState
import androidx.compose.runtime.mutableStateOf
import com.sebastianneubauer.jsontreeviewer.Contract.State
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

class ViewModel(
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher
) {

    private var viewModelState = mutableStateOf<State>(State.Initial)
    val state: ComposeState<State> = viewModelState

    fun updateDragAndDropState(state: DragAndDropState) {
        when(state) {
            is DragAndDropState.Success -> {
                viewModelState.value = State.Loading
                coroutineScope.launch(ioDispatcher) {
                    val json = readFile(state.filePath)
                    viewModelState.value = if(json != null) {
                        State.Content(json)
                    } else {
                        State.Error
                    }
                }
            }
            is DragAndDropState.Failure -> viewModelState.value = State.Error
            else -> Unit
        }
    }

    private fun readFile(filePath: String): String? {
        return File(filePath)
            .takeIf { it.exists() && it.isFile && it.extension in supportedFileTypes }
            ?.readText()
    }
}