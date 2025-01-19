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
import kotlin.io.path.fileSize
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import kotlin.time.measureTimedValue
import androidx.compose.runtime.State as ComposeState

class ViewModel(
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher,
    private val timeSource: TimeSource,
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
                            val (json, time) = timeSource.measureTimedValue { validFile.readText() }
                            val fileSize = validFile.toPath().fileSize() / 1024F

                            State.Content(
                                json = json,
                                searchDirection = null,
                                stats = Contract.Stats(
                                    filePath = validFile.path,
                                    fileName = validFile.name,
                                    fileSize = "%.2f".format(fileSize) + "KB",
                                    fileReadTime = time.toString(unit = DurationUnit.MILLISECONDS, decimals = 3),
                                    fileLines = json.lines().count().toString()
                                )
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
            viewModelState.value = getStateFromClipboardData()
            true
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
                stats = Contract.Stats(
                    filePath = "from clipboard",
                    fileName = "n/a",
                    fileSize = "n/a",
                    fileReadTime = "n/a",
                    fileLines = clipboardString.lines().count().toString()
                )
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

    fun showJsonParsingError(throwable: Throwable) {
        viewModelState.value = State.Error(
            error = Contract.ErrorType.JsonParserError(message = throwable.localizedMessage)
        )
    }
}