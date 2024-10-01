package com.sebastianneubauer.jsontreeviewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sebastianneubauer.jsontree.JsonTree
import com.sebastianneubauer.jsontreeviewer.ui.DragAndDropBox
import com.sebastianneubauer.jsontreeviewer.ui.DragAndDropState
import com.sebastianneubauer.jsontreeviewer.ui.rememberDragAndDropTarget
import jsontreeviewer.composeapp.generated.resources.Res
import jsontreeviewer.composeapp.generated.resources.chart_box_outline
import jsontreeviewer.composeapp.generated.resources.error_drag_and_drop
import jsontreeviewer.composeapp.generated.resources.error_file_read
import jsontreeviewer.composeapp.generated.resources.error_json_parser
import jsontreeviewer.composeapp.generated.resources.initial_drag_and_drop
import jsontreeviewer.composeapp.generated.resources.plus_circle_outline
import jsontreeviewer.composeapp.generated.resources.stats_filelines
import jsontreeviewer.composeapp.generated.resources.stats_filename
import jsontreeviewer.composeapp.generated.resources.stats_filepath
import jsontreeviewer.composeapp.generated.resources.stats_filereadtime
import jsontreeviewer.composeapp.generated.resources.stats_filesize
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
@Preview
fun App(viewModel: ViewModel) {
    AppUi(
        state = viewModel.state.value,
        onDragAndDropStateChanged = viewModel::updateDragAndDropState,
        onJsonParsingError = viewModel::showJsonParsingError
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppUi(
    state: Contract.State,
    onDragAndDropStateChanged: (DragAndDropState) -> Unit,
    onJsonParsingError: (Throwable) -> Unit,
) {
    var isHovering by remember { mutableStateOf(false) }

    val dragAndDropTarget = rememberDragAndDropTarget(
        onDragAndDropStateChanged = onDragAndDropStateChanged,
        onHoveringChanged = { isHovering = it }
    )

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { true },
                    target = dragAndDropTarget
                )
        ) {
            when(state) {
                is Contract.State.Initial -> Initial(isHovering = isHovering)
                is Contract.State.Loading -> Loading(isHovering = isHovering)
                is Contract.State.Error -> Error(error = state.error, isHovering = isHovering)
                is Contract.State.Content -> Content(json = state.json, stats = state.stats, onJsonParsingError = onJsonParsingError)
            }
        }
    }
}

@Composable
private fun Content(
    json: String,
    stats: Contract.Stats,
    onJsonParsingError: (Throwable) -> Unit
) {
    var showSidebar by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(top = 32.dp)
    ) {
        JsonTree(
            modifier = Modifier.weight(1F),
            json = json,
            onLoading = {
                Box(modifier = Modifier.weight(1F)) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Blue
                    )
                }
            },
            onError = onJsonParsingError
        )

        Column {
            IconButton(
                modifier = Modifier
                    .align(Alignment.End)
                    .background(color = Color.LightGray.copy(alpha = 0.3F), shape = CircleShape),
                onClick = { showSidebar = !showSidebar },
            ) {
                Icon(
                    painter = painterResource(Res.drawable.chart_box_outline),
                    contentDescription = null
                )
            }

            AnimatedVisibility(
                visible = showSidebar,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(250.dp)
                            .padding(bottom = 32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(color = Color.White)
                            .border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(text = stringResource(Res.string.stats_filename, stats.fileName), color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stringResource(Res.string.stats_filepath, stats.filePath), color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stringResource(Res.string.stats_filelines, stats.fileLines), color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stringResource(Res.string.stats_filesize, stats.fileSize), color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stringResource(Res.string.stats_filereadtime, stats.fileReadTime), color = Color.Gray)
                    }
                }

            }

        }
    }
}

@Composable
private fun Initial(
    isHovering: Boolean,
) {
    DragAndDropBox(
        isHovering = isHovering,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = vectorResource(Res.drawable.plus_circle_outline),
                tint = Color.Gray,
                contentDescription = null
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.initial_drag_and_drop),
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun Loading(
    isHovering: Boolean
) {
    DragAndDropBox(isHovering = isHovering) {
        CircularProgressIndicator(color = Color.Blue)
    }
}

@Composable
private fun Error(
    error: Contract.ErrorType,
    isHovering: Boolean
) {
    DragAndDropBox(isHovering = isHovering) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when(error) {
                    is Contract.ErrorType.JsonParserError -> stringResource(Res.string.error_json_parser)
                    is Contract.ErrorType.DataDragAndDropError -> stringResource(Res.string.error_drag_and_drop)
                    is Contract.ErrorType.FileReadError -> stringResource(Res.string.error_file_read)
                },
                textAlign = TextAlign.Center,
                color = Color.Gray
            )

            if(error is Contract.ErrorType.JsonParserError) {
                Text(
                    text = error.message,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }
    }
}