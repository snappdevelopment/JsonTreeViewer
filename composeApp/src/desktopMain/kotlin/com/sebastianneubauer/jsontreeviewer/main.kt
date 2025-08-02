package com.sebastianneubauer.jsontreeviewer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import jsontreeviewer.composeapp.generated.resources.Res
import jsontreeviewer.composeapp.generated.resources.app_name
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.jetbrains.compose.resources.stringResource

fun main() = application {
    val coroutineScope = CoroutineScope(Job())
    val viewModel = ViewModel(
        coroutineScope = coroutineScope,
        ioDispatcher = Dispatchers.IO,
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_name),
        onKeyEvent = { viewModel.onKeyEvent(it) }
    ) {
        App(viewModel = viewModel)
    }
}