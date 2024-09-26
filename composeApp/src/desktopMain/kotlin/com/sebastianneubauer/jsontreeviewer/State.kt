package com.sebastianneubauer.jsontreeviewer

object Contract {

    sealed interface State {
        data object Initial: State
        data object Loading: State
        data class Content(
            val json: String
        ): State
        data object Error: State
    }
}

val supportedFileTypes = listOf("json", "txt")