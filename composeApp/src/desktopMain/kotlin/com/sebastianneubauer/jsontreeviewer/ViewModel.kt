package com.sebastianneubauer.jsontreeviewer

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import com.sebastianneubauer.jsontreeviewer.Contract.State
import com.sebastianneubauer.jsontreeviewer.ui.DragAndDropState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import androidx.compose.runtime.State as ComposeState

class ViewModel(
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private var viewModelState = mutableStateOf<State>(State.Initial)
    val state: ComposeState<State> = viewModelState

    fun updateDragAndDropState(state: DragAndDropState) {
        when(state) {
            is DragAndDropState.Success -> {
                viewModelState.value = when(viewModelState.value) {
                    is State.Initial -> State.InitialLoading
                    else -> State.Loading
                }
                coroutineScope.launch(ioDispatcher) {
                    val newState = state.file
                        .takeIf { it.exists() && it.isFile && it.extension in supportedFileTypes }
                        ?.let { validFile ->
                            val json = validFile.readText()

                            State.Content(
                                json = json,
                                searchDirection = null,
                                displayMode = Contract.DisplayMode.Render,
                            )
                        }
                        ?: State.Error(error = Contract.ErrorType.FileReadError)

                    viewModelState.value = newState
                }
            }
            is DragAndDropState.Failure -> viewModelState.value = State.Error(error = Contract.ErrorType.DataDragAndDropError)
        }
    }

    fun onKeyEvent(event: KeyEvent): Boolean {
        return if ((event.isCtrlPressed || event.isMetaPressed) && event.key == Key.V) {
            val state = viewModelState.value
            if(state is State.Content) {
                false
            } else {
                viewModelState.value = getStateFromClipboardData()
                true
            }
        } else if(event.key == Key.DirectionDown || event.key == Key.Enter) {
            val newState = getStateFromSearchDirectionEvent(isDownEvent = true)
            viewModelState.value = newState
            newState is State.Content
        } else if(event.key == Key.DirectionUp) {
            val newState = getStateFromSearchDirectionEvent(isDownEvent = false)
            viewModelState.value = newState
            newState is State.Content
        } else {
            false
        }
    }

    fun updateDisplayMode(displayMode: Contract.DisplayMode) {
        val state = viewModelState.value
        if(state is State.Content) {
            viewModelState.value = state.copy(displayMode = displayMode)
        }
    }

    fun updateJson(json: String) {
        val state = viewModelState.value
        if(state is State.Content) {
            viewModelState.value = state.copy(json = json)
        }
    }

    fun reset() {
        viewModelState.value = State.Initial
    }

    private fun getStateFromClipboardData(): State {
        val clipboardString = try {
            Toolkit.getDefaultToolkit()
                .systemClipboard
                .getData(DataFlavor.stringFlavor) as String
        } catch (e: Exception) {
            null
        }

        return if(clipboardString != null) {
            State.Content(
                json = clipboardString,
                searchDirection = null,
                displayMode = Contract.DisplayMode.Render,
            )
        } else {
            State.Error(error = Contract.ErrorType.CopyPasteError)
        }
    }

    private fun getStateFromSearchDirectionEvent(isDownEvent: Boolean): State {
        val currentState = viewModelState.value
        return if(currentState is State.Content) {
            val currentSearchDirection = currentState.searchDirection
            val newSearchDirection = if(isDownEvent) {
                when(currentSearchDirection) {
                    is Contract.SearchDirection.Next -> currentSearchDirection.copy(increment = currentSearchDirection.increment + 1)
                    else -> Contract.SearchDirection.Next(increment = 0)
                }
            } else {
                when(currentSearchDirection) {
                    is Contract.SearchDirection.Previous -> currentSearchDirection.copy(increment = currentSearchDirection.increment + 1)
                    else -> Contract.SearchDirection.Previous(increment = 0)
                }
            }
            currentState.copy(searchDirection = newSearchDirection)
        } else currentState
    }
}