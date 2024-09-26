package com.sebastianneubauer.jsontreeviewer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

fun main() = application {
    val coroutineScope = CoroutineScope(Job())
    val viewModel = ViewModel(
        coroutineScope = coroutineScope,
        ioDispatcher = Dispatchers.IO
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "JsonTreeViewer",
    ) {
        App(viewModel = viewModel)
    }
}