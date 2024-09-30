package com.sebastianneubauer.jsontreeviewer

object Contract {

    sealed interface State {
        data object Initial: State
        data object Loading: State
        data class Error(val error: ErrorType): State
        data class Content(val json: String): State
    }

    sealed class ErrorType {
        data object DataDragAndDropError: ErrorType()
        data object FileReadError: ErrorType()
        data class JsonParserError(val message: String): ErrorType()
    }
}

val supportedFileTypes = listOf("json", "txt")