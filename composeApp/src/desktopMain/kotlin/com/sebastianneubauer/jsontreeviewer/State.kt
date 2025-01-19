package com.sebastianneubauer.jsontreeviewer

object Contract {

    sealed interface State {
        data object Initial: State
        data object InitialLoading: State
        data object Loading: State
        data class Error(val error: ErrorType): State
        data class Content(val json: String, val stats: Stats, val searchDirection: SearchDirection?): State
    }

    sealed class ErrorType {
        data object DataDragAndDropError: ErrorType()
        data object FileReadError: ErrorType()
        data object CopyPasteError: ErrorType()
        data class JsonParserError(val message: String): ErrorType()
    }

    sealed interface SearchDirection {
        val increment: Int

        data class Next(override val increment: Int): SearchDirection
        data class Previous(override val increment: Int): SearchDirection
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