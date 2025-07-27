package com.sebastianneubauer.jsontreeviewer

object Contract {

    sealed interface State {
        data object Initial: State
        data object InitialLoading: State
        data object Loading: State
        data class Error(val error: ErrorType): State
        data class Content(
            val json: String,
            val searchDirection: SearchDirection?,
            val displayMode: DisplayMode
        ): State
    }

    sealed class ErrorType {
        data object DataDragAndDropError: ErrorType()
        data object FileReadError: ErrorType()
        data object CopyPasteError: ErrorType()
    }

    sealed interface SearchDirection {
        val increment: Int

        data class Next(override val increment: Int): SearchDirection
        data class Previous(override val increment: Int): SearchDirection
    }

    enum class DisplayMode {
        Render,
        Edit
    }
}

val supportedFileTypes = listOf("json", "txt")