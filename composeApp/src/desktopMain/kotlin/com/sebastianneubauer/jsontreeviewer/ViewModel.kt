package com.sebastianneubauer.jsontreeviewer

import androidx.compose.runtime.State as ComposeState
import androidx.compose.runtime.mutableStateOf
import com.sebastianneubauer.jsontreeviewer.Contract.State
import com.sebastianneubauer.jsontreeviewer.ui.DragAndDropState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.fileSize
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue

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
                    val newState = state.file
                        .takeIf { it.exists() && it.isFile && it.extension in supportedFileTypes }
                        ?.let { validFile ->
                            val (json, time) = measureTimedValue {
                                validFile.readText()
                            }

                            val fileSize = validFile.toPath().fileSize() / 1024F

                            State.Content(
                                json = json,
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

    fun showJsonParsingError(throwable: Throwable) {
        viewModelState.value = State.Error(
            error = Contract.ErrorType.JsonParserError(message = throwable.localizedMessage)
        )
    }
}