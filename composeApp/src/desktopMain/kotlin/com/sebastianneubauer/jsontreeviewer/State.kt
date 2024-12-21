package com.sebastianneubauer.jsontreeviewer

object Contract {

    sealed interface State {
        data object Initial: State
        data object InitialLoading: State
        data object Loading: State
        data class Error(val error: ErrorType): State
        data class Content(val json: String, val stats: Stats): State
    }

    sealed class ErrorType {
        data object DataDragAndDropError: ErrorType()
        data object FileReadError: ErrorType()
        data object CopyPasteError: ErrorType()
        data class JsonParserError(val message: String): ErrorType()
    }

    data class Stats(
        val filePath: String,
        val fileName: String,
        val fileSize: String,
        val fileReadTime: String,
        val fileLines: String,
    )
}

val supportedFileTypes = listOf("json", "txt")